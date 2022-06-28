package com.sequenceiq.cloudbreak.reactor.api.event.stack.loadbalancer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sequenceiq.cloudbreak.dto.StackDtoDelegate;
import com.sequenceiq.cloudbreak.reactor.api.event.StackEvent;

public class RestartCmForLbRequest extends StackEvent {

    private final StackDtoDelegate stack;

    @JsonCreator
    public RestartCmForLbRequest(
            @JsonProperty("stack") StackDtoDelegate stack) {
        super(stack.getId());
        this.stack = stack;
    }

    public StackDtoDelegate getStack() {
        return stack;
    }
}
