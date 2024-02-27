package io.resys.thena.docdb.models.org.support;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ImmutableOrgCommit;
import io.resys.thena.docdb.api.models.ImmutableOrgUser;
import io.resys.thena.docdb.api.models.ImmutableOrgUserMembership;
import io.resys.thena.docdb.api.models.ImmutableOrgUserRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserRole;
import io.resys.thena.docdb.models.git.GitInserts.BatchStatus;
import io.resys.thena.docdb.models.git.commits.CommitLogger;
import io.resys.thena.docdb.models.org.ImmutableOrgBatchForOne;
import io.resys.thena.docdb.support.OidUtils;
import io.resys.thena.docdb.support.RepoAssert;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOneUserCreate {

  private final String repoId;
  private final String author;
  private final String message;

  private List<OrgGroup> groups; 
  private List<OrgRole> roles;  
  private String userName;
  private String email;
  private String externalId;

  public BatchForOneUserCreate groups(List<OrgGroup> groups) { this.groups = groups; return this; }
  public BatchForOneUserCreate roles(List<OrgRole> roles) {    this.roles = roles; return this; }
  public BatchForOneUserCreate userName(String userName) {     this.userName = userName; return this; }
  public BatchForOneUserCreate email(String email) {           this.email = email; return this; }
  public BatchForOneUserCreate externalId(String externalId) { this.externalId = externalId; return this; }
  
  public ImmutableOrgBatchForOne create() {
    RepoAssert.notEmpty(repoId,   () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author,   () -> "author can't be empty!");
    RepoAssert.notEmpty(message,  () -> "message can't be empty!");
    RepoAssert.notEmpty(email,    () -> "email can't be empty!");
    RepoAssert.notEmpty(userName, () -> "userName can't be empty!");
    RepoAssert.notNull(groups,    () -> "groups can't be null!");
    RepoAssert.notNull(roles,     () -> "roles can't be null!");
    
    final var commitId = OidUtils.gen();
    final var createdAt = Instant.now();
    
    //
    final var user = ImmutableOrgUser.builder()
      .id(OidUtils.gen())
      .externalId(externalId)
      .userName(userName)
      .email(email)
      .version(commitId)
      .build();
    
    final var memberships = new ArrayList<OrgUserMembership>();
    for(final var group : this.groups) {
      final var membership = ImmutableOrgUserMembership.builder()
          .id(OidUtils.gen())
          .groupId(group.getId())
          .userId(user.getId())
          .commitId(commitId)
          .build();
      memberships.add(membership);
    }
    
    final var userRoles = new ArrayList<OrgUserRole>();
    for(final var role : this.roles) {
      final var useRole = ImmutableOrgUserRole.builder()
          .id(OidUtils.gen())
          .userId(user.getId())
          .roleId(role.getId())
          .commitId(commitId)
          .build();
      userRoles.add(useRole);
    }
    
    final var logger = new CommitLogger();
    logger
      .append(" | created")
      .append(System.lineSeparator())
      .append("  + commit:        ").append(commitId)
      .append(System.lineSeparator())
      .append("  + user:        ").append(user.getId())
      .append(System.lineSeparator())
      .append("  + added to groups: ").append(String.join(",", groups.stream().map(g -> g.getGroupName() + "::" + g.getId()).toList()))
      .append(System.lineSeparator())
      .append("  + added to roles: ").append(String.join(",", roles.stream().map(g -> g.getRoleName() + "::" + g.getId()).toList()))
      .append(System.lineSeparator());
    
    final var commit = ImmutableOrgCommit.builder()
        .id(commitId)
        .author(author)
        .message(message)
        .createdAt(createdAt)
        .log(logger.toString())
        .build();

    final var batch = ImmutableOrgBatchForOne.builder()
      .repoId(repoId)
      .status(BatchStatus.OK)
      .commit(commit)
      .addUsers(user)
      .userMemberships(memberships)
      .userRoles(userRoles)
      .log(ImmutableMessage.builder().text(logger.toString()).build())
      .build();
    return batch;
  }
}
