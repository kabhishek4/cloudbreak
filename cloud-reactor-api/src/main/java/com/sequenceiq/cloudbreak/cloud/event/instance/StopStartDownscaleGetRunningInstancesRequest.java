package com.sequenceiq.cloudbreak.cloud.event.instance;

import java.util.List;
import java.util.Set;

import com.sequenceiq.cloudbreak.cloud.context.CloudContext;
import com.sequenceiq.cloudbreak.cloud.event.resource.CloudStackRequest;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;

public class StopStartDownscaleGetRunningInstancesRequest extends CloudStackRequest<StopStartDownscaleGetRunningInstancesResult> {

    private final String hostGroupName;

    private final List<CloudInstance> allInstancesInHostGroup;

    private final Set<Long> hostIds;

    public StopStartDownscaleGetRunningInstancesRequest(CloudContext cloudContext, CloudCredential cloudCredential, CloudStack cloudStack, String hostGroupName,
            List<CloudInstance> allInstancesInHostGroup, Set<Long> hostIds) {
        super(cloudContext, cloudCredential, cloudStack);
        this.hostGroupName = hostGroupName;
        this.allInstancesInHostGroup = allInstancesInHostGroup;
        this.hostIds = hostIds;
    }

    public String getHostGroupName() {
        return hostGroupName;
    }

    public List<CloudInstance> getAllInstancesInHostGroup() {
        return allInstancesInHostGroup;
    }

    public Set<Long> getHostIds() {
        return hostIds;
    }
}
