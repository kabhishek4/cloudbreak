package com.sequenceiq.distrox.api.v1.distrox.model.upgrade.rds;

import com.sequenceiq.cloudbreak.common.database.TargetMajorVersion;
import com.sequenceiq.cloudbreak.validation.ValidRdsUpgradeRequest;

@ValidRdsUpgradeRequest
public class DistroXRdsUpgradeV1Request {

    private TargetMajorVersion targetVersion;

    public TargetMajorVersion getTargetVersion() {
        return targetVersion;
    }

    public void setTargetVersion(TargetMajorVersion targetVersion) {
        this.targetVersion = targetVersion;
    }

    @Override
    public String toString() {
        return "DistroXRdsUpgradeV1Request{" +
                "targetVersion=" + targetVersion +
                '}';
    }
}
