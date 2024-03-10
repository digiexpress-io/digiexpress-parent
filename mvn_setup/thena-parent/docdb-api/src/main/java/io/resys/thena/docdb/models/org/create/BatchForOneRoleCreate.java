package io.resys.thena.docdb.models.org.create;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ImmutableOrgCommit;
import io.resys.thena.docdb.api.models.ImmutableOrgCommitTree;
import io.resys.thena.docdb.api.models.ImmutableOrgGroupRole;
import io.resys.thena.docdb.api.models.ImmutableOrgRole;
import io.resys.thena.docdb.api.models.ImmutableOrgUserRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.IsOrgObject;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgCommitTree;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroupRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgOperationType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserRole;
import io.resys.thena.docdb.models.git.GitInserts.BatchStatus;
import io.resys.thena.docdb.models.org.ImmutableOrgBatchForOne;
import io.resys.thena.docdb.support.OidUtils;
import io.resys.thena.docdb.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOneRoleCreate {

  private final String repoId;
  private final String author;
  private final String message;

  private List<OrgGroup> groups; 
  private List<OrgUser> users;  
  private String roleName;
  private String roleDesc;
  private String externalId;

  public BatchForOneRoleCreate groups(List<OrgGroup> groups) { 		this.groups = groups; return this; }
  public BatchForOneRoleCreate users(List<OrgUser> users) {    		this.users = users; return this; }
  public BatchForOneRoleCreate roleName(String roleName) {     		this.roleName = roleName; return this; }
  public BatchForOneRoleCreate roleDescription(String roleDesc) { this.roleDesc = roleDesc; return this; }
  public BatchForOneRoleCreate externalId(String externalId) { 		this.externalId = externalId; return this; }
  
  public ImmutableOrgBatchForOne create() {
    RepoAssert.notEmpty(repoId,   () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author,   () -> "author can't be empty!");
    RepoAssert.notEmpty(message,  () -> "message can't be empty!");
    RepoAssert.notEmpty(roleName, () -> "roleName can't be empty!");
    RepoAssert.notEmpty(roleDesc, () -> "roleDesc can't be empty!");
    RepoAssert.notNull(groups,    () -> "groups can't be null!");
    RepoAssert.notNull(users,     () -> "roles can't be null!");
    
    final var commitId = OidUtils.gen();
    final var createdAt = OffsetDateTime.now();
    final var tree = new ArrayList<OrgCommitTree>();
    
    final var role = ImmutableOrgRole.builder()
      .id(OidUtils.gen())
      .commitId(commitId)
      .externalId(externalId)
      .roleName(roleName)
      .roleDescription(roleDesc)
      .build();
    tree.add(addToTree(commitId, role));
    
    
    final var groupRoles = new ArrayList<OrgGroupRole>();
    for(final var group : this.groups) {
      final var membership = ImmutableOrgGroupRole.builder()
          .id(OidUtils.gen())
          .groupId(group.getId())
          .roleId(role.getId())
          .commitId(commitId)
          .build();
      tree.add(addToTree(commitId, membership));
      groupRoles.add(membership);
    }
    
    final var userRoles = new ArrayList<OrgUserRole>();
    for(final var user : this.users) {
      final var userRole = ImmutableOrgUserRole.builder()
          .id(OidUtils.gen())
          .userId(user.getId())
          .roleId(role.getId())
          .commitId(commitId)
          .build();
      tree.add(addToTree(commitId, userRole));
      userRoles.add(userRole);
    }
    
    final var logger = new StringBuilder();
    logger
    	.append(System.lineSeparator())
      .append(" | created")
      .append(System.lineSeparator())
      .append("  + commit:          ").append(commitId).append(" tree: ").append(tree.size() + "").append(" entries")
      .append(System.lineSeparator())
      .append("  + role:            ").append(role.getId()).append("::").append(roleName)
      .append(System.lineSeparator())
      .append("  + added to groups: ").append(String.join(",", groups.stream().map(g -> g.getGroupName() + "::" + g.getId()).toList()))
      .append(System.lineSeparator())
      .append("  + added to users:  ").append(String.join(",", users.stream().map(g -> g.getUserName() + "::" + g.getId()).toList()))
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
      .addRoles(role)
      .userRoles(userRoles)
      .groupRoles(groupRoles)
      .log(ImmutableMessage.builder().text(logger.toString()).build())
      .build();
    return batch;
  }
  
  public OrgCommitTree addToTree(String commitId, IsOrgObject target) {
    return ImmutableOrgCommitTree.builder()
        .actorId(target.getId())
        .actorType(target.getDocType().name())
        .commitId(commitId)
        .operationType(OrgOperationType.ADD)
        .id(OidUtils.gen())
        .value(JsonObject.mapFrom(target))
        .build();
  }
}