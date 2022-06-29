package com.sequenceiq.cloudbreak.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.sequenceiq.distrox.api.v1.distrox.model.upgrade.rds.DistroXRdsUpgradeRequestValidator;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DistroXRdsUpgradeRequestValidator.class)
public @interface ValidRdsUpgradeRequest {

    String message() default "Invalid external database upgrade request";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}