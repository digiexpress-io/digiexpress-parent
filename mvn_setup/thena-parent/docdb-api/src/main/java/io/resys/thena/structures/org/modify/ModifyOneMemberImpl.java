package io.resys.thena.structures.org.modify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import io.resys.thena.api.actions.ImmutableOneMemberEnvelope;
import io.resys.thena.api.actions.OrgCommitActions.ModType;
import io.resys.thena.api.actions.OrgCommitActions.ModifyOneMember;
import io.resys.thena.api.actions.OrgCommitActions.OneMemberEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.org.OrgActorStatus;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.org.OrgInserts.OrgBatchForOne;
import io.resys.thena.structures.org.OrgState;
import io.resys.thena.structures.org.modify.BatchForOneMemberModify.NoMemberChangesException;
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
  private Collection<String> partiesToAdd = new LinkedHashSet<>();
  private Collection<String> partiesToDisable = new LinkedHashSet<>();
  private Collection<String> partiesToRemove = new LinkedHashSet<>();
  private Collection<String> partiesToSet;
  
  private Collection<String> allRights = new LinkedHashSet<>();
  private Collection<String> rightsToRemove = new LinkedHashSet<>();
  private Collection<String> rightsToAdd = new LinkedHashSet<>();
  private Collection<String> rightsToDisable = new LinkedHashSet<>();
  private Collection<String> rightsToSet;
  
  private Map<String, List<String>> addUseGroupRoles = new HashMap<>();
  private Map<String, List<String>> removeUseGroupRoles = new HashMap<>();
  private OrgActorStatus.OrgActorStatusType newStatus;
  
  @Override public ModifyOneMemberImpl memberId(String userId) {
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
  @Override public ModifyOneMember status(OrgActorStatus.OrgActorStatusType status) {
    this.newStatus = RepoAssert.notNull(status, () -> "new status can't be null!");
    return this;
  }
  @Override public ModifyOneMember setAllParties(List<String> partyIdNameOrExtId) {
    this.partiesToSet = new LinkedHashSet<>(RepoAssert.notNull(partyIdNameOrExtId, () -> "setAllParties can't be null!"));
    this.partiesToAdd.clear();
    this.partiesToRemove.clear();
    this.partiesToDisable.clear();
    this.allParties.clear();
    this.allParties.addAll(partyIdNameOrExtId);
    return this;
  }
  @Override public ModifyOneMember setAllRights(List<String> rightIdNameOrExtId) {
    this.rightsToSet = new LinkedHashSet<>(RepoAssert.notNull(rightIdNameOrExtId, () -> "setAllRights can't be null!"));
    this.rightsToAdd.clear();
    this.rightsToRemove.clear();
    this.rightsToDisable.clear();
    this.allRights.clear();
    this.allRights.addAll(rightIdNameOrExtId);
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
      partiesToAdd.add(partyId);
    } else if(type == ModType.DISABLED) {
      partiesToDisable.add(partyId);
    } else if(type == ModType.REMOVE) {
      partiesToRemove.add(partyId);
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
      rightsToAdd.add(rightId);
    } else if(type == ModType.DISABLED) {
      rightsToDisable.add(rightId);
    } else if(type == ModType.REMOVE) {
      rightsToRemove.add(rightId);
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
    
    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(repoId).build();
    return this.state.withOrgTransaction(scope, this::doInTx);
  }
  
  
  private Uni<OneMemberEnvelope> doInTx(OrgState tx) {
		// parties
		final Uni<List<OrgParty>> partiesPromiseFromAddingRemovingDisabling = this.allParties.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) : 
			tx.query().parties().findAll(allParties).collect().asList();
		
    final Uni<List<OrgParty>> partiesPromiseExisting = tx.query().parties().findAllByMemberId(memberId).collect().asList();
    final Uni<List<OrgParty>> partyPromise =  Uni.combine().all().unis(
        partiesPromiseFromAddingRemovingDisabling,
        partiesPromiseExisting
    ).asTuple().onItem().transform(tuple -> 
      ImmutableList.<OrgParty>builder()
        .addAll(tuple.getItem1())
        .addAll(tuple.getItem2())
        .build());
	

		// memberships
    final Uni<List<OrgMembership>> membershipsPromise = tx.query().memberships().findAllByMemberId(memberId).collect().asList();
		
		// rights
		final Uni<List<OrgRight>> rightsPromiseFromAddingRemovingDisabling = this.allRights.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) :
			tx.query().rights().findAll(allRights).collect().asList();
		
    final Uni<List<OrgRight>> rightsPromiseExisting = tx.query().rights().findAllByMemberId(memberId).collect().asList();
    
    final Uni<List<OrgRight>> rightsPromise = Uni.combine().all().unis(
        rightsPromiseFromAddingRemovingDisabling,
        rightsPromiseExisting
    ).asTuple().onItem().transform(tuple -> 
      ImmutableList.<OrgRight>builder()
        .addAll(tuple.getItem1())
        .addAll(tuple.getItem2())
        .build()
    );
		
		
		// statuses
		final Uni<List<OrgActorStatus>> statusPromise = tx.query().actorStatus().findAllByMemberId(memberId).collect().asList();
		
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
		    } catch (NoMemberChangesException e) {
		      return Uni.createFrom().item(ImmutableOneMemberEnvelope.builder()
            .repoId(repoId)
            .addMessages(ImmutableMessage.builder()
                .exception(e).text("Nothing to commit, data already in the expected state!")
                .build())
            .status(CommitResultStatus.NO_CHANGES)
            .build());
		    }
		  });
  }
  
  private OneMemberEnvelope validateQueryResponse(
      OrgState tx, 
      OrgMember member, 
      List<OrgParty> parties, 
      List<OrgRight> rights) {
    
    if(member == null) {
      return ImmutableOneMemberEnvelope.builder()
          .repoId(repoId)
          .status(CommitResultStatus.ERROR)
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
          .status(CommitResultStatus.ERROR)
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
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find all roles: \r\n found: \r\n" + found + " \r\n but requested: \r\n" + expected + "!")
              .build())
          .build();
    }
    return null;
  }
  
  private Uni<OneMemberEnvelope> createResponse(
      OrgState tx, 
      OrgMember member, 
      List<OrgParty> allRelatedParties, 
      List<OrgRight> allRelatedRights,
      List<OrgActorStatus> status,
      List<OrgMembership> memberships,
      List<OrgMemberRight> memberRights) throws NoMemberChangesException {
    
    
    final var modify = new BatchForOneMemberModify(tx.getTenantId(), author, message)
      .newExternalId(externalId)
      .newEmail(email)
      .newUserName(userName)
      .newStatus(newStatus)
      .current(member)
      .currentStatus(status)
      .currentMemberships(memberships)
      .currentMemberRights(memberRights);

    appendParties(allRelatedParties, modify);
    appendRights(allRelatedRights, modify);
    setParties(allRelatedParties, memberships, modify);
    setRights(allRelatedRights, memberRights, modify);
    appendMemberRights(allRelatedParties, allRelatedRights, modify);

    
    final OrgBatchForOne batch = modify.create();
    return tx.insert().batchMany(batch)
        .onItem().transform(rsp -> {
          
          final var removedRights = rsp.getMemberRightsToDelete().stream().map(r -> r.getRightId()).toList();
          final var removedParties = rsp.getMembershipsToDelete().stream().map(r -> r.getPartyId()).toList();

          return ImmutableOneMemberEnvelope.builder()
              .repoId(repoId)
              .member(rsp.getMembersToUpdate().isEmpty() ? member : rsp.getMembersToUpdate().get(0))
              .addMessages(ImmutableMessage.builder().text(rsp.getLog()).build())
              .addAllMessages(rsp.getMessages())
              .status(BatchStatus.mapStatus(rsp.getStatus()))
              .directRights(allRelatedRights.stream()
                  .filter(right -> !removedRights.contains(right.getId()))
                  .toList()
              )
              .directParties(allRelatedParties.stream()
                  .filter(party -> !removedParties.contains(party.getId()))
                  .toList())
              .build();
        });
  }
  
  private void setParties(List<OrgParty> parties, List<OrgMembership> memberships, BatchForOneMemberModify modify) {
    if(this.partiesToSet == null) {
      return;
    }
    
    final var byId = parties.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
   
    // removal all that are not part pf the new set
    memberships.forEach(membership -> {
      final var party = byId.get(membership.getPartyId());
      
      // already exists nothing to do
      if(party.isMatch(this.partiesToSet)) {
        return;
      }
      modify.modifyMembership(ModType.REMOVE, party);
    });
    
    // add all new
    partiesToSet.stream().map(id -> parties.stream().filter(m -> m.isMatch(id)).findFirst().get()).forEach(party -> {
      modify.modifyMembership(ModType.ADD, party);      
    });
  }  
  
  private void setRights(List<OrgRight> rights, List<OrgMemberRight> memberRights, BatchForOneMemberModify modify) {
    if(this.rightsToSet == null) {
      return;
    }
    
    final var rightsById = rights.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
   
    // removal all that are not part pf the new set
    memberRights.forEach(memberRight -> {
      final var right = rightsById.get(memberRight.getRightId());
      
      // already exists nothing to do
      if(right.isMatch(this.rightsToSet)) {
        return;
      }
      modify.modifyMemberRights(ModType.REMOVE, right);
    });
    
    // add all new
    rightsToSet.stream().map(id -> rights.stream().filter(m -> m.isMatch(id)).findFirst().get()).forEach(right -> {
      modify.modifyMemberRights(ModType.ADD, right);      
    });
  }  
  
  
  private void appendMemberRights(List<OrgParty> parties, List<OrgRight> rights, BatchForOneMemberModify modify) {
    final Map<OrgParty, List<OrgRight>> modifyMemberRightsInParty = new HashMap<>();
    final Map<String, List<OrgRight>> addGroupsBy = new HashMap<>();
    final Map<String, String> addGroupMapping = new HashMap<>();
    
    parties.forEach(group -> {
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
      
      for(final var entry : this.addUseGroupRoles.entrySet()) {
        final var addRoleToUserGroup = entry.getValue().stream().filter(c -> role.isMatch(c)).findFirst().isPresent();
        if(addRoleToUserGroup) {
          addGroupsBy.get(addGroupMapping.get(entry.getKey())).add(role);
        }
      }
    });
    
    modify.modifyMemberRightsInParty(ModType.ADD, modifyMemberRightsInParty);
  }
  
  private void appendRights(List<OrgRight> rights, BatchForOneMemberModify modify) {
    // Remove or add roles 
    rights.forEach(role -> {
      
      if(role.isMatch(rightsToAdd)) {
        modify.modifyMemberRights(ModType.ADD, role);
      }
      if(role.isMatch(rightsToDisable)) {
        modify.modifyMemberRights(ModType.DISABLED, role);
      }
      if(role.isMatch(rightsToRemove)) {
        modify.modifyMemberRights(ModType.REMOVE, role);
      }
    });
  }
  private void appendParties(List<OrgParty> parties, BatchForOneMemberModify modify) {
    parties.forEach(group -> {
      if(group.isMatch(partiesToAdd)) {
        modify.modifyMembership(ModType.ADD, group);
      }
      if(group.isMatch(partiesToDisable)) {
         modify.modifyMembership(ModType.DISABLED, group);
      }
      if(group.isMatch(partiesToRemove)) {
        modify.modifyMembership(ModType.REMOVE, group);
      }
     
    });
  }
}
