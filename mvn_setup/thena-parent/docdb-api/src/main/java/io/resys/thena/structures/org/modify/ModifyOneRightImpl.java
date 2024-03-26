package io.resys.thena.structures.org.modify;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import io.resys.thena.api.actions.ImmutableOneRightEnvelope;
import io.resys.thena.api.actions.OrgCommitActions.ModType;
import io.resys.thena.api.actions.OrgCommitActions.ModifyOneParty;
import io.resys.thena.api.actions.OrgCommitActions.ModifyOneRight;
import io.resys.thena.api.actions.OrgCommitActions.OneRightEnvelope;
import io.resys.thena.api.entities.Tenant.CommitResultStatus;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMember;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMemberRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgParty;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgRight;
import io.resys.thena.api.models.ImmutableMessage;
import io.resys.thena.spi.DataMapper;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.org.OrgInserts.OrgBatchForOne;
import io.resys.thena.structures.org.OrgState.OrgRepo;
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
  private Optional<String> externalId;

  private Collection<String> allParties = new LinkedHashSet<>();
  private Collection<String> partiesToAdd = new LinkedHashSet<>();
  private Collection<String> partiesToRemove = new LinkedHashSet<>();
  private Collection<String> allMembers = new LinkedHashSet<>();
  private Collection<String> membersToAdd = new LinkedHashSet<>();
  private Collection<String> membersToRemove = new LinkedHashSet<>();
  
  @Override public ModifyOneRightImpl rightId(String rightId) {       this.rightId = RepoAssert.notEmpty(rightId,         () -> "rightId can't be empty!"); return this; }
  @Override public ModifyOneRightImpl author(String author) {         this.author = RepoAssert.notEmpty(author,           () -> "author can't be empty!"); return this; }
  @Override public ModifyOneRightImpl message(String message) {       this.message = RepoAssert.notEmpty(message,         () -> "message can't be empty!"); return this; }
  @Override public ModifyOneRightImpl externalId(String externalId) { this.externalId = Optional.ofNullable(externalId); return this; }
  @Override public ModifyOneRightImpl rightName(String rightName) {   this.rightName = Optional.ofNullable(RepoAssert.notEmpty(rightName, () -> "rightName can't be empty!")); return this; }
  @Override public ModifyOneRightImpl rightDescription(String rightDescription) { this.rightDescription = Optional.ofNullable(RepoAssert.notEmpty(rightDescription, () -> "rightDescription can't be empty!")); return this; }
  @Override
  public ModifyOneParty status(OrgActorStatusType status) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override 
  public ModifyOneRightImpl modifyParty(ModType type, String partyIds) { 
    RepoAssert.notEmpty(partyIds, () -> "partyIds can't be empty!");
    
    this.allParties.add(partyIds);
    if(type == ModType.ADD) {
      partiesToAdd.add(partyIds);
    } else if(type == ModType.DISABLED) {
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
    } else if(type == ModType.DISABLED) {
      membersToRemove.add(memberIds);
    } else {
      RepoAssert.fail("Unknown modification type: " + type + "!");
    }
    return this; 
  }

  @Override
  public Uni<OneRightEnvelope> build() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(rightId, () -> "rightId can't be empty!");
    
    RepoAssert.notEmptyIfDefined(rightName, () -> "rightName can't be empty!");
    RepoAssert.notEmptyIfDefined(rightDescription, () -> "email can't be empty!");

    return this.state.toOrgState().withTransaction(repoId, this::doInTx);
  }
  
  
  private Uni<OneRightEnvelope> doInTx(OrgRepo tx) {
		// find all parties
		final Uni<List<OrgParty>> partiesPromise = this.allParties.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) : 
			tx.query().parties().findAll(allParties).collect().asList();
		
		// find all members
		final Uni<List<OrgMember>> membersPromise = this.allMembers.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) :
			tx.query().members().findAll(allMembers).collect().asList();
		
		// all right for parties
		final Uni<List<OrgPartyRight>> partyRightsPromise = 
		  tx.query().partyRights().findAllByRightId(rightId).collect().asList();

    // all right for members
		final Uni<List<OrgMemberRight>> memberRightsPromise = 
      tx.query().memberRights().findAllByMemberId(rightId).collect().asList();
    
		// find right status
		final Uni<List<OrgActorStatus>> rightStatusPromise = 
	    tx.query().actorStatus().findAllByIdRightId(rightId).collect().asList();
	    
		final Uni<OrgRight> rightPromise = tx.query().rights().getById(rightId);
		
		// join data
		return Uni.combine().all().unis(
		    partiesPromise, 
		    membersPromise, 
		    partyRightsPromise, 
		    
		    memberRightsPromise, 
		    rightStatusPromise, 
		    rightPromise
		).asTuple()
		  .onItem().transformToUni(tuple -> modifyRight(
			  tx,
	      tuple.getItem1(), 
	      tuple.getItem2(),
	      tuple.getItem3(),
	      tuple.getItem4(),
	      tuple.getItem5(),
	      tuple.getItem6()
		  )
		);
  }

  private Uni<OneRightEnvelope> modifyRight(
      OrgRepo tx, 
      List<OrgParty> parties, 
      List<OrgMember> members,
      List<OrgPartyRight> partyRights,
      List<OrgMemberRight> memberRights, 
      List<OrgActorStatus> rightStatus,           
      OrgRight right) {
    
    if(parties.size() < this.allParties.size()) {
      final var found = String.join(", ", parties.stream().map(e -> e.getPartyName()).toList());
      final var expected = String.join(", ", this.allParties);
      return Uni.createFrom().item(ImmutableOneRightEnvelope.builder()
          .repoId(repoId)
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find all parties: \r\n found: \r\n" + found + " \r\n but requested: \r\n" + expected + "!")
              .build())
          .build());
    }
    
    if(members.size() < this.allMembers.size()) {
      final var found = String.join(", ", members.stream().map(e -> e.getUserName()).toList());
      final var expected = String.join(", ", this.allMembers);
      return Uni.createFrom().item(ImmutableOneRightEnvelope.builder()
          .repoId(repoId)
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find all members: \r\n found: \r\n" + found + " \r\n but requested: \r\n" + expected + "!")
              .build())
          .build());
    }
    
    final var modify = new BatchForOneRightModify(tx.getRepo().getId(), author, message)
        .externalId(externalId)
        .memberRights(memberRights)
        .rightStatus(rightStatus)
        .partyRights(partyRights)
        .rightName(rightName)
        .rightDescription(rightDescription)
        .current(right); 

    // Remove or add groups 
    parties.forEach(party -> {
      if( partiesToAdd.contains(party.getPartyName()) ||
          partiesToAdd.contains(party.getId()) ||
          partiesToAdd.contains(party.getExternalId())
      ) {
        modify.updateParty(ModType.ADD, party);
      }
      
      if( partiesToRemove.contains(party.getPartyName()) ||
          partiesToRemove.contains(party.getId()) ||
          partiesToRemove.contains(party.getExternalId())
       ) {
         modify.updateParty(ModType.DISABLED, party);
       }
    });
    
    // Remove or add roles 
    members.forEach(member -> {
      
      if( membersToAdd.contains(member.getUserName()) ||
          membersToAdd.contains(member.getId()) ||
          membersToAdd.contains(member.getExternalId())
      ) {
        modify.updateMember(ModType.ADD, member);
      }
      
      if( membersToRemove.contains(member.getUserName()) ||
          membersToRemove.contains(member.getId()) ||
          membersToRemove.contains(member.getExternalId())
       ) {
        modify.updateMember(ModType.DISABLED, member);
       }
    });
    
    
    try {
      final OrgBatchForOne batch = modify.create();
      return tx.insert().batchMany(batch)
          .onItem().transform(rsp -> ImmutableOneRightEnvelope.builder()
            .repoId(repoId)
            .right(rsp.getRights().isEmpty() ? null : rsp.getRights().get(0))
            .addMessages(rsp.getLog())
            .addAllMessages(rsp.getMessages())
            .status(DataMapper.mapStatus(rsp.getStatus()))
            .build());
    } catch (NoRightChangesException e) {
      return Uni.createFrom().item(ImmutableOneRightEnvelope.builder()
            .repoId(repoId)
            .addMessages(ImmutableMessage.builder()
                .exception(e).text("Nothing to commit, rights data already in the expected state!")
                .build())
            .status(CommitResultStatus.NO_CHANGES)
            .build());
    }
  }

}
