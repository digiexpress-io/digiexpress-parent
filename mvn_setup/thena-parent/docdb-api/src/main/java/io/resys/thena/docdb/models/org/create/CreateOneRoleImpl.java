package io.resys.thena.docdb.models.org.create;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.resys.thena.docdb.api.actions.ImmutableOneRoleEnvelope;
import io.resys.thena.docdb.api.actions.OrgCommitActions.CreateOneRole;
import io.resys.thena.docdb.api.actions.OrgCommitActions.OneRoleEnvelope;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.models.org.OrgInserts.OrgBatchForOne;
import io.resys.thena.docdb.models.org.OrgState.OrgRepo;
import io.resys.thena.docdb.spi.DataMapper;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateOneRoleImpl implements CreateOneRole {

  private final DbState state;

  private String repoId;
  private String author;
  private String message;

  private String externalId;
  private String roleName;
  private String roleDescription;

  private List<String> addRoleToGroups = new ArrayList<>();
  private List<String> addRoleToUsers = new ArrayList<>();

  @Override public CreateOneRoleImpl repoId(String repoId) {         this.repoId = RepoAssert.notEmpty(repoId,           () -> "repoId can't be empty!"); return this; }
  @Override public CreateOneRoleImpl author(String author) {         this.author = RepoAssert.notEmpty(author,           () -> "author can't be empty!"); return this; }
  @Override public CreateOneRoleImpl message(String message) {       this.message = RepoAssert.notEmpty(message,         () -> "message can't be empty!"); return this; }
  @Override public CreateOneRoleImpl roleName(String roleName) {   	 this.roleName = RepoAssert.notEmpty(roleName,       () -> "roleName can't be empty!"); return this; }
  @Override public CreateOneRoleImpl roleDescription(String desc) {  this.roleDescription = RepoAssert.notEmpty(desc,    () -> "roleDescription can't be empty!"); return this; }
  @Override public CreateOneRoleImpl externalId(String externalId) { this.externalId = externalId; return this; }
  
  @Override public CreateOneRoleImpl addRoleToGroups(List<String> addRoleToGroups) { this.addRoleToGroups.addAll(RepoAssert.notNull(addRoleToGroups, () -> "addRoleToGroups can't be empty!")); return this; }
  @Override public CreateOneRoleImpl addRoleToUsers(List<String> addRoleToUsers) { 	 this.addRoleToUsers.addAll(RepoAssert.notNull(addRoleToUsers, () -> "addRoleToUsers can't be empty!")); return this; }
  
  @Override
  public Uni<OneRoleEnvelope> build() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(roleName, () -> "roleName can't be empty!");
    RepoAssert.notEmpty(roleDescription, () -> "roleDescription can't be empty!");

    return this.state.toOrgState().withTransaction(repoId, this::doInTx);
  }
  
  private Uni<OneRoleEnvelope> doInTx(OrgRepo tx) {
		// find users
		final Uni<List<OrgMember>> usersUni = this.addRoleToUsers.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) : 
			tx.query().members().findAll(addRoleToUsers).collect().asList();
		
		
		// find group
		final Uni<List<OrgParty>> groupsUni = this.addRoleToGroups.isEmpty() ?
			Uni.createFrom().item(Collections.emptyList()) : 
			tx.query().parties().findAll(addRoleToGroups).collect().asList();
	
		// join data
		return Uni.combine().all().unis(usersUni, groupsUni).asTuple()
		  .onItem().transformToUni(tuple -> createRole(
			  tx,
	      tuple.getItem1(), 
	      tuple.getItem2()
		  )
		);
  }

  private Uni<OneRoleEnvelope> createRole(OrgRepo tx, List<OrgMember> users, List<OrgParty> groups) {
    final OrgBatchForOne batch = new BatchForOneRoleCreate(tx.getRepo().getId(), author, message)
        .externalId(externalId)
        .users(users)
        .groups(groups)
        .roleName(roleName)
        .roleDescription(roleDescription)
        .create();

    return tx.insert().batchOne(batch)
      .onItem().transform(rsp -> ImmutableOneRoleEnvelope.builder()
        .repoId(repoId)
        .role(rsp.getRoles().isEmpty() ? null : rsp.getRoles().get(0))
        .addMessages(rsp.getLog())
        .addAllMessages(rsp.getMessages())
        .status(DataMapper.mapStatus(rsp.getStatus()))
        .build());
  } 
}
