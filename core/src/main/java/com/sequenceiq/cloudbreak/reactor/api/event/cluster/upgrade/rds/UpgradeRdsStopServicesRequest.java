package com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.rds;

import com.sequenceiq.cloudbreak.common.database.TargetMajorVersion;
import com.sequenceiq.cloudbreak.common.event.AcceptResult;

import reactor.rx.Promise;

public class UpgradeRdsStopServicesRequest extends AbstractUpgradeRdsEvent {

    public UpgradeRdsStopServicesRequest(Long stackId, TargetMajorVersion version) {
        super(stackId, version);
    }

    public UpgradeRdsStopServicesRequest(String selector, Long stackId, TargetMajorVersion version) {
        super(selector, stackId, version);
    }

    public UpgradeRdsStopServicesRequest(String selector, Long stackId, Promise<AcceptResult> accepted, TargetMajorVersion version) {
        super(selector, stackId, accepted, version);
    }
}
