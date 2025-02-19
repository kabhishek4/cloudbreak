package com.sequenceiq.cloudbreak.rotation;

import static com.sequenceiq.cloudbreak.rotation.CloudbreakSecretRotationStep.CLUSTER_PROXY;
import static com.sequenceiq.cloudbreak.rotation.CloudbreakSecretRotationStep.CM_USER;
import static com.sequenceiq.cloudbreak.rotation.CloudbreakSecretRotationStep.SALT_PILLAR;
import static com.sequenceiq.cloudbreak.rotation.secret.step.CommonSecretRotationStep.REDBEAMS_ROTATE_POLLING;
import static com.sequenceiq.cloudbreak.rotation.secret.step.CommonSecretRotationStep.VAULT;

import java.util.List;

import com.sequenceiq.cloudbreak.rotation.secret.SecretType;
import com.sequenceiq.cloudbreak.rotation.secret.step.SecretRotationStep;

public enum CloudbreakSecretType implements SecretType {
    CLUSTER_CB_CM_ADMIN_PASSWORD(List.of(VAULT, CM_USER, CLUSTER_PROXY)),
    CLUSTER_MGMT_CM_ADMIN_PASSWORD(List.of(VAULT, CM_USER, CLUSTER_PROXY)),
    DATAHUB_EXTERNAL_DATABASE_ROOT_PASSWORD(List.of(REDBEAMS_ROTATE_POLLING, SALT_PILLAR)),
    DATALAKE_EXTERNAL_DATABASE_ROOT_PASSWORD(List.of(SALT_PILLAR));

    private final List<SecretRotationStep> steps;

    CloudbreakSecretType(List<SecretRotationStep> steps) {
        this.steps = steps;
    }

    @Override
    public List<SecretRotationStep> getSteps() {
        return steps;
    }
}
