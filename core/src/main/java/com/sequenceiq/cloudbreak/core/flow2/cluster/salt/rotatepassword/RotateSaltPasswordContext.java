package com.sequenceiq.cloudbreak.core.flow2.cluster.salt.rotatepassword;

import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.RotateSaltPasswordReason;
import com.sequenceiq.flow.core.CommonContext;
import com.sequenceiq.flow.core.FlowParameters;

public class RotateSaltPasswordContext extends CommonContext {

    private final Stack stack;

    private final RotateSaltPasswordReason reason;

    public RotateSaltPasswordContext(FlowParameters flowParameters, Stack stack, RotateSaltPasswordReason reason) {
        super(flowParameters);
        this.stack = stack;
        this.reason = reason;
    }

    public Stack getStack() {
        return stack;
    }

    public RotateSaltPasswordReason getReason() {
        return reason;
    }
}
