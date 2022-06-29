package com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.rds;

import com.sequenceiq.cloudbreak.common.database.TargetMajorVersion;

public class UpgradeRdsDataBackupRequest extends AbstractUpgradeRdsEvent {

    public UpgradeRdsDataBackupRequest(Long stackId, TargetMajorVersion version) {
        super(stackId, version);
    }
}
