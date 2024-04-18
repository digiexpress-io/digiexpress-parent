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

import io.resys.thena.api.actions.ImmutableOnePartyEnvelope;
import io.resys.thena.api.actions.OrgCommitActions.ModType;
import io.resys.thena.api.actions.OrgCommitActions.ModifyOneParty;
import io.resys.thena.api.actions.OrgCommitActions.OnePartyEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.org.OrgActorStatus;
import io.resys.thena.api.entities.org.OrgActorStatus.OrgActorStatusType;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgDocSubType;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.org.OrgInserts.OrgBatchForOne;
import io.resys.thena.structures.org.OrgState;
import io.resys.thena.structures.org.modify.BatchForOnePartyModify.NoPartyChangesException;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ModifyOnePartyImpl implements ModifyOneParty {

  private final DbState state;
  private final String repoId;
  private String author;
  private String message;

  private String partyId;
  private Optional<String> partyName;
  private Optional<String> partyDescription;
  private Optional<String> parentPartyId;
  private Optional<String> externalId;
  private Optional<OrgDocSubType> partySubType;

  private Collection<String> allMembers = new LinkedHashSet<>();
  private Collection<String> membersToSet;
  private Collection<String> membersToAdd = new LinkedHashSet<>();
  private Collection<String> membersToDisable = new LinkedHashSet<>();
  private Collection<String> membersToRemove = new LinkedHashSet<>();
  
  private Collection<String> allRights = new LinkedHashSet<>();
  private Collection<String> rightsToRemove = new LinkedHashSet<>();
  private Collection<String> rightsToAdd = new LinkedHashSet<>();
  private Collection<String> rightsToDisable = new LinkedHashSet<>();
  private Collection<String> rightsToSet;
  private Map<String, List<String>> addMemberWithRights = new HashMap<>();
  private Map<String, List<String>> disableMemberWithRights = new HashMap<>();
  private OrgActorStatusType newStatus;
  
  @Override public ModifyOnePartyImpl partyId(String partyId) {
    this.partyId = RepoAssert.notEmpty(partyId, () -> "partyId can't be empty!"); 
    return this; 
  }
  @Override public ModifyOnePartyImpl author(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this; 
  }
  @Override public ModifyOnePartyImpl message(String message) {
    this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!"); 
    return this; 
  }
  @Override public ModifyOnePartyImpl partyName(String partyName) {
    this.partyName = Optional.ofNullable(RepoAssert.notEmpty(partyName, () -> "partyName can't be empty!")); 
    return this; 
  }
  @Override public ModifyOnePartyImpl partyDescription(String partyDescription) {
    this.partyDescription = Optional.ofNullable(RepoAssert.notEmpty(partyDescription, () -> "partyDescription can't be empty!")); 
    return this; 
  }
  @Override public ModifyOnePartyImpl parentId(String parentId) {
    this.parentPartyId = Optional.ofNullable(RepoAssert.notBlank(parentId, () -> "parentId can't be empty!")); 
    return this; 
  }
  @Override public ModifyOnePartyImpl partySubType(OrgDocSubType partySubType) {
    this.partySubType = Optional.ofNullable(RepoAssert.notNull(partySubType, () -> "partySubType can't be null!")); 
    return this; 
  }
  @Override public ModifyOnePartyImpl externalId(String externalId) { 
    this.externalId = Optional.ofNullable(externalId); 
    return this; 
  }
  @Override public ModifyOnePartyImpl status(OrgActorStatusType status) {
    this.newStatus = RepoAssert.notNull(status, () -> "new status can't be null!");
    return this;
  }
  @Override public ModifyOneParty setAllMembers(List<String> memberIdNameOrExtId) {
    this.membersToSet = new LinkedHashSet<>(RepoAssert.notNull(memberIdNameOrExtId, () -> "setAllMembers can't be null!"));
    this.membersToAdd.clear();
    this.membersToRemove.clear();
    this.membersToDisable.clear();
    this.allMembers.clear();
    this.allMembers.addAll(memberIdNameOrExtId);
    return this;
  }
  @Override public ModifyOneParty setAllRights(List<String> rightIdNameOrExtId) {
    this.rightsToSet = new LinkedHashSet<>(RepoAssert.notNull(rightIdNameOrExtId, () -> "setAllRights can't be null!"));
    this.rightsToAdd.clear();
    this.rightsToRemove.clear();
    this.rightsToDisable.clear();
    this.allRights.clear();
    this.allRights.addAll(rightIdNameOrExtId);
    return this;
  }
  @Override 
  public ModifyOnePartyImpl modifyMember(ModType type, String memberId) { 
    RepoAssert.notEmpty(memberId, () -> "memberId can't be empty!");
    this.allMembers.add(memberId);
    if(type == ModType.ADD) {
      membersToAdd.add(memberId);
    } else if(type == ModType.DISABLED) {
      membersToDisable.add(memberId);
    } else if(type == ModType.REMOVE) {
      membersToRemove.add(memberId);
    } else {
      RepoAssert.fail("Unknown modification type: " + type + "!");
    }
    return this; 
   }
  @Override 
  public ModifyOnePartyImpl modifyRight(ModType type, String rightId) {
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
  public ModifyOnePartyImpl modifyMemberRight(ModType type, String memberIdNameOrExtId, String rightIdNameOrExtId) {
    RepoAssert.notEmpty(memberIdNameOrExtId, () -> "memberIdNameOrExtId can't be empty!");
    RepoAssert.notEmpty(rightIdNameOrExtId, () -> "rightIdNameOrExtId can't be empty!");

    this.allMembers.add(memberIdNameOrExtId);
    this.allRights.add(rightIdNameOrExtId);
    if(type == ModType.ADD) {
      if(!this.addMemberWithRights.containsKey(memberIdNameOrExtId)) {
        this.addMemberWithRights.put(memberIdNameOrExtId, new ArrayList<>());
      }
      this.addMemberWithRights.get(memberIdNameOrExtId).add(rightIdNameOrExtId);
    } else if(type == ModType.DISABLED) {
      if(!this.disableMemberWithRights.containsKey(memberIdNameOrExtId)) {
        this.disableMemberWithRights.put(memberIdNameOrExtId, new ArrayList<>());
      }
      this.disableMemberWithRights.get(memberIdNameOrExtId).add(rightIdNameOrExtId);
    } else {
      RepoAssert.fail("Unknown modification type: " + type + "!");
    }
    return this; 
  }
  @Override
  public Uni<OnePartyEnvelope> build() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(partyId, () -> "partyId can't be empty!");
    
    RepoAssert.notEmptyIfDefined(partyName, () -> "partyName can't be empty!");
    RepoAssert.notEmptyIfDefined(partyDescription, () -> "partyDescription can't be empty!");
    RepoAssert.notNullIfDefined(partySubType, () -> "partySubType can't be empty!");
    RepoAssert.notEmptyIfDefined(externalId, () -> "externalId can't be empty!");

    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(repoId).build();
    return this.state.withOrgTransaction(scope, this::doInTx);
  }
  
  
  private Uni<OnePartyEnvelope> doInTx(OrgState tx) {
		// members
    final Uni<List<OrgMember>> memberPromiseFromAddingRemovingDisabling = this.allMembers.isEmpty() ? 
        Uni.createFrom().item(Collections.emptyList()) : 
        tx.query().members().findAll(allMembers).collect().asList();
    
    final Uni<List<OrgMember>> memebrsPromiseExisting = tx.query().members().findAllByPartyId(partyId).collect().asList();
		final Uni<List<OrgMember>> memberPromise =  Uni.combine().all().unis(
		    memberPromiseFromAddingRemovingDisabling,
        memebrsPromiseExisting
    ).asTuple().onItem().transform(tuple -> 
      ImmutableList.<OrgMember>builder()
        .addAll(tuple.getItem1())
        .addAll(tuple.getItem2())
        .build()
    );

		// memberships
    final Uni<List<OrgMembership>> membershipsPromise = tx.query().memberships().findAllByPartyId(partyId).collect().asList();
		
		// statuses
		final Uni<List<OrgActorStatus>> statusPromise = tx.query().actorStatus().findAllByPartyId(partyId).collect().asList();
		
		// party
		final Uni<OrgParty> partyPromise = tx.query().parties().getById(partyId);

    // member data
    final Uni<Optional<OrgParty>> parentPartyPromise;
    if(parentPartyId == null) {
      parentPartyPromise = Uni.createFrom().nullItem();
    } else if(parentPartyId.isEmpty()) {
      parentPartyPromise = Uni.createFrom().item(Optional.empty());
    } else {
      parentPartyPromise = tx.query().parties().getById(parentPartyId.get()).onItem().transform(party -> Optional.ofNullable(party));  
    }
    
		// rights
    final Uni<List<OrgPartyRight>> partyRightsPromise = tx.query().partyRights().findAllByPartyId(partyId).collect().asList();
    final Uni<List<OrgMemberRight>> memberRightsPromise = tx.query().memberRights().findAllByPartyId(partyId).collect().asList();
    
    final Uni<List<OrgRight>> rightsPromiseFromAddingRemovingDisabling = this.allRights.isEmpty() ? 
            Uni.createFrom().item(Collections.emptyList()) :
            tx.query().rights().findAll(allRights).collect().asList();
    final Uni<List<OrgRight>> rightsPromiseExisting = tx.query().rights().findAllByPartyId(partyId).collect().asList();
    
    final Uni<List<OrgRight>> rightsPromise = Uni.combine().all().unis(
        rightsPromiseFromAddingRemovingDisabling,
        rightsPromiseExisting
    ).asTuple().onItem().transform(tuple -> 
      ImmutableList.<OrgRight>builder()
        .addAll(tuple.getItem1())
        .addAll(tuple.getItem2())
        .build()
    );
      
		// join data
		return Uni.combine().all().unis(memberPromise, partyPromise, rightsPromise, statusPromise, membershipsPromise, partyRightsPromise, memberRightsPromise, parentPartyPromise).asTuple()
		  .onItem().transformToUni(tuple -> {
		    final var validated = validateQueryResponse(tx, tuple.getItem1(), tuple.getItem2(), tuple.getItem3(), tuple.getItem8());
		    if(validated != null) {
		      return Uni.createFrom().item(validated);
		    }
		    
		    try {
		      return createResponse(tx, tuple.getItem1(), tuple.getItem2(), tuple.getItem3(), tuple.getItem4(), tuple.getItem5(), tuple.getItem6(), tuple.getItem7(), tuple.getItem8());
		    } catch (NoPartyChangesException e) {
		      return Uni.createFrom().item(ImmutableOnePartyEnvelope.builder()
            .repoId(repoId)
            .addMessages(ImmutableMessage.builder()
                .exception(e).text("Nothing to commit, data already in the expected state!")
                .build())
            .status(CommitResultStatus.NO_CHANGES)
            .build());
		    }
		  });
  }
  
  private OnePartyEnvelope validateQueryResponse(
      OrgState tx, 
      List<OrgMember> members, 
      OrgParty party, 
      List<OrgRight> rights,
      Optional<OrgParty> parent) {
    
    if(party == null) {
      return ImmutableOnePartyEnvelope.builder()
          .repoId(repoId)
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find party by id/name/externalid: '" + partyId  +"'!")
              .build())
          .build();
    }
    if(parentPartyId != null && parentPartyId.isPresent() && parent.isEmpty() ) {
      return ImmutableOnePartyEnvelope.builder()
          .repoId(repoId)
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find parent party by id/name/externalid: '" + parentPartyId.get()  +"'!")
              .build())
          .build();
    }
    if(parent != null && parent.isPresent() && parent.get().getId().equals(party.getId()) ) {
      return ImmutableOnePartyEnvelope.builder()
          .repoId(repoId)
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Can't put parent id to self: '" + party.getId()  +"'!")
              .build())
          .build();
    }
    
    if(members.size() < this.allMembers.size()) {
      final var found = String.join(", ", members.stream().map(e -> e.getUserName()).toList());
      final var expected = String.join(", ", this.allMembers);
      return ImmutableOnePartyEnvelope.builder()
          .repoId(repoId)
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find all members: \r\n found: \r\n" + found + " \r\n but requested: \r\n" + expected + "!")
              .build())
          .build();
    }
    
    if(rights.size() < this.allRights.size()) {
      final var found = String.join(", ", rights.stream().map(e -> e.getRightName()).toList());
      final var expected = String.join(", ", this.allRights);
      return ImmutableOnePartyEnvelope.builder()
          .repoId(repoId)
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find all rights: \r\n found: \r\n" + found + " \r\n but requested: \r\n" + expected + "!")
              .build())
          .build();
    }
    return null;
  }
  
  private Uni<OnePartyEnvelope> createResponse(
      OrgState tx, 
      List<OrgMember> allRelatedMembers, 
      OrgParty party, 
      List<OrgRight> allRelatedRights,
      List<OrgActorStatus> status,
      List<OrgMembership> memberships,
      List<OrgPartyRight> partyRights,
      List<OrgMemberRight> memberRights,
      Optional<OrgParty> parent) 
  throws NoPartyChangesException {

    final var modify = new BatchForOnePartyModify(tx.getTenantId(), author, message)
      .newExternalId(externalId)
      .newPartyName(partyName)
      .newPartyDesc(partyDescription)
      .newPartySubType(partySubType)
      .newParentPartyId(parent)
      .newStatus(newStatus)
      .current(party)
      .currentStatus(status)
      .currentMemberships(memberships)
      .currentPartyRights(partyRights)
      .currentMemberRights(memberRights);

    appendMembers(allRelatedMembers, modify);
    appendRights(allRelatedRights, modify);
    appendMemberAndRight(allRelatedRights, allRelatedMembers, modify);
    setMembers(allRelatedMembers, memberships, modify);
    setRights(allRelatedRights, partyRights, modify);
    
    
    final OrgBatchForOne batch = modify.create();
    return tx.insert().batchMany(batch)
        .onItem().transform(rsp -> {
          
          final var removedRights = rsp.getPartyRightToDelete().stream().map(r -> r.getRightId()).toList();
          final var removedMembers = rsp.getMembershipsToDelete().stream().map(r -> r.getMemberId()).toList();

          return ImmutableOnePartyEnvelope.builder()
          .repoId(repoId)
          .party(rsp.getPartiesToUpdate().isEmpty() ? party : rsp.getPartiesToUpdate().get(0))
          .directMembers(allRelatedMembers.stream()
            .filter(member -> !removedMembers.contains(member.getId()))
            .toList())
          .directRights(allRelatedRights.stream()
            .filter(right -> !removedRights.contains(right.getId()))
            .toList()
          )
          .addMessages(ImmutableMessage.builder().text(rsp.getLog()).build())
          .addAllMessages(rsp.getMessages())
          .status(BatchStatus.mapStatus(rsp.getStatus()))
          .build();
        });
  }
  
  private void appendMemberAndRight(List<OrgRight> rights, List<OrgMember> members, BatchForOnePartyModify modify) {
    final Map<OrgMember, List<OrgRight>> modifyMemberRightsInParty = new HashMap<>();
    final Map<String, List<OrgRight>> addMembersWithRights = new HashMap<>();
    final Map<String, String> addGroupMapping = new HashMap<>();
    
    members.forEach(member -> {
      final var addToGroupRole = this.addMemberWithRights.keySet().stream().filter(c -> member.isMatch(c)).findFirst();
      if(addToGroupRole.isPresent()) {
        final var roles = new ArrayList<OrgRight>();
        addMembersWithRights.put(member.getId(), roles);
        modifyMemberRightsInParty.put(member, roles);
        addGroupMapping.put(addToGroupRole.get(), member.getId());
      }
    });
    
    rights.forEach(role -> {
      for(final var entry : this.addMemberWithRights.entrySet()) {
        final var addRoleToUserGroup = entry.getValue().stream().filter(c -> role.isMatch(c)).findFirst().isPresent();
        if(addRoleToUserGroup) {
          addMembersWithRights.get(addGroupMapping.get(entry.getKey())).add(role);
        }
      }
    });
    modify.modifyMemberRightsInParty(ModType.ADD, modifyMemberRightsInParty);  
  }
  
  private void appendRights(List<OrgRight> rights, BatchForOnePartyModify modify) {

   rights.forEach(right -> {
      if(right.isMatch(rightsToAdd)) {
        modify.modifyPartyRights(ModType.ADD, right);
      }
      if(right.isMatch(rightsToDisable)) {
        modify.modifyPartyRights(ModType.DISABLED, right);
      }
      if(right.isMatch(rightsToRemove)) {
        modify.modifyPartyRights(ModType.REMOVE, right);
      }
    });
  }
  
  private void setRights(List<OrgRight> rights, List<OrgPartyRight> partyRights, BatchForOnePartyModify modify) {
    if(this.rightsToSet == null) {
      return;
    }
    
    final var rightsById = rights.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
   
    // removal all that are not part pf the new set
    partyRights.forEach(partyRight -> {
      final var right = rightsById.get(partyRight.getRightId());
      
      // already exists nothing to do
      if(right.isMatch(this.rightsToSet)) {
        return;
      }
      modify.modifyPartyRights(ModType.REMOVE, right);
    });
    
    // add all new
    rightsToSet.stream().map(id -> rights.stream().filter(m -> m.isMatch(id)).findFirst().get()).forEach(right -> {
      modify.modifyPartyRights(ModType.ADD, right);      
    });
  }  
  
  private void setMembers(List<OrgMember> members, List<OrgMembership> memberships, BatchForOnePartyModify modify) {
    if(this.membersToSet == null) {
      return;
    }
    
    final var byId = members.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
   
    // removal all that are not part pf the new set
    memberships.forEach(membership -> {
      final var member = byId.get(membership.getMemberId());
      
      // already exists nothing to do
      if(member.isMatch(this.membersToSet)) {
        return;
      }
      modify.modifyMembership(ModType.REMOVE, member);
    });
    
    // add all new
    membersToSet.stream().map(id -> members.stream().filter(m -> m.isMatch(id)).findFirst().get()).forEach(member -> {
      modify.modifyMembership(ModType.ADD, member);      
    });
  }  
  
  private void appendMembers(List<OrgMember> members, BatchForOnePartyModify modify) {
    members.forEach(member -> {
      if(member.isMatch(membersToAdd)) {
        modify.modifyMembership(ModType.ADD, member);
      }
      if(member.isMatch(membersToDisable)) {
         modify.modifyMembership(ModType.DISABLED, member);
      }
      if(member.isMatch(membersToRemove)) {
        modify.modifyMembership(ModType.REMOVE, member);
      }
    });
  }
}
