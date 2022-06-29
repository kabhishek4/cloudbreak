package com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.rds;

import com.sequenceiq.cloudbreak.common.database.TargetMajorVersion;

public class UpgradeRdsUpgradeDatabaseServerRequest extends AbstractUpgradeRdsEvent {

    public UpgradeRdsUpgradeDatabaseServerRequest(Long stackId, TargetMajorVersion version) {
        super(stackId, version);
    }
}
