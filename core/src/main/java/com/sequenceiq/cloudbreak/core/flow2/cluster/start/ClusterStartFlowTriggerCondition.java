package com.sequenceiq.cloudbreak.core.flow2.cluster.start;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.service.stack.StackDtoService;
import com.sequenceiq.cloudbreak.view.ClusterView;
import com.sequenceiq.cloudbreak.view.StackView;
import com.sequenceiq.flow.core.FlowTriggerCondition;
import com.sequenceiq.flow.core.FlowTriggerConditionResult;

@Component
public class ClusterStartFlowTriggerCondition implements FlowTriggerCondition {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterStartFlowTriggerCondition.class);

    @Inject
    private StackDtoService stackDtoService;

    @Override
    public FlowTriggerConditionResult isFlowTriggerable(Long stackId) {
        FlowTriggerConditionResult result = FlowTriggerConditionResult.OK;
        StackView stack = stackDtoService.getStackViewById(stackId);
        ClusterView clusterView = stackDtoService.getClusterViewByStackId(stackId);
        if (clusterView == null || !stack.isStartInProgress()) {
            String msg = String.format("Cluster start cannot be triggered, because cluster %s.",
                    clusterView == null ? "is null" : "not in startRequested status");
            LOGGER.info(msg);
            result = new FlowTriggerConditionResult(msg);
        }
        return result;
    }
}
