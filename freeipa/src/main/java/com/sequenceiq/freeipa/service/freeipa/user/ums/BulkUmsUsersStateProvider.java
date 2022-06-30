package com.sequenceiq.freeipa.service.freeipa.user.ums;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cloudera.thunderhead.service.usermanagement.UserManagementProto;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sequenceiq.cloudbreak.auth.altus.GrpcUmsClient;
import com.sequenceiq.cloudbreak.auth.crn.RegionAwareInternalCrnGeneratorFactory;
import com.sequenceiq.cloudbreak.logger.MDCUtils;
import com.sequenceiq.freeipa.service.freeipa.user.UserSyncConstants;
import com.sequenceiq.freeipa.service.freeipa.user.conversion.FmsUserConverter;
import com.sequenceiq.freeipa.service.freeipa.user.model.EnvironmentAccessRights;
import com.sequenceiq.freeipa.service.freeipa.user.model.FmsGroup;
import com.sequenceiq.freeipa.service.freeipa.user.model.UmsUsersState;
import com.sequenceiq.freeipa.service.freeipa.user.model.UserSyncOptions;
import com.sequenceiq.freeipa.service.freeipa.user.model.UsersState;
import com.sequenceiq.freeipa.service.freeipa.user.model.WorkloadCredential;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

@Component
public class BulkUmsUsersStateProvider extends BaseUmsUsersStateProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(BulkUmsUsersStateProvider.class);

    @Inject
    private GrpcUmsClient grpcUmsClient;

    @Inject
    private UmsRightsChecksFactory umsRightsChecksFactory;

    @Inject
    private FmsUserConverter fmsUserConverter;

    @Inject
    private RegionAwareInternalCrnGeneratorFactory regionAwareInternalCrnGeneratorFactory;

    @Inject
    private UmsCredentialProvider umsCredentialProvider;

    public Map<String, UmsUsersState> get(
            String accountId, Collection<String> environmentCrns,
            UserSyncOptions options) {
        List<String> environmentCrnList = List.copyOf(environmentCrns);
        UserManagementProto.GetUserSyncStateModelResponse userSyncStateModel = grpcUmsClient.getUserSyncStateModel(
                accountId,
                umsRightsChecksFactory.get(environmentCrnList),
                true,
                regionAwareInternalCrnGeneratorFactory);

        Map<String, FmsGroup> groups = convertGroupsToFmsGroups(userSyncStateModel.getGroupList());
        Map<UserManagementProto.WorkloadAdministrationGroup, FmsGroup> wags =
                convertWagsToFmsGroups(userSyncStateModel.getWorkloadAdministrationGroupList());
        List<String> requestedWorkloadUsernames = userSyncStateModel.getActorList().stream()
                .map(UserManagementProto.UserSyncActor::getActorDetails)
                .map(UserManagementProto.UserSyncActorDetails::getWorkloadUsername)
                .collect(Collectors.toList());

        Map<String, UmsUsersState> umsUsersStateMap = Maps.newHashMap();
        IntStream.range(0, environmentCrnList.size())
                .forEach(environmentIndex -> {
                    String environmentCrn = environmentCrnList.get(environmentIndex);
                    UmsUsersState.Builder umsUsersStateBuilder = UmsUsersState.newBuilder()
                            .setWorkloadAdministrationGroups(wags.values());
                    UsersState.Builder usersStateBuilder = UsersState.newBuilder();

                    addRequestedWorkloadUsernames(umsUsersStateBuilder, requestedWorkloadUsernames);
                    addGroupsToUsersStateBuilder(usersStateBuilder, groups.values());

                    ActorHandler actorHandler = ActorHandler.newBuilder()
                            .withFmsGroupConverter(getFmsGroupConverter())
                            .withUmsUsersStateBuilder(umsUsersStateBuilder)
                            .withUsersStateBuilder(usersStateBuilder)
                            .withCrnToFmsGroup(groups)
                            .withWagNamesForOtherEnvironments(Set.of())
                            .build();
                    addActorsToUmsUsersStateBuilder(
                            environmentIndex,
                            userSyncStateModel,
                            actorHandler);

                    addServicePrincipalsCloudIdentities(
                            umsUsersStateBuilder,
                            grpcUmsClient.listServicePrincipalCloudIdentities(
                                    accountId, environmentCrn));

                    UsersState usersState = usersStateBuilder.build();
                    umsUsersStateBuilder.setUsersState(usersState);

                    setLargeGroups(umsUsersStateBuilder, usersState, options);

                    umsUsersStateMap.put(environmentCrn, umsUsersStateBuilder.build());
                });
        return umsUsersStateMap;
    }

    private void addActorsToUmsUsersStateBuilder(
            int environmentIndex,
            UserManagementProto.GetUserSyncStateModelResponse userSyncStateModel,
            ActorHandler actorHandler) {


        // process actors - users and machine users are combined in the actor list
        userSyncStateModel.getActorList().forEach(actor -> {
            UserManagementProto.RightsCheckResult rightsCheckResult = actor.getRightsCheckResult(environmentIndex);
            EnvironmentAccessRights environmentAccessRights = new EnvironmentAccessRights(
                    rightsCheckResult.getHasRight(0),
                    rightsCheckResult.getHasRight(1));
            Supplier<Collection<String>> groupMembershipSupplier = () ->
                    actor.getGroupIndexList().stream()
                            .map(groupIndex ->
                                    userSyncStateModel.getGroupList().get(groupIndex).getCrn())
                            .collect(Collectors.toList());
            Supplier<Collection<String>> wagMembershipSupplier = () ->
                    actor.getWorkloadAdministrationGroupIndexList().stream()
                            .map(wagIndex ->
                                    userSyncStateModel.getWorkloadAdministrationGroupList()
                                            .get(wagIndex).getWorkloadAdministrationGroupName())
                            .collect(Collectors.toList());
            Supplier<WorkloadCredential> workloadCredentialSupplier = () ->
                    umsCredentialProvider.getCredentials(actor.getActorDetails().getCrn());

            try {
                actorHandler.handleActor(
                        environmentAccessRights,
                        fmsUserConverter.toFmsUser(actor.getActorDetails()),
                        actor.getActorDetails().getCrn(),
                        groupMembershipSupplier,
                        wagMembershipSupplier,
                        workloadCredentialSupplier,
                        actor.getActorDetails().getCloudIdentityList());
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                    LOGGER.warn("Member CRN {} not found in UMS. NOT_FOUND errors indicate that a user/machineUser " +
                                    "has been deleted after we have retrieved the list of users/machineUsers from " +
                                    "the UMS. Member will not be added to the UMS Users State. {}",
                            actor.getActorDetails().getCrn(), e.getLocalizedMessage());
                } else {
                    throw e;
                }
            }
        });
    }

    private List<String> getVirtualGroupNamesByActor(List<UserManagementProto.WorkloadAdministrationGroup> orderedRelatedWags,
            UserManagementProto.RightsCheckResult rightsCheckResult) {
        Map<UserManagementProto.WorkloadAdministrationGroup, Boolean> result = Maps.newHashMap();
        orderedRelatedWags.stream().forEach(wag -> {
            int wagIndex = orderedRelatedWags.indexOf(wag);
            result.put(wag, rightsCheckResult.getHasRight(wagIndex));
        });
        return result.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(entry -> entry.getKey().getWorkloadAdministrationGroupName())
                .collect(Collectors.toList());
    }

    private List<String> getGroupNamesByActor(List<UserManagementProto.Group> allGroups, List<UserManagementProto.Group> relatedGroups,
            UserManagementProto.UserSyncActor actor) {
        return actor.getGroupIndexList().stream()
                .map(groupIndex -> allGroups.get(groupIndex).getCrn())
                .filter(groupCrn -> relatedGroups.stream().map(UserManagementProto.Group::getCrn)
                        .collect(Collectors.toList()).contains(groupCrn))
                .collect(Collectors.toList());
    }

    private List<UserManagementProto.Group> getRelatedGroups(UserManagementProto.GetUserSyncStateModelResponse userSyncStateModel,
            List<String> resourceAssigneesCrns) {
        return userSyncStateModel.getGroupList().stream()
                .filter(group -> resourceAssigneesCrns.contains(group.getCrn()))
                .collect(Collectors.toList());
    }

    private List<UserManagementProto.UserSyncActor> getRelatedActors(UserManagementProto.GetUserSyncStateModelResponse userSyncStateModel,
            List<String> resourceAssigneesCrns) {
        return userSyncStateModel.getActorList().stream()
                .filter(userSyncActor -> resourceAssigneesCrns.contains(userSyncActor.getActorDetails().getCrn()) ||
                        userSyncActor.getGroupIndexList().stream()
                                .anyMatch(groupIndex -> resourceAssigneesCrns.contains(userSyncStateModel.getGroup(groupIndex).getCrn())))
                .filter(userSyncActor -> userSyncActor.getRightsCheckResultList().stream()
                        .anyMatch(rightsCheckResult -> rightsCheckResult.getHasRightList().stream().anyMatch(Boolean::booleanValue)))
                .collect(Collectors.toList());
    }

    private List<String> getResourceAssignees(String environmentCrn) {
        return grpcUmsClient.listAssigneesOfResource(environmentCrn, MDCUtils.getRequestId())
                .stream()
                .map(UserManagementProto.ResourceAssignee::getAssigneeCrn)
                .collect(Collectors.toList());
    }

    private List<UserManagementProto.WorkloadAdministrationGroup> getRelatedWagsOrderedByRightCheck(
            Map<UserManagementProto.WorkloadAdministrationGroup, FmsGroup> wags,
            String environmentCrn, UsersState.Builder usersStateBuilder) {
        List<UserManagementProto.WorkloadAdministrationGroup> orderedRelatedWags = Lists.newArrayList();
        List<UserManagementProto.WorkloadAdministrationGroup> relatedWags =
                addWagsToUsersStateBuilder(usersStateBuilder, wags, environmentCrn);
        UserSyncConstants.RIGHTS.stream().forEach(right -> {
            Optional<UserManagementProto.WorkloadAdministrationGroup> wagByRight = relatedWags.stream().filter(wag ->
                    StringUtils.equals(wag.getRightName(), right)).findFirst();
            if (wagByRight.isPresent()) {
                orderedRelatedWags.add(wagByRight.get());
            }
        });
        return orderedRelatedWags;
    }

}
