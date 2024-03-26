package io.resys.thena.docdb.models.org.create;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.resys.thena.docdb.api.actions.ImmutableOnePartyEnvelope;
import io.resys.thena.docdb.api.actions.OrgCommitActions.CreateOneParty;
import io.resys.thena.docdb.api.actions.OrgCommitActions.OnePartyEnvelope;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRight;
import io.resys.thena.docdb.models.org.OrgInserts.OrgBatchForOne;
import io.resys.thena.docdb.models.org.OrgState.OrgRepo;
import io.resys.thena.docdb.spi.DataMapper;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
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

  private List<String> addUsersToGroup = new ArrayList<>();
  private List<String> addRolesToGroup = new ArrayList<>();

  @Override public CreateOnePartyImpl author(String author) {         this.author = RepoAssert.notEmpty(author,           () -> "author can't be empty!"); return this; }
  @Override public CreateOnePartyImpl message(String message) {       this.message = RepoAssert.notEmpty(message,         () -> "message can't be empty!"); return this; }
  @Override public CreateOnePartyImpl partyName(String groupName) {   this.groupName = RepoAssert.notEmpty(groupName,     () -> "groupName can't be empty!"); return this; }
  @Override public CreateOnePartyImpl partyDescription(String desc) { this.groupDescription = RepoAssert.notEmpty(desc,   () -> "groupDescription can't be empty!"); return this; }
  
  @Override public CreateOnePartyImpl parentId(String parentId) {     this.parentId = parentId; return this; }
  @Override public CreateOnePartyImpl externalId(String externalId) { this.externalId = externalId; return this; }
  @Override public CreateOnePartyImpl addMemberToParty(List<String> addUsersToGroup) { this.addUsersToGroup.addAll(RepoAssert.notNull(addUsersToGroup, () -> "addUsersToGroup can't be empty!")); return this; }
  @Override public CreateOnePartyImpl addRightsToParty(List<String> addRolesToGroup) { this.addRolesToGroup.addAll(RepoAssert.notNull(addRolesToGroup, () -> "addRolesToGroup can't be empty!")); return this; }
  
  @Override
  public Uni<OnePartyEnvelope> build() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(groupName, () -> "groupName can't be empty!");
    RepoAssert.notEmpty(groupDescription, () -> "groupDescription can't be empty!");

    return this.state.toOrgState().withTransaction(repoId, this::doInTx);
  }
  
  private Uni<OnePartyEnvelope> doInTx(OrgRepo tx) {
		// find users
		final Uni<List<OrgMember>> usersUni = this.addUsersToGroup.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) : 
			tx.query().members().findAll(addUsersToGroup).collect().asList();
		
		// roles
		final Uni<List<OrgRight>> rolesUni = this.addRolesToGroup.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) :
			tx.query().rights().findAll(addRolesToGroup).collect().asList();
		
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

  private Uni<OnePartyEnvelope> createGroup(OrgRepo tx, List<OrgMember> users, List<OrgRight> roles, Optional<OrgParty> parent) {
    final OrgBatchForOne batch = new BatchForOnePartyCreate(tx.getRepo().getId(), author, message)
        .externalId(externalId)
        .users(users)
        .roles(roles)
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
        .status(DataMapper.mapStatus(rsp.getStatus()))
        .build();
      	
      });
  } 
}
