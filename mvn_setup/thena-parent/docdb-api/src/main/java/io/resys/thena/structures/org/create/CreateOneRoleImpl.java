package io.resys.thena.structures.org.create;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.resys.thena.api.actions.ImmutableOneRightEnvelope;
import io.resys.thena.api.actions.OrgCommitActions.CreateOneRight;
import io.resys.thena.api.actions.OrgCommitActions.OneRightEnvelope;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.org.OrgInserts.OrgBatchForOne;
import io.resys.thena.structures.org.OrgState;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateOneRoleImpl implements CreateOneRight {

  private final DbState state;

  private final String repoId;
  private String author;
  private String message;

  private String externalId;
  private String roleName;
  private String roleDescription;

  private List<String> addRoleToGroups = new ArrayList<>();
  private List<String> addRoleToUsers = new ArrayList<>();

  @Override public CreateOneRoleImpl author(String author) {         this.author = RepoAssert.notEmpty(author,           () -> "author can't be empty!"); return this; }
  @Override public CreateOneRoleImpl message(String message) {       this.message = RepoAssert.notEmpty(message,         () -> "message can't be empty!"); return this; }
  @Override public CreateOneRoleImpl rightName(String roleName) {   	 this.roleName = RepoAssert.notEmpty(roleName,       () -> "roleName can't be empty!"); return this; }
  @Override public CreateOneRoleImpl rightDescription(String desc) {  this.roleDescription = RepoAssert.notEmpty(desc,    () -> "roleDescription can't be empty!"); return this; }
  @Override public CreateOneRoleImpl externalId(String externalId) { this.externalId = externalId; return this; }
  
  @Override public CreateOneRoleImpl addRightToParties(List<String> addRoleToGroups) { this.addRoleToGroups.addAll(RepoAssert.notNull(addRoleToGroups, () -> "addRoleToGroups can't be empty!")); return this; }
  @Override public CreateOneRoleImpl addRightToMembers(List<String> addRoleToUsers) { 	 this.addRoleToUsers.addAll(RepoAssert.notNull(addRoleToUsers, () -> "addRoleToUsers can't be empty!")); return this; }
  
  @Override
  public Uni<OneRightEnvelope> build() {
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(roleName, () -> "roleName can't be empty!");
    RepoAssert.notEmpty(roleDescription, () -> "roleDescription can't be empty!");

    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(repoId).build();
    return this.state.withOrgTransaction(scope, this::doInTx);
  }
  
  private Uni<OneRightEnvelope> doInTx(OrgState tx) {
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

  private Uni<OneRightEnvelope> createRole(OrgState tx, List<OrgMember> users, List<OrgParty> groups) {
    final OrgBatchForOne batch = new BatchForOneRightCreate(tx.getTenantId(), author, message)
        .externalId(externalId)
        .users(users)
        .parties(groups)
        .partyName(roleName)
        .partyDescription(roleDescription)
        .create();

    return tx.insert().batchMany(batch)
      .onItem().transform(rsp -> ImmutableOneRightEnvelope.builder()
        .repoId(repoId)
        .right(rsp.getRights().isEmpty() ? null : rsp.getRights().get(0))
        .addMessages(rsp.getLog())
        .addAllMessages(rsp.getMessages())
        .status(BatchStatus.mapStatus(rsp.getStatus()))
        .build());
  } 
}
