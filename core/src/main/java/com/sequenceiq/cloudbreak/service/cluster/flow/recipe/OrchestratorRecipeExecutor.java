package com.sequenceiq.cloudbreak.service.cluster.flow.recipe;

import static com.sequenceiq.cloudbreak.core.bootstrap.service.ClusterDeletionBasedExitCriteriaModel.clusterDeletionBasedModel;
import static com.sequenceiq.cloudbreak.event.ResourceEvent.CLUSTER_UPLOAD_RECIPES;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.api.client.util.Joiner;
import com.google.common.annotations.VisibleForTesting;
import com.sequenceiq.cloudbreak.api.endpoint.v4.common.Status;
import com.sequenceiq.cloudbreak.common.orchestration.Node;
import com.sequenceiq.cloudbreak.core.bootstrap.service.ClusterDeletionBasedExitCriteriaModel;
import com.sequenceiq.cloudbreak.domain.stack.cluster.host.HostGroup;
import com.sequenceiq.cloudbreak.domain.stack.instance.InstanceMetaData;
import com.sequenceiq.cloudbreak.dto.StackDto;
import com.sequenceiq.cloudbreak.orchestrator.exception.CloudbreakOrchestratorException;
import com.sequenceiq.cloudbreak.orchestrator.exception.CloudbreakOrchestratorFailedException;
import com.sequenceiq.cloudbreak.orchestrator.exception.CloudbreakOrchestratorTimeoutException;
import com.sequenceiq.cloudbreak.orchestrator.host.HostOrchestrator;
import com.sequenceiq.cloudbreak.orchestrator.model.GatewayConfig;
import com.sequenceiq.cloudbreak.orchestrator.model.RecipeModel;
import com.sequenceiq.cloudbreak.service.CloudbreakException;
import com.sequenceiq.cloudbreak.service.GatewayConfigService;
import com.sequenceiq.cloudbreak.service.cluster.flow.recipe.RecipeExecutionFailureCollector.RecipeFailure;
import com.sequenceiq.cloudbreak.service.stack.InstanceMetaDataService;
import com.sequenceiq.cloudbreak.structuredevent.event.CloudbreakEventService;
import com.sequenceiq.cloudbreak.util.StackUtil;
import com.sequenceiq.cloudbreak.view.StackView;

