package com.sequenceiq.distrox.api.v1.distrox.model.upgrade.rds;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.sequenceiq.cloudbreak.validation.ValidRdsUpgradeRequest;

public class DistroXRdsUpgradeRequestValidator implements ConstraintValidator<ValidRdsUpgradeRequest, DistroXRdsUpgradeV1Request> {

    @Override
    public boolean isValid(DistroXRdsUpgradeV1Request value, ConstraintValidatorContext context) {
        return true;
    }
}

