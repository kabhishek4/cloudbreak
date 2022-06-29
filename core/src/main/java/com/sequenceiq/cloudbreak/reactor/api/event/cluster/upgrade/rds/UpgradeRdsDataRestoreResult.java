package com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.rds;

import com.sequenceiq.cloudbreak.common.database.TargetMajorVersion;

public class UpgradeRdsDataRestoreResult extends AbstractUpgradeRdsEvent {

    public UpgradeRdsDataRestoreResult(Long stackId, TargetMajorVersion version) {
        super(stackId, version);
    }

}
