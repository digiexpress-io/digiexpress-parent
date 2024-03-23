package io.resys.thena.docdb.models.org.modify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.resys.thena.docdb.api.actions.ImmutableOnePartyEnvelope;
import io.resys.thena.docdb.api.actions.OrgCommitActions.ModType;
import io.resys.thena.docdb.api.actions.OrgCommitActions.ModifyOneParty;
import io.resys.thena.docdb.api.actions.OrgCommitActions.OnePartyEnvelope;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRight;
import io.resys.thena.docdb.models.org.OrgInserts.OrgBatchForOne;
import io.resys.thena.docdb.models.org.OrgState.OrgRepo;
import io.resys.thena.docdb.models.org.modify.BatchForOnePartyModify.NoPartyChangesException;
import io.resys.thena.docdb.spi.DataMapper;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ModifyOnePartyImpl implements ModifyOneParty {

  private final DbState state;

  private String repoId;
  private String author;
  private String message;

  private String partyId;
  private Optional<String> partyName;
  private Optional<String> partyDescription;
  private Optional<String> parentPartyId;
  private Optional<String> externalId;

  private Collection<String> allMembers = new LinkedHashSet<>();
  private Collection<String> membersToAdd = new LinkedHashSet<>();
  private Collection<String> membersToDisable = new LinkedHashSet<>();
  private Collection<String> membersToRemove = new LinkedHashSet<>();
  
  private Collection<String> allRights = new LinkedHashSet<>();
  private Collection<String> rightsToRemove = new LinkedHashSet<>();
  private Collection<String> rightsToAdd = new LinkedHashSet<>();
  private Collection<String> rightsToDisable = new LinkedHashSet<>();
  private Map<String, List<String>> addMemberWithRights = new HashMap<>();
  private Map<String, List<String>> disableMemberWithRights = new HashMap<>();
  private OrgActorStatusType newStatus;
  
  @Override public ModifyOnePartyImpl partyId(String partyId) {
    this.partyId = RepoAssert.notEmpty(partyId, () -> "partyId can't be empty!"); 
    return this; 
  }
  @Override public ModifyOnePartyImpl repoId(String repoId) {
    this.repoId = RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!"); 
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
    this.parentPartyId = Optional.ofNullable(RepoAssert.notEmpty(parentId, () -> "parentId can't be empty!")); 
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
    RepoAssert.notEmptyIfDefined(externalId, () -> "externalId can't be empty!");

    return this.state.toOrgState().withTransaction(repoId, this::doInTx);
  }
  
  
  private Uni<OnePartyEnvelope> doInTx(OrgRepo tx) {
		// members
		final Uni<List<OrgMember>> memberPromise = this.allMembers.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) : 
			tx.query().members().findAll(allMembers).collect().asList();

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
    final Uni<List<OrgRight>> rightsPromise = this.allRights.isEmpty() ? 
        Uni.createFrom().item(Collections.emptyList()) :
        tx.query().rights().findAll(allRights).collect().asList();
      

    
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
            .status(Repo.CommitResultStatus.NO_CHANGES)
            .build());
		    }
		  });
  }
  
  private OnePartyEnvelope validateQueryResponse(
      OrgRepo tx, 
      List<OrgMember> members, 
      OrgParty party, 
      List<OrgRight> rights,
      Optional<OrgParty> parent) {
    
    if(party == null) {
      return ImmutableOnePartyEnvelope.builder()
          .repoId(repoId)
          .status(Repo.CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find party by id/name/externalid: '" + partyId  +"'!")
              .build())
          .build();
    }
    if(parentPartyId != null && parentPartyId.isPresent() && parent.isEmpty() ) {
      return ImmutableOnePartyEnvelope.builder()
          .repoId(repoId)
          .status(Repo.CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find parent party by id/name/externalid: '" + parentPartyId.get()  +"'!")
              .build())
          .build();
    }
    if(parent != null && parent.isPresent() && parent.get().getId().equals(party.getId()) ) {
      return ImmutableOnePartyEnvelope.builder()
          .repoId(repoId)
          .status(Repo.CommitResultStatus.ERROR)
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
          .status(Repo.CommitResultStatus.ERROR)
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
          .status(Repo.CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find all rights: \r\n found: \r\n" + found + " \r\n but requested: \r\n" + expected + "!")
              .build())
          .build();
    }
    return null;
  }
  
  private Uni<OnePartyEnvelope> createResponse(
      OrgRepo tx, 
      List<OrgMember> members, 
      OrgParty party, 
      List<OrgRight> rights,
      List<OrgActorStatus> status,
      List<OrgMembership> memberships,
      List<OrgPartyRight> partyRights,
      List<OrgMemberRight> memberRights,
      Optional<OrgParty> parent) throws NoPartyChangesException {
    
    final Map<OrgMember, List<OrgRight>> modifyMemberRightsInParty = new HashMap<>();
    final Map<String, List<OrgRight>> addMembersWithRights = new HashMap<>();
    final Map<String, String> addGroupMapping = new HashMap<>();
    
    final var modify = new BatchForOnePartyModify(tx.getRepo().getId(), author, message)
      .newExternalId(externalId)
      .newPartyName(partyName)
      .newPartyDesc(partyDescription)
      .newParentPartyId(parent)
      .newStatus(newStatus)
      .current(party)
      .currentStatus(status)
      .currentMemberships(memberships)
      .currentPartyRights(partyRights)
      .currentMemberRights(memberRights);

    // Remove or add groups 
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
      final var addToGroupRole = this.addMemberWithRights.keySet().stream().filter(c -> member.isMatch(c)).findFirst();
      if(addToGroupRole.isPresent()) {
        final var roles = new ArrayList<OrgRight>();
        addMembersWithRights.put(member.getId(), roles);
        modifyMemberRightsInParty.put(member, roles);
        addGroupMapping.put(addToGroupRole.get(), member.getId());
      }
    });
    
    // Remove or add roles 
    rights.forEach(role -> {
      
      if(role.isMatch(rightsToAdd)) {
        modify.modifyPartyRights(ModType.ADD, role);
      }
      if(role.isMatch(rightsToDisable)) {
        modify.modifyPartyRights(ModType.DISABLED, role);
      }
      if(role.isMatch(rightsToRemove)) {
        modify.modifyPartyRights(ModType.REMOVE, role);
      }      
      for(final var entry : this.addMemberWithRights.entrySet()) {
        final var addRoleToUserGroup = entry.getValue().stream().filter(c -> role.isMatch(c)).findFirst().isPresent();
        if(addRoleToUserGroup) {
          addMembersWithRights.get(addGroupMapping.get(entry.getKey())).add(role);
        }
      }
    });
    
    final OrgBatchForOne batch = modify
        .modifyMemberRightsInParty(ModType.ADD, modifyMemberRightsInParty)
        .create();
    return tx.insert().batchMany(batch)
        .onItem().transform(rsp -> ImmutableOnePartyEnvelope.builder()
          .repoId(repoId)
          .party(rsp.getParties().isEmpty() ? null : rsp.getParties().get(0))
          .addMessages(rsp.getLog())
          .addAllMessages(rsp.getMessages())
          .status(DataMapper.mapStatus(rsp.getStatus()))
          .build());
  }
}
