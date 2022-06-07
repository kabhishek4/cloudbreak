package com.sequenceiq.cloudbreak.cloud.event.instance;

import java.util.List;

import com.sequenceiq.cloudbreak.cloud.context.CloudContext;
import com.sequenceiq.cloudbreak.cloud.event.resource.CloudStackRequest;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;

public class StopStartUpscaleGetStartedInstancesRequest extends CloudStackRequest<StopStartUpscaleStartInstancesResult> {

    private final String hostGroupName;

    private final Integer adjustment;

    private final List<CloudInstance> allInstancesInHostGroup;

    public StopStartUpscaleGetStartedInstancesRequest(CloudContext cloudContext, CloudCredential cloudCredential, CloudStack cloudStack,
            String hostGroupName, Integer adjustment, List<CloudInstance> allInstancesInHostGroup) {
        super(cloudContext, cloudCredential, cloudStack);
        this.hostGroupName = hostGroupName;
        this.adjustment = adjustment;
        this.allInstancesInHostGroup = allInstancesInHostGroup;
    }

    public String getHostGroupName() {
        return hostGroupName;
    }

    public List<CloudInstance> getAllInstancesInHostGroup() {
        return allInstancesInHostGroup;
    }

    public Integer getAdjustment() {
        return adjustment;
    }
}
