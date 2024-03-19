package io.resys.thena.docdb.models.org.create;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ImmutableOrgCommit;
import io.resys.thena.docdb.api.models.ImmutableOrgCommitTree;
import io.resys.thena.docdb.api.models.ImmutableOrgGroup;
import io.resys.thena.docdb.api.models.ImmutableOrgPartyRight;
import io.resys.thena.docdb.api.models.ImmutableOrgMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.IsOrgObject;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgCommitTree;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgOperationType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMembership;
import io.resys.thena.docdb.models.git.GitInserts.BatchStatus;
import io.resys.thena.docdb.models.org.ImmutableOrgBatchForOne;
import io.resys.thena.docdb.support.OidUtils;
import io.resys.thena.docdb.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOneGroupCreate {

  private final String repoId;
  private final String author;
  private final String message;

  private OrgGroup parent;
  private List<OrgMember> users; 
  private List<OrgRole> roles;  
  private String groupName;
  private String groupDescription;
  private String externalId;

  public BatchForOneGroupCreate parent(OrgGroup parent) { 		this.parent = parent; return this; }
  public BatchForOneGroupCreate users(List<OrgMember> users) { 	this.users = users; return this; }
  public BatchForOneGroupCreate roles(List<OrgRole> roles) {    this.roles = roles; return this; }
  public BatchForOneGroupCreate groupName(String groupName) {   this.groupName = groupName; return this; }
  public BatchForOneGroupCreate groupDescription(String desc) {	this.groupDescription = desc; return this; }
  public BatchForOneGroupCreate externalId(String externalId) { this.externalId = externalId; return this; }
  
  public ImmutableOrgBatchForOne create() {
    RepoAssert.notEmpty(repoId,   () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author,   () -> "author can't be empty!");
    RepoAssert.notEmpty(message,  () -> "message can't be empty!");
    RepoAssert.notEmpty(groupName,() -> "groupName can't be empty!");
    RepoAssert.notEmpty(groupDescription, () -> "groupDescription can't be empty!");
    RepoAssert.notNull(users,     () -> "users can't be null!");
    RepoAssert.notNull(roles,     () -> "roles can't be null!");
    
    final var commitId = OidUtils.gen();
    final var createdAt = OffsetDateTime.now();
    final var tree = new ArrayList<OrgCommitTree>();
    
    final var group = ImmutableOrgGroup.builder()
      .id(OidUtils.gen())
      .commitId(commitId)
      .externalId(externalId)
      .groupName(groupName)
      .groupDescription(groupDescription)
      .parentId(Optional.ofNullable(parent).map(p -> p.getId()).orElse(null))
      .build();
    tree.add(addToTree(commitId, group));
    
    
    final var memberships = new ArrayList<OrgMembership>();
    for(final var user : this.users) {
      final var membership = ImmutableOrgMembership.builder()
          .id(OidUtils.gen())
          .groupId(group.getId())
          .userId(user.getId())
          .commitId(commitId)
          .build();
      tree.add(addToTree(commitId, membership));
      memberships.add(membership);
    }
    
    final var groupRoles = new ArrayList<OrgPartyRight>();
    for(final var role : this.roles) {
      final var groupRole = ImmutableOrgPartyRight.builder()
          .id(OidUtils.gen())
          .groupId(group.getId())
          .roleId(role.getId())
          .commitId(commitId)
          .build();
      tree.add(addToTree(commitId, groupRole));
      groupRoles.add(groupRole);
    }
    
    final var logger = new StringBuilder();
    logger
      .append(System.lineSeparator())
      .append(" | created")
      .append(System.lineSeparator())
      .append("  + commit:         ").append(commitId).append(" tree: ").append(tree.size() + "").append(" entries")
      .append(System.lineSeparator())
      .append("  + group:          ").append(group.getId()).append("::").append(groupName)
      .append(System.lineSeparator())
      .append("  + added users:    ").append(String.join(",", users.stream().map(g -> g.getUserName() + "::" + g.getId()).toList()))
      .append(System.lineSeparator())
      .append("  + added to roles: ").append(String.join(",", roles.stream().map(g -> g.getRoleName() + "::" + g.getId()).toList()))
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
      .addGroups(group)
      .userMemberships(memberships)
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
