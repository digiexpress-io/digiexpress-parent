package io.resys.thena.docdb.models.org.create;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.resys.thena.docdb.api.actions.ImmutableOneGroupEnvelope;
import io.resys.thena.docdb.api.actions.OrgCommitActions.CreateOneGroup;
import io.resys.thena.docdb.api.actions.OrgCommitActions.OneGroupEnvelope;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.resys.thena.docdb.models.org.OrgInserts.OrgBatchForOne;
import io.resys.thena.docdb.models.org.OrgState.OrgRepo;
import io.resys.thena.docdb.models.org.support.BatchForOneGroupCreate;
import io.resys.thena.docdb.spi.DataMapper;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateOneGroupImpl implements CreateOneGroup {

  private final DbState state;

  private String repoId;
  private String author;
  private String message;

  private String externalId;
  private String parentId;
  private String groupName;
  private String groupDescription;

  private List<String> addUsersToGroup = new ArrayList<>();
  private List<String> addRolesToGroup = new ArrayList<>();

  @Override public CreateOneGroupImpl repoId(String repoId) {         this.repoId = RepoAssert.notEmpty(repoId,           () -> "repoId can't be empty!"); return this; }
  @Override public CreateOneGroupImpl author(String author) {         this.author = RepoAssert.notEmpty(author,           () -> "author can't be empty!"); return this; }
  @Override public CreateOneGroupImpl message(String message) {       this.message = RepoAssert.notEmpty(message,         () -> "message can't be empty!"); return this; }
  @Override public CreateOneGroupImpl groupName(String groupName) {   this.groupName = RepoAssert.notEmpty(groupName,     () -> "groupName can't be empty!"); return this; }
  @Override public CreateOneGroupImpl groupDescription(String desc) { this.groupDescription = RepoAssert.notEmpty(desc,   () -> "groupDescription can't be empty!"); return this; }
  
  @Override public CreateOneGroupImpl parentId(String parentId) {     this.parentId = parentId; return this; }
  @Override public CreateOneGroupImpl externalId(String externalId) { this.externalId = externalId; return this; }
  @Override public CreateOneGroupImpl addUsersToGroup(List<String> addUsersToGroup) { this.addUsersToGroup.addAll(RepoAssert.notNull(addUsersToGroup, () -> "addUsersToGroup can't be empty!")); return this; }
  @Override public CreateOneGroupImpl addRolesToGroup(List<String> addRolesToGroup) { this.addRolesToGroup.addAll(RepoAssert.notNull(addRolesToGroup, () -> "addRolesToGroup can't be empty!")); return this; }
  
  @Override
  public Uni<OneGroupEnvelope> build() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(groupName, () -> "groupName can't be empty!");
    RepoAssert.notEmpty(groupDescription, () -> "groupDescription can't be empty!");

    return this.state.toOrgState().withTransaction(repoId, this::doInTx);
  }
  
  private Uni<OneGroupEnvelope> doInTx(OrgRepo tx) {
		// find users
		final Uni<List<OrgUser>> usersUni = this.addUsersToGroup.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) : 
			tx.query().users().findAll(addUsersToGroup).collect().asList();
		
		// roles
		final Uni<List<OrgRole>> rolesUni = this.addRolesToGroup.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) :
			tx.query().roles().findAll(addRolesToGroup).collect().asList();
		
		// fetch parent group
		final Uni<Optional<OrgGroup>> parentUni = this.parentId == null ? 
			Uni.createFrom().item(Optional.empty()) : 
			tx.query().groups().getById(parentId).onItem().transform(parent -> Optional.of(parent));
	
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

  private Uni<OneGroupEnvelope> createGroup(OrgRepo tx, List<OrgUser> users, List<OrgRole> roles, Optional<OrgGroup> parent) {
    final OrgBatchForOne batch = new BatchForOneGroupCreate(tx.getRepo().getId(), author, message)
        .externalId(externalId)
        .users(users)
        .roles(roles)
        .groupName(groupName)
        .groupDescription(groupDescription)
        .parent(parent.orElse(null))
        .create();

    return tx.insert().batchOne(batch)
      .onItem().transform(rsp -> {
      	
      	
      	return ImmutableOneGroupEnvelope.builder()
        .repoId(repoId)
        .group(rsp.getGroups().isEmpty() ? null : rsp.getGroups().get(0))
        .addMessages(rsp.getLog())
        .addAllMessages(rsp.getMessages())
        .status(DataMapper.mapStatus(rsp.getStatus()))
        .build();
      	
      });
  } 
}
