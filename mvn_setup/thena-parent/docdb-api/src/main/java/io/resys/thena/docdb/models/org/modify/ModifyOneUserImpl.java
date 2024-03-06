package io.resys.thena.docdb.models.org.modify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.resys.thena.docdb.api.actions.ImmutableOneUserEnvelope;
import io.resys.thena.docdb.api.actions.OrgCommitActions.ModType;
import io.resys.thena.docdb.api.actions.OrgCommitActions.ModifyOneUser;
import io.resys.thena.docdb.api.actions.OrgCommitActions.OneUserEnvelope;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.Repo.CommitResultStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserGroupsAndRolesWithLog;
import io.resys.thena.docdb.models.org.OrgInserts.OrgBatchForOne;
import io.resys.thena.docdb.models.org.OrgState.OrgRepo;
import io.resys.thena.docdb.models.org.queries.OrgUserGroupsAndRolesQueryImpl;
import io.resys.thena.docdb.models.org.support.BatchForOneUserModify;
import io.resys.thena.docdb.spi.DataMapper;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ModifyOneUserImpl implements ModifyOneUser {

  private final DbState state;

  private String repoId;
  private String author;
  private String message;

  private String userId;
  private Optional<String> userName;
  private Optional<String> email;
  private Optional<String> externalId;

  private List<String> allGroups = new ArrayList<>();
  private List<String> groupsToAdd = new ArrayList<>();
  private List<String> groupsToRemove = new ArrayList<>();
  private List<String> allRoles = new ArrayList<>();
  private List<String> rolesToAdd = new ArrayList<>();
  private List<String> rolesToRemove = new ArrayList<>();

  @Override public ModifyOneUserImpl userId(String userId) {         this.userId = RepoAssert.notEmpty(userId,           () -> "userId can't be empty!"); return this; }
  @Override public ModifyOneUserImpl repoId(String repoId) {         this.repoId = RepoAssert.notEmpty(repoId,           () -> "repoId can't be empty!"); return this; }
  @Override public ModifyOneUserImpl author(String author) {         this.author = RepoAssert.notEmpty(author,           () -> "author can't be empty!"); return this; }
  @Override public ModifyOneUserImpl message(String message) {       this.message = RepoAssert.notEmpty(message,         () -> "message can't be empty!"); return this; }
  @Override public ModifyOneUserImpl userName(String userName) {     this.userName = Optional.ofNullable(RepoAssert.notEmpty(userName,       () -> "userName can't be empty!")); return this; }
  @Override public ModifyOneUserImpl email(String email) {           this.email = Optional.ofNullable(RepoAssert.notEmpty(email,             () -> "email can't be empty!")); return this; }
  @Override public ModifyOneUserImpl externalId(String externalId) { this.externalId = Optional.ofNullable(externalId); return this; }
  @Override 
  public ModifyOneUserImpl groups(ModType type, List<String> groups) { 
    this.allGroups.addAll(RepoAssert.notEmpty(groups, () -> "groups can't be empty!"));
    if(type == ModType.ADD) {
      groupsToAdd.addAll(groups);
    } else if(type == ModType.REMOVE) {
      groupsToRemove.addAll(groups);
    } else {
      RepoAssert.fail("Unknown modification type: " + type + "!");
    }
    return this; 
   }
  @Override 
  public ModifyOneUserImpl roles(ModType type, List<String> roles) {
    this.allRoles.addAll(RepoAssert.notEmpty(roles, () -> "roles can't be empty!"));
    if(type == ModType.ADD) {
      rolesToAdd.addAll(roles);
    } else if(type == ModType.REMOVE) {
      rolesToRemove.addAll(roles);
    } else {
      RepoAssert.fail("Unknown modification type: " + type + "!");
    }
    return this; 
  }

  @Override
  public Uni<OneUserEnvelope> build() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(userId, () -> "userId can't be empty!");
    
    RepoAssert.notEmptyIfDefined(userName, () -> "userName can't be empty!");
    RepoAssert.notEmptyIfDefined(email, () -> "email can't be empty!");

    return this.state.toOrgState().withTransaction(repoId, this::doInTx);
  }
  
  
  private Uni<OneUserEnvelope> doInTx(OrgRepo tx) {
		// find groups
		final Uni<List<OrgGroup>> groupsUni = this.allGroups.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) : 
			tx.query().groups().findAll(allGroups).collect().asList();
		
		// roles
		final Uni<List<OrgRole>> rolesUni = this.allRoles.isEmpty() ? 
			Uni.createFrom().item(Collections.emptyList()) :
			tx.query().roles().findAll(allRoles).collect().asList();
		
		final Uni<QueryEnvelope<OrgUserGroupsAndRolesWithLog>> userUni = new OrgUserGroupsAndRolesQueryImpl(state).repoId(repoId).get(userId);
	    
		
		// join data
		return Uni.combine().all().unis(userUni, groupsUni, rolesUni).asTuple()
		  .onItem().transformToUni(tuple -> modifyUser(
			  tx,
	      tuple.getItem1(), 
	      tuple.getItem2(),
	      tuple.getItem3()
		  )
		);
  }

  private Uni<OneUserEnvelope> modifyUser(
      OrgRepo tx, 
      QueryEnvelope<OrgUserGroupsAndRolesWithLog> user, 
      List<OrgGroup> groups, 
      List<OrgRole> roles) {
    
    if(user.getStatus() == QueryEnvelopeStatus.ERROR) {
      return Uni.createFrom().item(ImmutableOneUserEnvelope.builder()
          .repoId(repoId)
          .status(CommitResultStatus.ERROR)
          .addAllMessages(user.getMessages())
          .build());
    }
    
    if(groups.size() != this.allGroups.size()) {
      final var found = String.join(", ", groups.stream().map(e -> e.getGroupName()).toList());
      final var expected = String.join(", ", this.allGroups);
      return Uni.createFrom().item(ImmutableOneUserEnvelope.builder()
          .repoId(repoId)
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find all groups: \r\n found: \r\n" + found + " \r\n but requested: \r\n" + expected + "!")
              .build())
          .build());
    }
    
    if(roles.size() != this.allRoles.size()) {
      final var found = String.join(", ", roles.stream().map(e -> e.getRoleName()).toList());
      final var expected = String.join(", ", this.allRoles);
      return Uni.createFrom().item(ImmutableOneUserEnvelope.builder()
          .repoId(repoId)
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .text("Could not find all roles: \r\n found: \r\n" + found + " \r\n but requested: \r\n" + expected + "!")
              .build())
          .build());
    }
    
    final var modify = new BatchForOneUserModify(tx.getRepo().getId(), author, message)
      .externalId(externalId)
      .email(email)
      .current(user.getObjects())
      .userName(userName); 

    
    // Remove or add groups 
    groups.forEach(group -> {
      if( groupsToAdd.contains(group.getGroupName()) ||
          groupsToAdd.contains(group.getId()) ||
          groupsToAdd.contains(group.getExternalId())
      ) {
        modify.updateGroups(ModType.ADD, group);
      }
      
      if( groupsToRemove.contains(group.getGroupName()) ||
          groupsToRemove.contains(group.getId()) ||
          groupsToRemove.contains(group.getExternalId())
       ) {
         modify.updateGroups(ModType.REMOVE, group);
       }
    });
    
    // Remove or add roles 
    roles.forEach(role -> {
      
      if( rolesToAdd.contains(role.getRoleName()) ||
          rolesToAdd.contains(role.getId()) ||
          rolesToAdd.contains(role.getExternalId())
      ) {
        modify.updateRoles(ModType.ADD, role);
      }
      
      if( rolesToAdd.contains(role.getRoleName()) ||
          rolesToAdd.contains(role.getId()) ||
          rolesToAdd.contains(role.getExternalId())
       ) {
         modify.updateRoles(ModType.REMOVE, role);
       }
    });
    
    final OrgBatchForOne batch = modify.create();
     
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
