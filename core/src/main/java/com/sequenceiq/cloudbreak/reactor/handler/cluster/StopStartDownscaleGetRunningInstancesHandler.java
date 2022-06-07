package com.sequenceiq.cloudbreak.reactor.handler.cluster;

import static com.sequenceiq.cloudbreak.cloud.model.HostName.hostName;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.cloud.CloudConnector;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.context.CloudContext;
import com.sequenceiq.cloudbreak.cloud.handler.CloudPlatformEventHandler;
import com.sequenceiq.cloudbreak.cloud.init.CloudPlatformConnectors;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.CloudVmInstanceStatus;
import com.sequenceiq.cloudbreak.cloud.model.InstanceStatus;
import com.sequenceiq.cloudbreak.cluster.api.ClusterStatusService;
import com.sequenceiq.cloudbreak.cluster.status.ExtendedHostStatuses;
import com.sequenceiq.cloudbreak.converter.CloudInstanceIdToInstanceMetaDataConverter;
import com.sequenceiq.cloudbreak.converter.spi.InstanceMetaDataToCloudInstanceConverter;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.domain.stack.instance.InstanceMetaData;
import com.sequenceiq.cloudbreak.cloud.event.instance.StopStartDownscaleGetRunningInstancesRequest;
import com.sequenceiq.cloudbreak.cloud.event.instance.StopStartDownscaleGetRunningInstancesResult;
import com.sequenceiq.cloudbreak.service.cluster.ClusterApiConnectors;
import com.sequenceiq.cloudbreak.service.stack.RuntimeVersionService;
import com.sequenceiq.cloudbreak.service.stack.StackService;

import reactor.bus.Event;
import reactor.bus.EventBus;

@Component
public class StopStartDownscaleGetRunningInstancesHandler implements CloudPlatformEventHandler<StopStartDownscaleGetRunningInstancesRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StopStartDownscaleGetRunningInstancesHandler.class);

    @Inject
    private CloudPlatformConnectors cloudPlatformConnectors;

    @Inject
    private ClusterApiConnectors clusterApiConnectors;

    @Inject
    private StackService stackService;

    @Inject
    private RuntimeVersionService runtimeVersionService;

    @Inject
    private CloudInstanceIdToInstanceMetaDataConverter cloudInstanceIdToInstanceMetaDataConverter;

    @Inject
    private InstanceMetaDataToCloudInstanceConverter instanceMetaDataToCloudInstanceConverter;

    @Inject
    private EventBus eventBus;

    @Override
    public Class<StopStartDownscaleGetRunningInstancesRequest> type() {
        return StopStartDownscaleGetRunningInstancesRequest.class;
    }

    @Override
    public void accept(Event<StopStartDownscaleGetRunningInstancesRequest> event) {
        StopStartDownscaleGetRunningInstancesRequest request = event.getData();
        LOGGER.info("StopStartDownscaleGetRunningInstancesHandler: {}", event.getData().getResourceId());

        CloudContext cloudContext = request.getCloudContext();

        try {
            CloudConnector<?> connector  = cloudPlatformConnectors.get(cloudContext.getPlatformVariant());
            AuthenticatedContext ac = getAuthenticatedContext(request, cloudContext, connector);

            Stack stack = stackService.getByIdWithLists(request.getResourceId());
            List<InstanceMetaData> startedInstancesMetadata = cloudInstanceIdToInstanceMetaDataConverter.getNotDeletedAndNotZombieInstances(
                    stack, request.getHostGroupName(), collectStartedInstancesFromCloudProvider(connector, ac, request.getAllInstancesInHostGroup()).stream()
                            .map(CloudInstance::getInstanceId)
                            .collect(Collectors.toSet()));

            ClusterStatusService clusterStatusService = clusterApiConnectors.getConnector(stack).clusterStatusService();

            ExtendedHostStatuses extendedHostStatuses = clusterStatusService.getExtendedHostStatuses(
                    runtimeVersionService.getRuntimeVersion(stack.getCluster().getId()));

            List<InstanceMetaData> startedInstanceMetadataWithServicesNotRunning = startedInstancesMetadata.stream()
                    .filter(i -> i.getInstanceGroupName().equals(request.getHostGroupName()))
                    .filter(i -> !extendedHostStatuses.isHostHealthy(hostName(i.getDiscoveryFQDN())))
                    .collect(Collectors.toList());

            List<CloudInstance> instancesWithServicesNotRunning = instanceMetaDataToCloudInstanceConverter.convert(
                    startedInstanceMetadataWithServicesNotRunning, stack.getEnvironmentCrn(), stack.getStackAuthentication());
            StopStartDownscaleGetRunningInstancesResult result = new StopStartDownscaleGetRunningInstancesResult(stack.getId(),
                    instancesWithServicesNotRunning, request.getHostGroupName(), request.getHostIds());
            notify(result, event);
        } catch (Exception e) {
            String errorMessage = "Failed to collect started instances with unhealthy services from cloud provider";
            LOGGER.error(errorMessage, e);
            StopStartDownscaleGetRunningInstancesResult result = new StopStartDownscaleGetRunningInstancesResult(errorMessage, e, request.getResourceId(),
                    request.getHostGroupName());
            notify(result, event);
        }

    }

    private List<CloudInstance> collectStartedInstancesFromCloudProvider(CloudConnector<?> connector, AuthenticatedContext ac,
            List<CloudInstance> cloudInstances) {
        List<CloudVmInstanceStatus> vmInstanceStatuses = connector.instances().checkWithoutRetry(ac, cloudInstances);
        return vmInstanceStatuses.stream()
                .filter(vm -> InstanceStatus.STARTED.equals(vm.getStatus()))
                .map(CloudVmInstanceStatus::getCloudInstance)
                .collect(Collectors.toList());
    }

    private AuthenticatedContext getAuthenticatedContext(StopStartDownscaleGetRunningInstancesRequest request, CloudContext cloudContext,
            CloudConnector<?> connector) {
        return connector.authentication().authenticate(cloudContext, request.getCloudCredential());
    }

    protected void notify(StopStartDownscaleGetRunningInstancesResult result, Event<StopStartDownscaleGetRunningInstancesRequest> event) {
        eventBus.notify(result.selector(), new Event<>(event.getHeaders(), result));
    }
}
