package com.sequenceiq.redbeams.service.network;

import static com.sequenceiq.cloudbreak.common.network.NetworkConstants.SUBNET_ID;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.sequenceiq.cloudbreak.cloud.model.CloudSubnet;
import com.sequenceiq.cloudbreak.common.mappable.CloudPlatform;
import com.sequenceiq.cloudbreak.converter.ServiceEndpointCreationToEndpointTypeConverter;
import com.sequenceiq.common.model.PrivateEndpointType;
import com.sequenceiq.environment.api.v1.environment.model.response.DetailedEnvironmentResponse;
import com.sequenceiq.redbeams.domain.stack.DBStack;
import com.sequenceiq.redbeams.exception.RedbeamsException;

@Service
public class NetworkParameterAdder {

    // These constants must match those in AwsNetworkView, AzureNetworkView and/or GcpDatabaseNetworkView
    private static final String VPC_ID = "vpcId";

    private static final String VPC_CIDR = "vpcCidr";

    private static final String SHARED_PROJECT_ID = "sharedProjectId";

    private static final String VPC_CIDRS = "vpcCidrs";

    private static final String AVAILABILITY_ZONE = "availabilityZone";

    private static final String ENDPOINT_TYPE = "endpointType";

    private static final String SUBNET_FOR_PRIVATE_ENDPOINT = "subnetForPrivateEndpoint";

    private static final String EXISTING_PRIVATE_DNS_ZONE_ID = "existingDatabasePrivateDnsZoneId";

    private static final String SUBNETS = "subnets";

    @Inject
    private ServiceEndpointCreationToEndpointTypeConverter serviceEndpointCreationToEndpointTypeConverter;

    @Inject
    private SubnetListerService subnetListerService;

    @Inject
    private SubnetChooserService subnetChooserService;

    public Map<String, Object> addSubnetIds(Map<String, Object> parameters, List<String> subnetIds, List<String> azs, CloudPlatform cloudPlatform) {
        switch (cloudPlatform) {
            case AWS:
            case GCP:
            case MOCK:
                parameters.put(SUBNET_ID, String.join(",", subnetIds));
                parameters.put(AVAILABILITY_ZONE, String.join(",", azs));
                break;
            case AZURE:
                parameters.put(SUBNETS, String.join(",", subnetIds));
                break;
            default:
                throw new RedbeamsException(String.format("Support for cloud platform %s not yet added", cloudPlatform.name()));
        }
        return parameters;
    }

    public Map<String, Object> addParameters(
            Map<String, Object> parameters, DetailedEnvironmentResponse environmentResponse, CloudPlatform cloudPlatform, DBStack dbStack) {
        switch (cloudPlatform) {
            case AWS:
                parameters.put(VPC_CIDR, environmentResponse.getNetwork().getNetworkCidr());
                parameters.put(VPC_CIDRS, environmentResponse.getNetwork().getNetworkCidrs());
                parameters.put(VPC_ID, environmentResponse.getNetwork().getAws().getVpcId());
                break;
            case AZURE:
                PrivateEndpointType privateEndpointType
                        = serviceEndpointCreationToEndpointTypeConverter.convert(
                                environmentResponse.getNetwork().getServiceEndpointCreation(), cloudPlatform.name());
                parameters.put(ENDPOINT_TYPE, privateEndpointType);
                if (PrivateEndpointType.USE_PRIVATE_ENDPOINT == privateEndpointType) {
                    parameters.put(SUBNET_FOR_PRIVATE_ENDPOINT, getAzureSubnetToUseWithPrivateEndpoint(environmentResponse, dbStack));
                    parameters.put(EXISTING_PRIVATE_DNS_ZONE_ID, environmentResponse.getNetwork().getAzure().getDatabasePrivateDnsZoneId());
                }
                break;
            case GCP:
                if (!Strings.isNullOrEmpty(environmentResponse.getNetwork().getGcp().getSharedProjectId())) {
                    parameters.put(SHARED_PROJECT_ID, environmentResponse.getNetwork().getGcp().getSharedProjectId());
                }
                break;
            case MOCK:
                parameters.put(VPC_ID, environmentResponse.getNetwork().getMock().getVpcId());
                break;
            default:
                throw new RedbeamsException(String.format("Support for cloud platform %s not yet added", cloudPlatform.name()));
        }
        return parameters;
    }

    private String getAzureSubnetToUseWithPrivateEndpoint(DetailedEnvironmentResponse detailedEnvironmentResponse, DBStack dbStack) {
        String subscriptionId = subnetListerService.getAzureSubscriptionId(detailedEnvironmentResponse.getCrn());
        return subnetChooserService.chooseSubnetForPrivateEndpoint(
                detailedEnvironmentResponse.getNetwork().getSubnetMetas().values(), dbStack, detailedEnvironmentResponse.getNetwork().isExistingNetwork())
                .stream()
                .findFirst()
                .map(csn -> subnetListerService.expandAzureResourceId(csn, detailedEnvironmentResponse, subscriptionId))
                .map(CloudSubnet::getId).orElseThrow(() -> new RedbeamsException("It is not possible to create private endpoints for database: " +
                        "there are no subnets with privateEndpointNetworkPolicies disabled"));
    }
}
