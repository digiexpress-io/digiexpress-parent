package io.resys.thena.docdb.models.org.create;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.resys.thena.docdb.api.actions.ImmutableOneUserEnvelope;
import io.resys.thena.docdb.api.actions.OrgCommitActions.CreateOneUser;
import io.resys.thena.docdb.api.actions.OrgCommitActions.OneUserEnvelope;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.models.org.OrgInserts.OrgBatchForOne;
import io.resys.thena.docdb.models.org.OrgState.OrgRepo;
import io.resys.thena.docdb.models.org.support.BatchForOneUserCreate;
import io.resys.thena.docdb.spi.DataMapper;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateOneUserImpl implements CreateOneUser {

  private final DbState state;

  private String repoId;
  private String author;
  private String message;

  private String userName;
  private String email;
  private String externalId;

  private List<String> addUserToGroups = new ArrayList<>();
  private List<String> addUserToRoles = new ArrayList<>();

  @Override public CreateOneUserImpl repoId(String repoId) {         this.repoId = RepoAssert.notEmpty(repoId,           () -> "repoId can't be empty!"); return this; }
  @Override public CreateOneUserImpl author(String author) {         this.author = RepoAssert.notEmpty(author,           () -> "author can't be empty!"); return this; }
  @Override public CreateOneUserImpl message(String message) {       this.message = RepoAssert.notEmpty(message,         () -> "message can't be empty!"); return this; }
  @Override public CreateOneUserImpl userName(String userName) {     this.userName = RepoAssert.notEmpty(userName,       () -> "userName can't be empty!"); return this; }
  @Override public CreateOneUserImpl email(String email) {           this.email = RepoAssert.notEmpty(email,             () -> "email can't be empty!"); return this; }
  
  @Override public CreateOneUserImpl externalId(String externalId) { this.externalId = externalId; return this; }
  @Override public CreateOneUserImpl addUserToGroups(String ... addUserToGroups) { this.addUserToGroups.addAll(Arrays.asList(addUserToGroups)); return this; }
  @Override public CreateOneUserImpl addUserToGroups(List<String> addUserToGroups) { this.addUserToGroups.addAll(RepoAssert.notNull(addUserToGroups, () -> "addUserToGroups can't be empty!")); return this; }
  @Override public CreateOneUserImpl addUserToRoles(List<String> addUserToRoles) { this.addUserToRoles.addAll(RepoAssert.notNull(addUserToRoles, () -> "addUserToRoles can't be empty!")); return this; }
  
  @Override
  public Uni<OneUserEnvelope> build() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(userName, () -> "userName can't be empty!");
    RepoAssert.notEmpty(email, () -> "email can't be empty!");

    return this.state.toOrgState().withTransaction(repoId, this::doInTx);
  }
  
  private Uni<OneUserEnvelope> doInTx(OrgRepo tx) {
		// find groups
		final Uni<List<OrgGroup>> groupsUni = this.addUserToGroups.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) : 
			tx.query().groups().findAll(addUserToGroups).collect().asList();
		
		// roles
		final Uni<List<OrgRole>> rolesUni = this.addUserToRoles.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) :
			tx.query().roles().findAll(addUserToRoles).collect().asList();
		
		// join data
		return Uni.combine().all().unis(groupsUni, rolesUni).asTuple()
		  .onItem().transformToUni(tuple -> createUser(
			  tx,
	      tuple.getItem1(), 
	      tuple.getItem2()
		  )
		);
  }

  
  private Uni<OneUserEnvelope> createUser(OrgRepo tx, List<OrgGroup> groups, List<OrgRole> roles) {
    final OrgBatchForOne batch = new BatchForOneUserCreate(tx.getRepo().getId(), author, message)
        .externalId(externalId)
        .email(email)
        .groups(groups)
        .roles(roles)
        .userName(userName)
        .create(); 

    return tx.insert().batchOne(batch)
      .onItem().transform(rsp -> ImmutableOneUserEnvelope.builder()
        .repoId(repoId)
        .user(rsp.getUsers().isEmpty() ? null : rsp.getUsers().get(0))
        .addMessages(rsp.getLog())
        .addAllMessages(rsp.getMessages())
        .status(DataMapper.mapStatus(rsp.getStatus()))
        .build());
  } 
}
