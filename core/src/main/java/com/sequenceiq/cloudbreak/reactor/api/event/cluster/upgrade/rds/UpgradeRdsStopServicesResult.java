package com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.rds;

import com.sequenceiq.cloudbreak.common.database.TargetMajorVersion;

public class UpgradeRdsStopServicesResult extends AbstractUpgradeRdsEvent {

    public UpgradeRdsStopServicesResult(Long stackId, TargetMajorVersion version) {
        super(stackId, version);
    }
}
