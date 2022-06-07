package com.sequenceiq.cloudbreak.cloud.event.instance;

import java.util.Collections;
import java.util.List;

import com.sequenceiq.cloudbreak.cloud.event.CloudPlatformResult;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;

public class StopStartUpscaleGetStartedInstancesResult extends CloudPlatformResult {

    private final StopStartUpscaleGetStartedInstancesRequest getStartedInstancesRequest;

    private final List<CloudInstance> startedInstancesWithServicesNotRunning;

    private final Integer adjustment;

    private final String hostGroupName;

    public StopStartUpscaleGetStartedInstancesResult(Long resourceId, StopStartUpscaleGetStartedInstancesRequest getStartedInstancesRequest,
            List<CloudInstance> startedInstancesWithServicesNotRunning, Integer adjustment, String hostGroupName) {
        super(resourceId);
        this.getStartedInstancesRequest = getStartedInstancesRequest;
        this.startedInstancesWithServicesNotRunning = startedInstancesWithServicesNotRunning;
        this.adjustment = adjustment;
        this.hostGroupName = hostGroupName;
    }

    public StopStartUpscaleGetStartedInstancesResult(String statusReason, Exception errorDetails, Long resourceId,
            StopStartUpscaleGetStartedInstancesRequest getStartedInstancesRequest) {
        super(statusReason, errorDetails, resourceId);
        this.getStartedInstancesRequest = getStartedInstancesRequest;
        this.startedInstancesWithServicesNotRunning = Collections.emptyList();
        this.adjustment = 0;
        this.hostGroupName = null;
    }

    public StopStartUpscaleGetStartedInstancesRequest getGetStartedInstancesRequest() {
        return getStartedInstancesRequest;
    }

    public List<CloudInstance> getStartedInstancesWithServicesNotRunning() {
        return startedInstancesWithServicesNotRunning;
    }

    public Integer getAdjustment() {
        return adjustment;
    }

    public String getHostGroupName() {
        return hostGroupName;
    }
}
