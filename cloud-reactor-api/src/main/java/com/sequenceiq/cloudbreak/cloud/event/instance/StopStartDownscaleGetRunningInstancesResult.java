package com.sequenceiq.cloudbreak.cloud.event.instance;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.sequenceiq.cloudbreak.cloud.event.CloudPlatformResult;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;

public class StopStartDownscaleGetRunningInstancesResult extends CloudPlatformResult {

    private final List<CloudInstance> runningInstancesWithServicesNotRunning;

    private final String hostGroupName;

    private final Set<Long> hostIds;

    public StopStartDownscaleGetRunningInstancesResult(Long resourceId, List<CloudInstance> runningInstancesWithServicesNotRunning,
            String hostGroupName, Set<Long> hostIds) {
        super(resourceId);
        this.runningInstancesWithServicesNotRunning = runningInstancesWithServicesNotRunning;
        this.hostGroupName = hostGroupName;
        this.hostIds = hostIds;
    }

    public StopStartDownscaleGetRunningInstancesResult(String statusReason, Exception errorDetails, Long resourceId, String hostGroup) {
        super(statusReason, errorDetails, resourceId);
        this.hostGroupName = hostGroup;
        this.runningInstancesWithServicesNotRunning = Collections.emptyList();
        this.hostIds = Collections.emptySet();
    }

    public List<CloudInstance> getRunningInstancesWithServicesNotRunning() {
        return runningInstancesWithServicesNotRunning;
    }

    public String getHostGroupName() {
        return hostGroupName;
    }

    public Set<Long> getHostIds() {
        return hostIds;
    }
}
