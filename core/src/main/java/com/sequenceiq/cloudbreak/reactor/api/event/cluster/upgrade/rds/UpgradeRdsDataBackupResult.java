package com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.rds;

import com.sequenceiq.cloudbreak.common.database.TargetMajorVersion;

public class UpgradeRdsDataBackupResult extends AbstractUpgradeRdsEvent {

    public UpgradeRdsDataBackupResult(Long stackId, TargetMajorVersion version) {
        super(stackId, version);
    }
}
