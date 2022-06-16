package com.sequenceiq.cloudbreak.reactor.handler.cluster.atlas;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.common.event.Selectable;
import com.sequenceiq.cloudbreak.core.bootstrap.service.ClusterDeletionBasedExitCriteriaModel;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.domain.stack.cluster.Cluster;
import com.sequenceiq.cloudbreak.domain.stack.instance.InstanceMetaData;
import com.sequenceiq.cloudbreak.orchestrator.exception.CloudbreakOrchestratorFailedException;
import com.sequenceiq.cloudbreak.orchestrator.host.HostOrchestrator;
import com.sequenceiq.cloudbreak.orchestrator.model.GatewayConfig;
import com.sequenceiq.cloudbreak.orchestrator.model.SaltConfig;
import com.sequenceiq.cloudbreak.orchestrator.state.ExitCriteriaModel;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.atlas.CheckAtlasUpdatedSaltFailedEvent;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.atlas.CheckAtlasUpdatedRequest;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.atlas.CheckAtlasUpdatedSaltSuccessEvent;
import com.sequenceiq.cloudbreak.service.GatewayConfigService;
import com.sequenceiq.cloudbreak.service.stack.StackService;
import com.sequenceiq.cloudbreak.util.StackUtil;
import com.sequenceiq.flow.event.EventSelectorUtil;
import com.sequenceiq.flow.reactor.api.handler.ExceptionCatcherEventHandler;
import com.sequenceiq.flow.reactor.api.handler.HandlerEvent;

import reactor.bus.Event;

@Component
public class CheckAtlasUpdatedHandler extends ExceptionCatcherEventHandler<CheckAtlasUpdatedRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckAtlasUpdatedHandler.class);

    private static final int MAX_RETRIES = 120;

    @Inject
    private CheckAtlasUpdatedSaltConfigGenerator saltConfigGenerator;

    @Inject
    private HostOrchestrator hostOrchestrator;

    @Inject
    private StackService stackService;

    @Inject
    private GatewayConfigService gatewayConfigService;

    @Inject
    private StackUtil stackUtil;

    @Override
    public String selector() {
        return EventSelectorUtil.selector(CheckAtlasUpdatedRequest.class);
    }

    @Override
    protected Selectable defaultFailureEvent(Long stackId, Exception e, Event<CheckAtlasUpdatedRequest> request) {
        LOGGER.error("Failed to check atlas up-to-date state for stack with ID: " + stackId.toString(), e);
        return new CheckAtlasUpdatedSaltFailedEvent(stackId, e);
    }

    @Override
    protected Selectable doAccept(HandlerEvent<CheckAtlasUpdatedRequest> event) {
        CheckAtlasUpdatedRequest request = event.getData();
        LOGGER.info("Attempting to check whether Atlas is up to date for stack with ID {}", request.getResourceId());
        Stack stack = stackService.getByIdWithListsInTransaction(request.getResourceId());
        Cluster cluster = stack.getCluster();
        InstanceMetaData gatewayInstance = stack.getPrimaryGatewayInstance();
        GatewayConfig gatewayConfig = gatewayConfigService.getGatewayConfig(stack, gatewayInstance, cluster.hasGateway());
        Set<String> gatewayFQDN = Collections.singleton(gatewayInstance.getDiscoveryFQDN());
        ExitCriteriaModel exitModel = ClusterDeletionBasedExitCriteriaModel.clusterDeletionBasedModel(stack.getId(), cluster.getId());

        SaltConfig saltConfig = saltConfigGenerator.createSaltConfig(stack, MAX_RETRIES);
        try {
            hostOrchestrator.checkAtlasUpdated(gatewayConfig, gatewayFQDN, stackUtil.collectReachableNodes(stack), saltConfig, exitModel);
            return new CheckAtlasUpdatedSaltSuccessEvent(stack.getId());
        } catch (CloudbreakOrchestratorFailedException e) {
            LOGGER.error("Salt failed to check atlas up-to-date state.", e);
            return new CheckAtlasUpdatedSaltFailedEvent(stack.getId(), e);
        }
    }
}