@Component
class OrchestratorRecipeExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecipeEngine.class);

    @Inject
    private HostOrchestrator hostOrchestrator;

    @Inject
    private GatewayConfigService gatewayConfigService;

    @Inject
    private CloudbreakEventService cloudbreakEventService;

    @Inject
    private StackUtil stackUtil;

    @Inject
    private InstanceMetaDataService instanceMetaDataService;

    @Inject
    private RecipeExecutionFailureCollector recipeExecutionFailureCollector;

    public void uploadRecipes(StackDto stackDto, Map<HostGroup, List<RecipeModel>> recipeModels) throws CloudbreakException {
        Map<String, List<RecipeModel>> hostnameToRecipeMap = recipeModels.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getName(), Entry::getValue));
        List<GatewayConfig> allGatewayConfigs = gatewayConfigService.getAllGatewayConfigs(stackDto);
        StackView stack = stackDto.getStack();
        recipesEvent(stack.getId(), stack.getStatus(), hostnameToRecipeMap);
        try {
            hostOrchestrator.uploadRecipes(allGatewayConfigs, hostnameToRecipeMap, clusterDeletionBasedModel(stack.getId(), stackDto.getCluster().getId()));
        } catch (CloudbreakOrchestratorFailedException e) {
            throw new CloudbreakException(e);
        }
    }

    public void preServiceDeploymentRecipes(StackDto stack) throws CloudbreakException {
        executePreServiceDeploymentRecipes(stack, () -> stackUtil.collectReachableNodes(stack));
    }

    public void preServiceDeploymentRecipesOnTargets(StackDto stack, Map<String, String> candidateAddresses) throws CloudbreakException {
        executePreServiceDeploymentRecipes(stack,
                () -> stackUtil.collectReachableAndUnreachableCandidateNodes(stack, candidateAddresses.keySet()).getReachableNodes());
    }

    public void postClusterManagerStartRecipes(StackDto stackDto) throws CloudbreakException {
        executePostClusterManagerStartRecipes(stackDto, () -> stackUtil.collectReachableNodes(stackDto));
    }

    public void postClusterManagerStartRecipesOnTargets(StackDto stack, Map<String, String> candidateAddresses) throws CloudbreakException {
        executePostClusterManagerStartRecipes(stack,
                () -> stackUtil.collectReachableAndUnreachableCandidateNodes(stack, candidateAddresses.keySet()).getReachableNodes());
    }

    public void postServiceDeploymentRecipes(StackDto stack) throws CloudbreakException {
        executePostServiceDeploymentRecipes(stack, () -> stackUtil.collectReachableNodes(stack));
    }

    public void postServiceDeploymentRecipesOnTargets(StackDto stack, Map<String, String> candidateAddresses) throws CloudbreakException {
        executePostServiceDeploymentRecipes(stack,
                () -> stackUtil.collectReachableAndUnreachableCandidateNodes(stack, candidateAddresses.keySet()).getReachableNodes());
    }

    public void preTerminationRecipes(StackDto stack, boolean forced) throws CloudbreakException {
        preTerminationRecipesOnNodes(stack, stackUtil.collectReachableNodes(stack), forced);
    }

    public void preTerminationRecipes(StackDto stack, Set<String> hostNames) throws CloudbreakException {
        preTerminationRecipesOnNodes(stack, stackUtil.collectNodes(stack, hostNames), false);
    }

    private String getRecipeTimeoutErrorMessage(CloudbreakOrchestratorTimeoutException timeoutException) {
        return " recipe(s) failed to finish in " + timeoutException.getTimeoutMinutes() +
                " minute(s), please check your recipe(s) and recipe logs on the machines under /var/log/recipes! Reason:" + timeoutException.getMessage();
    }

    private String getRecipeExecutionFailureMessage(StackView stack, CloudbreakOrchestratorException exception) {
        LOGGER.info("Getting execution failure message in stack {} for exception", stack.getId(), exception);
        if (!recipeExecutionFailureCollector.canProcessExecutionFailure(exception)) {
            return exception.getMessage();
        }
        List<RecipeFailure> failures = recipeExecutionFailureCollector.collectErrors(exception);
        Set<InstanceMetaData> instanceMetaData = instanceMetaDataService.getAllInstanceMetadataByStackId(stack.getId());

        String message = failures.stream()
                .map(failure -> getSingleRecipeExecutionFailureMessage(instanceMetaData, failure))
                .collect(Collectors.joining("\n ---------------------------------------------- \n"));
        return new StringBuilder("Failed to execute recipe(s): \n").append(message).toString();
    }

    private void executePostClusterManagerStartRecipes(StackDto stackDto, Supplier<Set<Node>> targetNodeFn) throws CloudbreakException {
        GatewayConfig gatewayConfig = gatewayConfigService.getPrimaryGatewayConfig(stackDto);
        StackView stack = stackDto.getStack();
        try {
            Set<Node> targetNodes = targetNodeFn.get();
            hostOrchestrator.postClusterManagerStartRecipes(gatewayConfig, targetNodes,
                    clusterDeletionBasedModel(stack.getId(), stack.getClusterId()));
        } catch (CloudbreakOrchestratorTimeoutException timeoutException) {
            String postClusterManagerStartException = "Post cluster manager start" + getRecipeTimeoutErrorMessage(timeoutException);
            LOGGER.info("{} {}", postClusterManagerStartException, timeoutException);
            throw new CloudbreakException(postClusterManagerStartException, timeoutException);
        } catch (CloudbreakOrchestratorFailedException e) {
            String message = getRecipeExecutionFailureMessage(stack, e);
            LOGGER.info(message);
            throw new CloudbreakException(message, e);
        }
    }

    private void executePostServiceDeploymentRecipes(StackDto stack, Supplier<Set<Node>> targetNodeFn) throws CloudbreakException {
        GatewayConfig gatewayConfig = gatewayConfigService.getPrimaryGatewayConfig(stack);
        try {
            Set<Node> targetNodes = targetNodeFn.get();
            hostOrchestrator.postServiceDeploymentRecipes(gatewayConfig, targetNodes, clusterDeletionBasedModel(stack.getId(), stack.getCluster().getId()));
        } catch (CloudbreakOrchestratorTimeoutException timeoutException) {
            String postInstallException = getRecipeTimeoutErrorMessage(timeoutException);
            LOGGER.info("{} {}", postInstallException, timeoutException);
            throw new CloudbreakException(postInstallException, timeoutException);
        } catch (CloudbreakOrchestratorFailedException e) {
            String message = getRecipeExecutionFailureMessage(stack.getStack(), e);
            LOGGER.info(message);
            throw new CloudbreakException(message, e);
        }
    }

    private void executePreServiceDeploymentRecipes(StackDto stack, Supplier<Set<Node>> targetNodeFn) throws CloudbreakException {
        GatewayConfig gatewayConfig = gatewayConfigService.getPrimaryGatewayConfig(stack);
        try {
            Set<Node> targetNodes = targetNodeFn.get();
            hostOrchestrator.preServiceDeploymentRecipes(gatewayConfig, targetNodes, clusterDeletionBasedModel(stack.getId(), stack.getCluster().getId()));
        } catch (CloudbreakOrchestratorTimeoutException timeoutException) {
            String preServiceDeploymentRecipeException = "Pre service deployment" + getRecipeTimeoutErrorMessage(timeoutException);
            LOGGER.info("{} {}", preServiceDeploymentRecipeException, timeoutException);
            throw new CloudbreakException(preServiceDeploymentRecipeException, timeoutException);
        } catch (CloudbreakOrchestratorFailedException e) {
            String message = getRecipeExecutionFailureMessage(stack.getStack(), e);
            LOGGER.info(message);
            throw new CloudbreakException(message);
        }
    }

    private void preTerminationRecipesOnNodes(StackDto stack, Set<Node> nodes, boolean forced) throws CloudbreakException {
        GatewayConfig gatewayConfig = gatewayConfigService.getPrimaryGatewayConfig(stack);
        try {
            hostOrchestrator.preTerminationRecipes(gatewayConfig, nodes, ClusterDeletionBasedExitCriteriaModel.nonCancellableModel(), forced);
        } catch (CloudbreakOrchestratorTimeoutException timeoutException) {
            String preTerminationException = "Pre-termination" + getRecipeTimeoutErrorMessage(timeoutException);
            LOGGER.info("{} {}", preTerminationException, timeoutException);
            throw new CloudbreakException(preTerminationException, timeoutException);
        } catch (CloudbreakOrchestratorFailedException e) {
            String message = getRecipeExecutionFailureMessage(stack.getStack(), e);
            LOGGER.info(message);
            throw new CloudbreakException(message, e);
        }
    }

    @VisibleForTesting
    String getSingleRecipeExecutionFailureMessage(Set<InstanceMetaData> instanceMetaData, RecipeFailure failure) {
        String host = recipeExecutionFailureCollector.getInstanceMetadataByHost(instanceMetaData, failure.getHost())
                .map(metadata -> new StringBuilder("Hostgroup: '")
                        .append(metadata.getInstanceGroup().getGroupName())
                        .append("' - \n")
                        .append("Instance: '")
                        .append(metadata.getDiscoveryFQDN())
                        .append("'")
                        .toString())
                .orElse(new StringBuilder("Instance: '")
                        .append(failure.getHost())
                        .append("' (missing metadata)")
                        .toString());
        return new StringBuilder("[Recipe: '")
                .append(failure.getRecipeName())
                .append("' - \n")
                .append(host)
                .append(']')
                .toString();
    }

    private void recipesEvent(Long stackId, Status status, Map<String, List<RecipeModel>> recipeMap) {
        List<String> recipes = new ArrayList<>();
        for (Entry<String, List<RecipeModel>> entry : recipeMap.entrySet()) {
            Collection<String> recipeNamesPerHostgroup = new ArrayList<>(entry.getValue().size());
            for (RecipeModel rm : entry.getValue()) {
                recipeNamesPerHostgroup.add(rm.getName());
            }
            if (!recipeNamesPerHostgroup.isEmpty()) {
                String recipeNamesStr = Joiner.on(',').join(recipeNamesPerHostgroup);
                recipes.add(String.format("%s:[%s]", entry.getKey(), recipeNamesStr));
            }
        }

        if (!recipes.isEmpty()) {
            Collections.sort(recipes);
            String messageStr = Joiner.on(';').join(recipes);
            cloudbreakEventService.fireCloudbreakEvent(stackId, status.name(), CLUSTER_UPLOAD_RECIPES, Collections.singletonList(messageStr));
        }
    }
}
