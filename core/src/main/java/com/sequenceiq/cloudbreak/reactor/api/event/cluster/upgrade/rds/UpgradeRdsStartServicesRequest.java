package com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.rds;

import com.sequenceiq.cloudbreak.common.database.TargetMajorVersion;

public class UpgradeRdsStartServicesRequest extends AbstractUpgradeRdsEvent {

    public UpgradeRdsStartServicesRequest(Long stackId, TargetMajorVersion version) {
        super(stackId, version);
    }

}
