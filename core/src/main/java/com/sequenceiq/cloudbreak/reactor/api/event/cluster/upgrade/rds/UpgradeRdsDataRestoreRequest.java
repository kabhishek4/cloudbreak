package com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.rds;

import com.sequenceiq.cloudbreak.common.database.TargetMajorVersion;

public class UpgradeRdsDataRestoreRequest extends AbstractUpgradeRdsEvent {

    public UpgradeRdsDataRestoreRequest(Long stackId, TargetMajorVersion version) {
        super(stackId, version);
    }

}
