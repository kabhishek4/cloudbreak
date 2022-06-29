package com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.rds;

import com.sequenceiq.cloudbreak.common.database.TargetMajorVersion;

public class UpgradeRdsStartServicesResult extends AbstractUpgradeRdsEvent {

    public UpgradeRdsStartServicesResult(Long stackId, TargetMajorVersion version) {
        super(stackId, version);
    }

}
