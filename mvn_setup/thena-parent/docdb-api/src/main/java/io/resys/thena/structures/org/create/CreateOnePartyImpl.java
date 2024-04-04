package io.resys.thena.structures.org.create;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.resys.thena.api.actions.ImmutableOnePartyEnvelope;
import io.resys.thena.api.actions.OrgCommitActions.CreateOneParty;
import io.resys.thena.api.actions.OrgCommitActions.OnePartyEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.org.OrgInserts.OrgBatchForOne;
import io.resys.thena.structures.org.OrgState;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateOnePartyImpl implements CreateOneParty {

  private final DbState state;

  private final String repoId;
  private String author;
  private String message;

  private String externalId;
  private String parentId;
  private String groupName;
  private String groupDescription;

  private List<String> addUsersToParty = new ArrayList<>();
  private List<String> addRightsToParty = new ArrayList<>();

  @Override public CreateOnePartyImpl author(String author) {         this.author = RepoAssert.notEmpty(author,           () -> "author can't be empty!"); return this; }
  @Override public CreateOnePartyImpl message(String message) {       this.message = RepoAssert.notEmpty(message,         () -> "message can't be empty!"); return this; }
  @Override public CreateOnePartyImpl partyName(String groupName) {   this.groupName = RepoAssert.notEmpty(groupName,     () -> "groupName can't be empty!"); return this; }
  @Override public CreateOnePartyImpl partyDescription(String desc) { this.groupDescription = RepoAssert.notEmpty(desc,   () -> "groupDescription can't be empty!"); return this; }
  
  @Override public CreateOnePartyImpl parentId(String parentId) {     this.parentId = parentId; return this; }
  @Override public CreateOnePartyImpl externalId(String externalId) { this.externalId = externalId; return this; }
  @Override public CreateOnePartyImpl addMemberToParty(List<String> addUsersToGroup) { this.addUsersToParty.addAll(RepoAssert.notNull(addUsersToGroup, () -> "addUsersToGroup can't be empty!")); return this; }
  @Override public CreateOnePartyImpl addRightsToParty(List<String> addRolesToGroup) { this.addRightsToParty.addAll(RepoAssert.notNull(addRolesToGroup, () -> "addRolesToGroup can't be empty!")); return this; }
  
  @Override
  public Uni<OnePartyEnvelope> build() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(groupName, () -> "groupName can't be empty!");
    RepoAssert.notEmpty(groupDescription, () -> "groupDescription can't be empty!");

    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(repoId).build();
    return this.state.withOrgTransaction(scope, this::doInTx);
  }
  
  private Uni<OnePartyEnvelope> doInTx(OrgState tx) {
		// find users
		final Uni<List<OrgMember>> usersUni = this.addUsersToParty.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) : 
			tx.query().members().findAll(addUsersToParty).collect().asList();
		
		// roles
		final Uni<List<OrgRight>> rolesUni = this.addRightsToParty.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) :
			tx.query().rights().findAll(addRightsToParty).collect().asList();
		
		// fetch parent group
		final Uni<Optional<OrgParty>> parentUni = this.parentId == null ? 
			Uni.createFrom().item(Optional.empty()) : 
			tx.query().parties().getById(parentId).onItem().transform(parent -> Optional.of(parent));
	
		// join data
		return Uni.combine().all().unis(usersUni, rolesUni, parentUni).asTuple()
		  .onItem().transformToUni(tuple -> createGroup(
			  tx,
	      tuple.getItem1(), 
	      tuple.getItem2(), 
	      tuple.getItem3()
		  )
		);
  }

  private Uni<OnePartyEnvelope> createGroup(OrgState tx, List<OrgMember> members, List<OrgRight> rights, Optional<OrgParty> parent) {
    
    // assert rights
    if(rights.size() != this.addRightsToParty.size()) {
      final var found = String.join(", ", rights.stream().map(e -> e.getRightName()).toList());
      final var expected = String.join(", ", this.addRightsToParty);
      return Uni.createFrom().item(ImmutableOnePartyEnvelope.builder()
          .repoId(repoId)
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find all rights(for party): \r\n found: \r\n" + found + " \r\n but requested: \r\n" + expected + "!")
              .build())
          .build());
    }    
    
    final OrgBatchForOne batch = new BatchForOnePartyCreate(tx.getTenantId(), author, message)
        .externalId(externalId)
        .users(members)
        .roles(rights)
        .groupName(groupName)
        .groupDescription(groupDescription)
        .parent(parent.orElse(null))
        .create();

    return tx.insert().batchMany(batch)
      .onItem().transform(rsp -> {
      	return ImmutableOnePartyEnvelope.builder()
        .repoId(repoId)
        .party(rsp.getParties().isEmpty() ? null : rsp.getParties().get(0))
        .addMessages(rsp.getLog())
        .addAllMessages(rsp.getMessages())
        .status(BatchStatus.mapStatus(rsp.getStatus()))
        .build();
      })
      .onItem().transformToUni(rsp -> {
        if(rsp.getStatus() == CommitResultStatus.CONFLICT || rsp.getStatus() == CommitResultStatus.ERROR) {
          return Uni.createFrom().item(rsp);
        }
        
        return tx.query().rights().findAllByPartyId(rsp.getParty().getId())
            .collect().asList()
            .onItem().transform(direct -> ImmutableOnePartyEnvelope.builder()
                .from(rsp)
                .directRights(direct)
                .build());
      });
  } 
}
