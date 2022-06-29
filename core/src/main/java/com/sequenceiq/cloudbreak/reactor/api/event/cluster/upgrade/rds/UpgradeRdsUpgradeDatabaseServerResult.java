package com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.rds;

import com.sequenceiq.cloudbreak.common.database.TargetMajorVersion;

public class UpgradeRdsUpgradeDatabaseServerResult extends AbstractUpgradeRdsEvent {

    public UpgradeRdsUpgradeDatabaseServerResult(Long stackId, TargetMajorVersion version) {
        super(stackId, version);
    }
}
