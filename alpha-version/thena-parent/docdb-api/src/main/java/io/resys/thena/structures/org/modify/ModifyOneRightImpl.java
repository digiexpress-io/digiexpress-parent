package io.resys.thena.structures.org.modify;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import io.resys.thena.api.actions.ImmutableOneRightEnvelope;
import io.resys.thena.api.actions.OrgCommitActions.ModType;
import io.resys.thena.api.actions.OrgCommitActions.ModifyOneRight;
import io.resys.thena.api.actions.OrgCommitActions.OneRightEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.org.OrgActorStatusType;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberRight;
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
import io.resys.thena.structures.org.modify.BatchForOneRightModify.NoRightChangesException;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ModifyOneRightImpl implements ModifyOneRight {

  private final DbState state;
  private final String repoId;
  
  private String author;
  private String message;

  private String rightId;
  private Optional<String> rightName;
  private Optional<String> rightDescription;
  private Optional<OrgDocSubType> rightSubType;
  private Optional<String> externalId;

  private Collection<String> allParties = new LinkedHashSet<>();
  private Collection<String> partiesToAdd = new LinkedHashSet<>();
  private Collection<String> partiesToRemove = new LinkedHashSet<>();
  private Collection<String> partiesToSet;
  
  private Collection<String> allMembers = new LinkedHashSet<>();
  private Collection<String> membersToAdd = new LinkedHashSet<>();
  private Collection<String> membersToRemove = new LinkedHashSet<>();
  private Collection<String> membersToSet;
  
  private OrgActorStatusType status;
  
  @Override public ModifyOneRightImpl rightId(String rightId) {
    this.rightId = RepoAssert.notEmpty(rightId, () -> "rightId can't be empty!"); 
    return this; 
  }
  @Override public ModifyOneRightImpl author(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this; 
  }
  @Override public ModifyOneRightImpl message(String message) { 
    this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!"); 
    return this; 
  }
  @Override public ModifyOneRightImpl externalId(String externalId) {
    this.externalId = Optional.ofNullable(externalId); 
    return this; 
  }
  @Override public ModifyOneRightImpl rightName(String rightName) {
    this.rightName = Optional.ofNullable(RepoAssert.notEmpty(rightName, () -> "rightName can't be empty!")); 
    return this; 
  }
  @Override public ModifyOneRightImpl rightDescription(String rightDescription) { 
    this.rightDescription = Optional.ofNullable(RepoAssert.notEmpty(rightDescription, () -> "rightDescription can't be empty!")); 
    return this; 
  }
  @Override public ModifyOneRightImpl rightSubType(OrgDocSubType rightSubType) { 
    this.rightSubType = Optional.ofNullable(RepoAssert.notNull(rightSubType, () -> "rightSubType can't be null!")); 
    return this; 
  }
  @Override public ModifyOneRightImpl status(OrgActorStatusType status) {
    this.status = status;
    return this;
  }
  @Override 
  public ModifyOneRightImpl modifyParty(ModType type, String partyIds) { 
    RepoAssert.notEmpty(partyIds, () -> "partyIds can't be empty!");
    
    this.allParties.add(partyIds);
    if(type == ModType.ADD) {
      partiesToAdd.add(partyIds);
    } else if(type == ModType.REMOVE) {
      partiesToRemove.add(partyIds);
    } else {
      RepoAssert.fail("Unknown modification type: " + type + "!");
    }
    return this; 
   }
  @Override 
  public ModifyOneRightImpl modifyMember(ModType type, String memberIds) {
    RepoAssert.notEmpty(memberIds, () -> "memberIds can't be empty!");

    this.allMembers.add(memberIds);
    if(type == ModType.ADD) {
      membersToAdd.add(memberIds);
    } else if(type == ModType.REMOVE) {
      membersToRemove.add(memberIds);
    } else {
      RepoAssert.fail("Unknown modification type: " + type + "!");
    }
    return this; 
  }
  @Override
  public ModifyOneRight setAllMembers(List<String> memberIdNameOrExtId) {
    this.membersToSet = new LinkedHashSet<>(RepoAssert.notNull(memberIdNameOrExtId, () -> "setAllMembers can't be null!"));
    this.membersToAdd.clear();
    this.membersToRemove.clear();
    this.allMembers.clear();
    this.allMembers.addAll(memberIdNameOrExtId);
    return this;
  }
  @Override
  public ModifyOneRight setAllParties(List<String> partyIdNameOrExtId) {
    this.partiesToSet = new LinkedHashSet<>(RepoAssert.notNull(partyIdNameOrExtId, () -> "setAllParties can't be null!"));
    this.partiesToAdd.clear();
    this.partiesToRemove.clear();
    this.allParties.clear();
    this.allParties.addAll(partyIdNameOrExtId);
    return this;
  }
  @Override
  public Uni<OneRightEnvelope> build() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(rightId, () -> "rightId can't be empty!");
    RepoAssert.notEmptyIfDefined(rightName, () -> "rightName can't be empty!");
    RepoAssert.notNullIfDefined(rightSubType, () -> "rightSubType can't be empty!");
    RepoAssert.notEmptyIfDefined(rightDescription, () -> "email can't be empty!");
    
    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(repoId).build();
    return this.state.withOrgTransaction(scope, this::doInTx);
  }
  
  
  private Uni<OneRightEnvelope> doInTx(OrgState tx) {
		// find all parties
		final Uni<List<OrgParty>> partiesPromiseFromAddingRemovingDisabling = this.allParties.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) : 
			tx.query().parties().findAll(allParties).collect().asList();
		
    final Uni<List<OrgParty>> partiesPromiseExisting = tx.query().parties().findAllByRightId(rightId).collect().asList();
    final Uni<List<OrgParty>> partiesPromise =  Uni.combine().all().unis(
        partiesPromiseFromAddingRemovingDisabling,
        partiesPromiseExisting
    ).asTuple().onItem().transform(tuple -> 
      ImmutableList.<OrgParty>builder()
        .addAll(tuple.getItem1())
        .addAll(tuple.getItem2())
        .build().stream().distinct().toList()
    );
		
		
		// find all members
		final Uni<List<OrgMember>> memberPromiseFromAddingRemovingDisabling = this.allMembers.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) :
			tx.query().members().findAll(allMembers).collect().asList();
		
    final Uni<List<OrgMember>> memebrsPromiseExisting = tx.query().members().findAllByRightId(rightId).collect().asList();
    final Uni<List<OrgMember>> membersPromise =  Uni.combine().all().unis(
        memberPromiseFromAddingRemovingDisabling,
        memebrsPromiseExisting
    ).asTuple().onItem().transform(tuple -> 
      ImmutableList.<OrgMember>builder()
        .addAll(tuple.getItem1())
        .addAll(tuple.getItem2())
        .build().stream().distinct().toList()
    );
		
		// all right for parties
		final Uni<List<OrgPartyRight>> partyRightsPromise = 
		  tx.query().partyRights().findAllByRightId(rightId).collect().asList();

    // all right for members
		final Uni<List<OrgMemberRight>> memberRightsPromise = 
      tx.query().memberRights().findAllByRightId(rightId).collect().asList();
    
		final Uni<OrgRight> rightPromise = tx.query().rights().getById(rightId);
		
		// join data
		return Uni.combine().all().unis(
		    partiesPromise, 
		    membersPromise, 
		    partyRightsPromise, 
		    memberRightsPromise,
		    rightPromise
		).asTuple().onItem().transformToUni(tuple -> {
		  
		   final var validated = validateQueryResponse(tuple.getItem1(), tuple.getItem2());
       if(validated != null) {
         return Uni.createFrom().item(validated);
       }
		  
       try {
         
         return createResponse(
           tx,
           tuple.getItem1(), 
           tuple.getItem2(),
           tuple.getItem3(),
           tuple.getItem4(),
           tuple.getItem5()
         );
         
       } catch(NoRightChangesException ex) {
         return Uni.createFrom().item(ImmutableOneRightEnvelope.builder()
             .repoId(repoId)
             .addMessages(ImmutableMessage.builder()
                 .exception(ex).text("Nothing to commit, rights data already in the expected state!")
                 .build())
             .status(CommitResultStatus.NO_CHANGES)
             .build());
       }
		});
  }
  
  private OneRightEnvelope validateQueryResponse(List<OrgParty> parties, List<OrgMember> members) {

    if(parties.size() < this.allParties.size()) {
      final var found = String.join(", ", parties.stream().map(e -> e.getPartyName()).toList());
      final var expected = String.join(", ", this.allParties);
      return ImmutableOneRightEnvelope.builder()
          .repoId(repoId)
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find all parties: \r\n found: \r\n" + found + " \r\n but requested: \r\n" + expected + "!")
              .build())
          .build();
    }
    
    if(members.size() < this.allMembers.size()) {
      final var found = String.join(", ", members.stream().map(e -> e.getUserName()).toList());
      final var expected = String.join(", ", this.allMembers);
      return ImmutableOneRightEnvelope.builder()
          .repoId(repoId)
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find all members: \r\n found: \r\n" + found + " \r\n but requested: \r\n" + expected + "!")
              .build())
          .build();
    }
    return null;
  }
      

  private Uni<OneRightEnvelope> createResponse(
      OrgState tx, 
      List<OrgParty> allRelatedParties, 
      List<OrgMember> allRelatedMembers,
      List<OrgPartyRight> partyRights,
      List<OrgMemberRight> memberRights, 
      OrgRight right) throws NoRightChangesException {

    final var modify = new BatchForOneRightModify(tx.getTenantId(), author, message)
        .current(right)
        .currentMemberRights(memberRights)
        .currentPartyRights(partyRights)
        .newExternalId(externalId)
        .newRightName(rightName)
        .newRightDescription(rightDescription)
        .newRightSubType(rightSubType)
        .newStatus(status); 

    appendParties(allRelatedParties, modify);
    appendMembers(allRelatedMembers, modify);

    setParties(allRelatedParties, partyRights, modify);
    setMembers(allRelatedMembers, memberRights, modify);
    
    final OrgBatchForOne batch = modify.create();
    return tx.insert().batchMany(batch)
        .onItem().transform(rsp -> {
          
          final var removedMembers = rsp.getMemberRightsToDelete().stream().map(r -> r.getMemberId()).toList();
          final var removedParties = rsp.getPartyRightToDelete().stream().map(r -> r.getPartyId()).toList();
          
          return ImmutableOneRightEnvelope.builder()
          .repoId(repoId)
          .right(rsp.getRightsToUpdate().isEmpty() ? right : rsp.getRightsToUpdate().get(0))
          .addMessages(ImmutableMessage.builder().text(rsp.getLog()).build())
          .addAllMessages(rsp.getMessages())
          .status(BatchStatus.mapStatus(rsp.getStatus()))
          .directParties(allRelatedParties.stream()
              .filter(member -> !removedParties.contains(member.getId()))
              .toList())
          .directMembers(allRelatedMembers.stream()
              .filter(member -> !removedMembers.contains(member.getId()))
              .toList())
          .build();
        });

  }
  
  private void setParties(List<OrgParty> parties, List<OrgPartyRight> partyRights, BatchForOneRightModify modify) {
    if(this.partiesToSet == null) {
      return;
    }
    
    final var byId = parties.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
   
    // removal all that are not part pf the new set
    partyRights.forEach(membership -> {
      final var party = byId.get(membership.getPartyId());
      
      // already exists nothing to do
      if(party.isMatch(this.partiesToSet)) {
        return;
      }
      modify.updateParty(ModType.REMOVE, party);
    });
    
    // add all new
    partiesToSet.stream().map(id -> parties.stream().filter(m -> m.isMatch(id)).findFirst().get()).forEach(party -> {
      modify.updateParty(ModType.ADD, party);      
    });
  }  

  private void setMembers(List<OrgMember> members, List<OrgMemberRight> memberRights, BatchForOneRightModify modify) {
    if(this.membersToSet == null) {
      return;
    }
    
    final var byId = members.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
   
    // removal all that are not part pf the new set
    memberRights.forEach(membership -> {
      final var member = byId.get(membership.getMemberId());
      
      // already exists nothing to do
      if(member.isMatch(this.membersToSet)) {
        return;
      }
      modify.updateMember(ModType.REMOVE, member);
    });
    
    // add all new
    membersToSet.stream().map(id -> members.stream().filter(m -> m.isMatch(id)).findFirst().get()).forEach(member -> {
      modify.updateMember(ModType.ADD, member);      
    });
  }  
  
  
  private void appendParties(List<OrgParty> parties, BatchForOneRightModify modify) {
    parties.forEach(party -> {
      if( party.isMatch(partiesToAdd)) {
        modify.updateParty(ModType.ADD, party);
      }
      if( party.isMatch(partiesToRemove)) {
        modify.updateParty(ModType.REMOVE, party);
      }
    }); 
  }
  
  private void appendMembers(List<OrgMember> members, BatchForOneRightModify modify) {
    members.forEach(member -> {
      if( member.isMatch(membersToAdd) ) {
        modify.updateMember(ModType.ADD, member);
      }
      
      if( member.isMatch(membersToRemove) ) {
        modify.updateMember(ModType.REMOVE, member);
       }
    });
  }
}
