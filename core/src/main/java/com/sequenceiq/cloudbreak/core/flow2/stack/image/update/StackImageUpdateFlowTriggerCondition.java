package com.sequenceiq.cloudbreak.core.flow2.stack.image.update;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.view.StackView;
import com.sequenceiq.cloudbreak.service.stack.StackDtoService;
import com.sequenceiq.flow.core.FlowTriggerCondition;
import com.sequenceiq.flow.core.FlowTriggerConditionResult;

@Component
public class StackImageUpdateFlowTriggerCondition implements FlowTriggerCondition {
    private static final Logger LOGGER = LoggerFactory.getLogger(StackImageUpdateFlowTriggerCondition.class);

    @Inject
    private StackDtoService stackDtoService;

    @Override
    public FlowTriggerConditionResult isFlowTriggerable(Long stackId) {
        FlowTriggerConditionResult result = FlowTriggerConditionResult.OK;
        StackView stackView = stackDtoService.getStackViewById(stackId);
        boolean triggerable = stackView.isAvailable();
        if (!triggerable) {
            String msg = "Image update cannot be triggered because the cluster is not available.";
            LOGGER.debug(msg);
            result = new FlowTriggerConditionResult(msg);
        }
        return result;
    }
}
