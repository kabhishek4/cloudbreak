package com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.rds;

import com.sequenceiq.cloudbreak.common.database.TargetMajorVersion;
import com.sequenceiq.cloudbreak.common.event.AcceptResult;

import reactor.rx.Promise;

public class UpgradeRdsTriggerRequest extends AbstractUpgradeRdsEvent {

    public UpgradeRdsTriggerRequest(Long stackId, TargetMajorVersion version) {
        super(stackId, version);
    }

    public UpgradeRdsTriggerRequest(String selector, Long stackId, TargetMajorVersion version) {
        super(selector, stackId, version);
    }

    public UpgradeRdsTriggerRequest(String selector, Long stackId, Promise<AcceptResult> accepted, TargetMajorVersion version) {
        super(selector, stackId, accepted, version);
    }
}
