package io.resys.thena.docdb.models.org.modify;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Objects;

import io.resys.thena.docdb.api.actions.OrgCommitActions.ModType;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ImmutableOrgActorStatus;
import io.resys.thena.docdb.api.models.ImmutableOrgCommit;
import io.resys.thena.docdb.api.models.ImmutableOrgCommitTree;
import io.resys.thena.docdb.api.models.ImmutableOrgUser;
import io.resys.thena.docdb.api.models.ImmutableOrgUserMembership;
import io.resys.thena.docdb.api.models.ImmutableOrgUserRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.IsOrgObject;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgCommitTree;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgOperationType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserRole;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserGroupStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserGroupsAndRolesWithLog;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserRoleStatus;
import io.resys.thena.docdb.models.git.GitInserts.BatchStatus;
import io.resys.thena.docdb.models.org.ImmutableOrgBatchForOne;
import io.resys.thena.docdb.support.OidUtils;
import io.resys.thena.docdb.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOneUserModify {
  private final String repoId;
  private final String author;
  private final String message;
  private final List<ModGroup> groups = new ArrayList<>(); 
  private final List<ModRole> roles = new ArrayList<>();
  private final List<OrgCommitTree> tree = new ArrayList<OrgCommitTree>();
  private final List<OrgUserMembership> memberships = new ArrayList<>();
  private final List<OrgUserRole> userRoles = new ArrayList<>();
  
  private final List<OrgActorStatus> actorStatus = new ArrayList<>();
  private final List<String> identifiersForUpdates = new ArrayList<>();
  private OrgUserGroupsAndRolesWithLog current;

  private Optional<String> userName;
  private Optional<String> email;
  private Optional<String> externalId;

  public BatchForOneUserModify current(OrgUserGroupsAndRolesWithLog current) {this.current = current; return this; }
  public BatchForOneUserModify userName(Optional<String> userName) {     this.userName = userName; return this; }
  public BatchForOneUserModify email(Optional<String> email) {           this.email = email; return this; }
  public BatchForOneUserModify externalId(Optional<String> externalId) { this.externalId = externalId; return this; }
  public BatchForOneUserModify updateGroups(ModType type, OrgGroup groups) { 
    this.groups.add(new ModGroup(type, groups));
    return this; 
  }
  public BatchForOneUserModify updateRoles(ModType type, OrgRole role) { 
    this.roles.add(new ModRole(type, role)); 
    return this;
  }
  public ImmutableOrgBatchForOne create() throws NoChangesException {
    RepoAssert.notEmpty(repoId,   () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author,   () -> "author can't be empty!");
    RepoAssert.notEmpty(message,  () -> "message can't be empty!");
    RepoAssert.notNull(current,   () -> "user can't be null!");
    RepoAssert.notEmptyIfDefined(email,    () -> "email can't be empty!");
    RepoAssert.notEmptyIfDefined(userName, () -> "userName can't be empty!");
    
    RepoAssert.notNull(groups,    () -> "groups can't be null!");
    RepoAssert.notNull(roles,     () -> "roles can't be null!");
    
    final var commitId = OidUtils.gen();
    final var createdAt = OffsetDateTime.now();
    
    // user
    final var user = visitUserChanges(commitId);
    
    // groups
    final var groupStatus = this.current.getUserGroupStatus().stream().collect(Collectors.toMap(e -> e.getGroupId(), e -> e));
    for(final var entry : groups) {
      final var status = groupStatus.get(entry.getGroup().getId());
      if(entry.getType() == ModType.ADD) {
        this.visitAddUserToGroup(entry.getGroup(), status, commitId);  
      } else if(entry.getType() == ModType.REMOVE) {
        this.visitRemoveUserFromGroup(entry.getGroup(), status, commitId);
      } else {
        RepoAssert.fail("Unknown modification type: " + entry.getType() + "!"); 
      }
    }
    
    // roles
    final var roleStatus = this.current.getUserRoleStatus().stream().collect(Collectors.toMap(e -> e.getRoleId(), e -> e));
    for(final var entry : roles) {
      final var status = roleStatus.get(entry.getRole().getId());
      if(entry.getType() == ModType.ADD) {
        this.visitAddUserToRole(entry.getRole(), status, commitId);  
      } else if(entry.getType() == ModType.REMOVE) {
        this.visitRemoveUserFromRole(entry.getRole(), status, commitId);
      } else {
        RepoAssert.fail("Unknown modification type: " + entry.getType() + "!"); 
      }
    }
    
    final var logger = new StringBuilder();
    logger.append(System.lineSeparator())
      .append(" | updated")
      .append(System.lineSeparator())
      .append("  + commit:          ").append(commitId).append(" tree: ").append(tree.size() + "").append(" entries")
      .append(System.lineSeparator());
    
    if(!user.isEmpty()) {
      logger
        .append("  + user:            ").append(user.get().getId()).append("::").append(userName)
        .append(System.lineSeparator());
    } 
    logger
      .append("  + added to groups: ").append(String.join(",", groups.stream()
          .filter(g -> g.getType() == ModType.ADD)
          .map(g -> g.getGroup().getGroupName() + "::" + g.getGroup().getId())
          .toList()))
      .append(System.lineSeparator())

      .append("  + removed from groups: ").append(String.join(",", groups.stream()
          .filter(g -> g.getType() == ModType.REMOVE)
          .map(g -> g.getGroup().getGroupName() + "::" + g.getGroup().getId())
          .toList()))
      .append(System.lineSeparator())
      
      .append("  + added to roles:  ").append(String.join(",", roles.stream()
          .filter(g -> g.getType() == ModType.ADD)
          .map(g -> g.getRole().getRoleName() + "::" + g.getRole().getId())
          .toList()))
      
      .append("  + removed from roles:  ").append(String.join(",", roles.stream()
          .filter(g -> g.getType() == ModType.REMOVE)
          .map(g -> g.getRole().getRoleName() + "::" + g.getRole().getId())
          .toList()))
      .append(System.lineSeparator());
    
    final var commit = ImmutableOrgCommit.builder()
        .id(commitId)
        .author(author)
        .message(message)
        .createdAt(createdAt)
        .log(logger.toString())
        .tree(tree)
        .build();

    final var batch = ImmutableOrgBatchForOne.builder()
      .repoId(repoId)
      .status(BatchStatus.OK)
      .commit(commit)
      .users(user.map(u -> Arrays.asList(u)).orElse(Collections.emptyList()))
      .userMemberships(memberships)
      .userRoles(userRoles)
      .actorStatus(actorStatus)
      .identifiersForUpdates(identifiersForUpdates)
      .log(ImmutableMessage.builder().text(logger.toString()).build())
      .build();
    
    // no changes
    if(actorStatus.isEmpty() && userRoles.isEmpty() && memberships.isEmpty() && batch.getUsers().isEmpty()) {
      throw new NoChangesException();
    }
    return batch;
  }
  
  private void visitChangeTree(String commitId, IsOrgObject target, OrgOperationType op) {
    final var entry = ImmutableOrgCommitTree.builder()
        .actorId(target.getId())
        .actorType(target.getDocType().name())
        .commitId(commitId)
        .operationType(op)
        .id(OidUtils.gen())
        .value(JsonObject.mapFrom(target))
        .build();
    tree.add(entry);
  }
  
  private Optional<OrgUser> visitUserChanges(String commitId) {
    final var newState = ImmutableOrgUser.builder()
    .id(current.getUserId())
    .commitId(commitId)
    .externalId(externalId == null ? current.getExternalId() : externalId.get())
    .userName(userName == null ? current.getUserName() : userName.get())
    .email(email == null  ? current.getEmail() : email.get())
    .build();
    
    // no changes
    if( Objects.equal(newState.getEmail(), current.getEmail()) && 
        Objects.equal(newState.getExternalId(), current.getExternalId()) &&
        Objects.equal(newState.getUserName(), current.getUserName())
        ) {
      return Optional.empty();
    }
    visitChangeTree(commitId, newState, OrgOperationType.MOD);
    identifiersForUpdates.add(current.getUserId());
    return Optional.of(newState);
  }

  private void visitRemoveUserFromRole(OrgRole entry, OrgUserRoleStatus status, String commitId) {
    if(status == null) {
      final var roleStatus = ImmutableOrgActorStatus.builder()
          .id(OidUtils.gen())
          .roleId(entry.getId())
          .userId(current.getUserId())
          .commitId(commitId)
          .value(OrgActorStatusType.REMOVED)
          .build();      
      actorStatus.add(roleStatus);
      visitChangeTree(commitId, roleStatus, OrgOperationType.ADD);
      return;
    }
    
    // already removed
    if(status.getStatus() == OrgActorStatusType.REMOVED) {
      return;
    }

    final var roleStatus = ImmutableOrgActorStatus.builder()
        .id(status.getStatusId())
        .roleId(entry.getId())
        .userId(current.getUserId())
        .commitId(commitId)
        .value(OrgActorStatusType.REMOVED)
        .build();
    identifiersForUpdates.add(status.getStatusId());
    actorStatus.add(roleStatus);
    visitChangeTree(commitId, roleStatus, OrgOperationType.MOD);
  }
  
  private void visitAddUserToRole(OrgRole entry, OrgUserRoleStatus status, String commitId) {
    if(!current.getDirectRoleNames().contains(entry.getRoleName())) {
      final var membership = ImmutableOrgUserRole.builder()
          .id(OidUtils.gen())
          .roleId(entry.getId())
          .userId(current.getUserId())
          .commitId(commitId)
          .build();
      userRoles.add(membership);
      visitChangeTree(commitId, membership, OrgOperationType.ADD);
    }
    
    if(status != null) {
      final var roleStatus = ImmutableOrgActorStatus.builder()
          .id(status.getStatusId())
          .roleId(entry.getId())
          .userId(current.getUserId())
          .commitId(commitId)
          .value(OrgActorStatusType.IN_FORCE)
          .build();
      identifiersForUpdates.add(status.getStatusId());
      actorStatus.add(roleStatus);
      visitChangeTree(commitId, roleStatus, OrgOperationType.MOD);
    }
  }
  
  private void visitRemoveUserFromGroup(OrgGroup entry, OrgUserGroupStatus status, String commitId) {
    if(status == null) {
      final var groupStatus = ImmutableOrgActorStatus.builder()
          .id(OidUtils.gen())
          .groupId(entry.getId())
          .userId(current.getUserId())
          .commitId(commitId)
          .value(OrgActorStatusType.REMOVED)
          .build();
      actorStatus.add(groupStatus);
      visitChangeTree(commitId, groupStatus, OrgOperationType.ADD);
      return;
    }
    
    // already removed
    if(status.getStatus() == OrgActorStatusType.REMOVED) {
      return;
    }

    final var groupStatus = ImmutableOrgActorStatus.builder()
        .id(status.getStatusId())
        .groupId(entry.getId())
        .userId(current.getUserId())
        .commitId(commitId)
        .value(OrgActorStatusType.REMOVED)
        .build();
    identifiersForUpdates.add(status.getStatusId());
    actorStatus.add(groupStatus);
    visitChangeTree(commitId, groupStatus, OrgOperationType.MOD);
  }
  
  private void visitAddUserToGroup(OrgGroup entry, OrgUserGroupStatus status, String commitId) {
    if(!current.getDirectGroupNames().contains(entry.getGroupName())) {
      final var membership = ImmutableOrgUserMembership.builder()
          .id(OidUtils.gen())
          .groupId(entry.getId())
          .userId(current.getUserId())
          .commitId(commitId)
          .build();
      memberships.add(membership);
      visitChangeTree(commitId, membership, OrgOperationType.ADD);
    }
    
    if(status != null && status.getStatus() != OrgActorStatusType.IN_FORCE) {
      final var groupStatus = ImmutableOrgActorStatus.builder()
          .id(status.getStatusId())
          .groupId(entry.getId())
          .userId(current.getUserId())
          .commitId(commitId)
          .value(OrgActorStatusType.IN_FORCE)
          .build();
      actorStatus.add(groupStatus);
      identifiersForUpdates.add(status.getStatusId());
      visitChangeTree(commitId, groupStatus, OrgOperationType.MOD);
    }
  }
  
  @Data @RequiredArgsConstructor
  private static class ModRole {
    private final ModType type;
    private final OrgRole role;
  }
  
  @Data @RequiredArgsConstructor
  private static class ModGroup {
    private final ModType type;
    private final OrgGroup group;
  }
  
  public static class NoChangesException extends Exception {
    private static final long serialVersionUID = 3041890960089273165L;
    
  }
}
