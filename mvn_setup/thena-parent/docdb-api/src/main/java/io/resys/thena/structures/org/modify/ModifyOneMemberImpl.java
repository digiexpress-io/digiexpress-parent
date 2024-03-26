package io.resys.thena.structures.org.modify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.resys.thena.api.actions.ImmutableOneMemberEnvelope;
import io.resys.thena.api.actions.OrgCommitActions.ModType;
import io.resys.thena.api.actions.OrgCommitActions.ModifyOneMember;
import io.resys.thena.api.actions.OrgCommitActions.OneMemberEnvelope;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMember;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMemberRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMembership;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgParty;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgRight;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.spi.DataMapper;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.org.OrgInserts.OrgBatchForOne;
import io.resys.thena.structures.org.OrgState.OrgRepo;
import io.resys.thena.structures.org.modify.BatchForOneMemberModify.NoChangesException;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ModifyOneMemberImpl implements ModifyOneMember {

  private final DbState state;
  private final String repoId;
  
  private String author;
  private String message;

  private String memberId;
  private Optional<String> userName;
  private Optional<String> email;
  private Optional<String> externalId;

  private Collection<String> allParties = new LinkedHashSet<>();
  private Collection<String> groupsToAdd = new LinkedHashSet<>();
  private Collection<String> groupsToRemove = new LinkedHashSet<>();
  private Collection<String> allRights = new LinkedHashSet<>();
  private Collection<String> rolesToAdd = new LinkedHashSet<>();
  private Collection<String> rolesToRemove = new LinkedHashSet<>();
  private Map<String, List<String>> addUseGroupRoles = new HashMap<>();
  private Map<String, List<String>> removeUseGroupRoles = new HashMap<>();
  private OrgActorStatusType newStatus;
  
  @Override public ModifyOneMemberImpl userId(String userId) {
    this.memberId = RepoAssert.notEmpty(userId, () -> "userId can't be empty!"); 
    return this; 
  }
  @Override public ModifyOneMemberImpl author(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this; 
  }
  @Override public ModifyOneMemberImpl message(String message) {
    this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!"); 
    return this; 
  }
  @Override public ModifyOneMemberImpl userName(String userName) {
    this.userName = Optional.ofNullable(RepoAssert.notEmpty(userName, () -> "userName can't be empty!")); 
    return this; 
  }
  @Override public ModifyOneMemberImpl email(String email) {
    this.email = Optional.ofNullable(RepoAssert.notEmpty(email, () -> "email can't be empty!")); 
    return this; 
  }
  @Override public ModifyOneMemberImpl externalId(String externalId) { 
    this.externalId = Optional.ofNullable(externalId); 
    return this; 
  }
  @Override public ModifyOneMember status(OrgActorStatusType status) {
    this.newStatus = RepoAssert.notNull(status, () -> "new status can't be null!");
    return this;
  }
  @Override
  public ModifyOneMember modifyPartyRight(ModType type, String partyIdNameOrExtId, String rightIdNameOrExtId) {
    RepoAssert.notEmpty(partyIdNameOrExtId, () -> "partyIdNameOrExtId can't be empty!");
    RepoAssert.notEmpty(rightIdNameOrExtId, () -> "rightIdNameOrExtId can't be empty!");

    this.allParties.add(partyIdNameOrExtId);
    this.allRights.add(rightIdNameOrExtId);
    if(type == ModType.ADD) {
      if(!this.addUseGroupRoles.containsKey(partyIdNameOrExtId)) {
        this.addUseGroupRoles.put(partyIdNameOrExtId, new ArrayList<>());
      }
      this.addUseGroupRoles.get(partyIdNameOrExtId).add(rightIdNameOrExtId);
    } else if(type == ModType.DISABLED) {
      if(!this.removeUseGroupRoles.containsKey(partyIdNameOrExtId)) {
        this.removeUseGroupRoles.put(partyIdNameOrExtId, new ArrayList<>());
      }
      this.removeUseGroupRoles.get(partyIdNameOrExtId).add(rightIdNameOrExtId);
    } else {
      RepoAssert.fail("Unknown modification type: " + type + "!");
    }
    return this; 
  }
  @Override 
  public ModifyOneMemberImpl modifyParties(ModType type, String partyId) { 
    RepoAssert.notEmpty(partyId, () -> "partyId can't be empty!");
    this.allParties.add(partyId);
    if(type == ModType.ADD) {
      groupsToAdd.add(partyId);
    } else if(type == ModType.DISABLED) {
      groupsToRemove.add(partyId);
    } else {
      RepoAssert.fail("Unknown modification type: " + type + "!");
    }
    return this; 
   }
  @Override 
  public ModifyOneMemberImpl modifyRights(ModType type, String rightId) {
    RepoAssert.notEmpty(rightId, () -> "rightId can't be empty!");
    
    this.allRights.add(rightId);
    if(type == ModType.ADD) {
      rolesToAdd.add(rightId);
    } else if(type == ModType.DISABLED) {
      rolesToRemove.add(rightId);
    } else {
      RepoAssert.fail("Unknown modification type: " + type + "!");
    }
    return this; 
  }

  @Override
  public Uni<OneMemberEnvelope> build() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(memberId, () -> "userId can't be empty!");
    
    RepoAssert.notEmptyIfDefined(userName, () -> "userName can't be empty!");
    RepoAssert.notEmptyIfDefined(email, () -> "email can't be empty!");

    return this.state.toOrgState().withTransaction(repoId, this::doInTx);
  }
  
  
  private Uni<OneMemberEnvelope> doInTx(OrgRepo tx) {
		// parties
		final Uni<List<OrgParty>> partyPromise = this.allParties.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) : 
			tx.query().parties().findAll(allParties).collect().asList();

		// memberships
    final Uni<List<OrgMembership>> membershipsPromise = tx.query().memberships().findAllByMemberId(memberId).collect().asList();
		
		// roles
		final Uni<List<OrgRight>> rightsPromise = this.allRights.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) :
			tx.query().rights().findAll(allRights).collect().asList();
		
		// statuses
		final Uni<List<OrgActorStatus>> statusPromise = tx.query().actorStatus().findAllByIdMemberId(memberId).collect().asList();
		
		// member data
		final Uni<OrgMember> memberPromise = tx.query().members().getById(memberId);

    final Uni<List<OrgMemberRight>> memberRightsPromise = tx.query().memberRights().findAllByMemberId(memberId).collect().asList();
		
		
		// join data
		return Uni.combine().all().unis(memberPromise, partyPromise, rightsPromise, statusPromise, membershipsPromise, memberRightsPromise).asTuple()
		  .onItem().transformToUni(tuple -> {
		    final var validated = validateQueryResponse(tx, tuple.getItem1(), tuple.getItem2(), tuple.getItem3());
		    if(validated != null) {
		      return Uni.createFrom().item(validated);
		    }
		    
		    try {
		      return createResponse(tx, tuple.getItem1(), tuple.getItem2(), tuple.getItem3(), tuple.getItem4(), tuple.getItem5(), tuple.getItem6());
		    } catch (NoChangesException e) {
		      return Uni.createFrom().item(ImmutableOneMemberEnvelope.builder()
            .repoId(repoId)
            .addMessages(ImmutableMessage.builder()
                .exception(e).text("Nothing to commit, data already in the expected state!")
                .build())
            .status(Tenant.CommitResultStatus.NO_CHANGES)
            .build());
		    }
		  });
  }
  
  private OneMemberEnvelope validateQueryResponse(
      OrgRepo tx, 
      OrgMember member, 
      List<OrgParty> parties, 
      List<OrgRight> rights) {
    
    if(member == null) {
      return ImmutableOneMemberEnvelope.builder()
          .repoId(repoId)
          .status(Tenant.CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find member by id/name/externalid: '" + memberId  +"'!")
              .build())
          .build();
    }
    
    if(parties.size() < this.allParties.size()) {
      final var found = String.join(", ", parties.stream().map(e -> e.getPartyName()).toList());
      final var expected = String.join(", ", this.allParties);
      return ImmutableOneMemberEnvelope.builder()
          .repoId(repoId)
          .status(Tenant.CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find all groups: \r\n found: \r\n" + found + " \r\n but requested: \r\n" + expected + "!")
              .build())
          .build();
    }
    
    if(rights.size() < this.allRights.size()) {
      final var found = String.join(", ", rights.stream().map(e -> e.getRightName()).toList());
      final var expected = String.join(", ", this.allRights);
      return ImmutableOneMemberEnvelope.builder()
          .repoId(repoId)
          .status(Tenant.CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find all roles: \r\n found: \r\n" + found + " \r\n but requested: \r\n" + expected + "!")
              .build())
          .build();
    }
    return null;
  }
  
  private Uni<OneMemberEnvelope> createResponse(
      OrgRepo tx, 
      OrgMember member, 
      List<OrgParty> parties, 
      List<OrgRight> rights,
      List<OrgActorStatus> status,
      List<OrgMembership> memberships,
      List<OrgMemberRight> memberRights) throws NoChangesException {
    
    final Map<OrgParty, List<OrgRight>> modifyMemberRightsInParty = new HashMap<>();
    final Map<String, List<OrgRight>> addGroupsBy = new HashMap<>();
    final Map<String, String> addGroupMapping = new HashMap<>();
    
    final var modify = new BatchForOneMemberModify(tx.getRepo().getId(), author, message)
      .newExternalId(externalId)
      .newEmail(email)
      .newUserName(userName)
      .newStatus(newStatus)
      .current(member)
      .currentStatus(status)
      .currentMemberships(memberships)
      .currentMemberRights(memberRights);

    // Remove or add groups 
    parties.forEach(group -> {
      if(group.isMatch(groupsToAdd)) {
        modify.modifyMembership(ModType.ADD, group);
      }
      if(group.isMatch(groupsToRemove)) {
         modify.modifyMembership(ModType.DISABLED, group);
      }
      final var addToGroupRole = this.addUseGroupRoles.keySet().stream().filter(c -> group.isMatch(c)).findFirst();
      if(addToGroupRole.isPresent()) {
        final var roles = new ArrayList<OrgRight>();
        addGroupsBy.put(group.getId(), roles);
        modifyMemberRightsInParty.put(group, roles);
        addGroupMapping.put(addToGroupRole.get(), group.getId());
      }
    });
    
    // Remove or add roles 
    rights.forEach(role -> {
      
      if(role.isMatch(rolesToAdd)) {
        modify.modifyMemberRights(ModType.ADD, role);
      }
      if(role.isMatch(rolesToRemove)) {
        modify.modifyMemberRights(ModType.DISABLED, role);
      }
      
      for(final var entry : this.addUseGroupRoles.entrySet()) {
        final var addRoleToUserGroup = entry.getValue().stream().filter(c -> role.isMatch(c)).findFirst().isPresent();
        if(addRoleToUserGroup) {
          addGroupsBy.get(addGroupMapping.get(entry.getKey())).add(role);
        }
      }
    });
    
    final OrgBatchForOne batch = modify
        .modifyMemberRightsInParty(ModType.ADD, modifyMemberRightsInParty)
        .create();
    return tx.insert().batchMany(batch)
        .onItem().transform(rsp -> ImmutableOneMemberEnvelope.builder()
          .repoId(repoId)
          .member(rsp.getMembers().isEmpty() ? null : rsp.getMembers().get(0))
          .addMessages(rsp.getLog())
          .addAllMessages(rsp.getMessages())
          .status(DataMapper.mapStatus(rsp.getStatus()))
          .build());
  }
}
