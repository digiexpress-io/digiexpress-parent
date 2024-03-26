package io.resys.thena.models.org.create;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.resys.thena.api.models.ImmutableMessage;
import io.resys.thena.api.models.ImmutableOrgCommit;
import io.resys.thena.api.models.ImmutableOrgCommitTree;
import io.resys.thena.api.models.ImmutableOrgMember;
import io.resys.thena.api.models.ImmutableOrgMemberRight;
import io.resys.thena.api.models.ImmutableOrgMembership;
import io.resys.thena.api.models.ThenaOrgObject.IsOrgObject;
import io.resys.thena.api.models.ThenaOrgObject.OrgCommitTree;
import io.resys.thena.api.models.ThenaOrgObject.OrgMemberRight;
import io.resys.thena.api.models.ThenaOrgObject.OrgMembership;
import io.resys.thena.api.models.ThenaOrgObject.OrgOperationType;
import io.resys.thena.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.api.models.ThenaOrgObject.OrgRight;
import io.resys.thena.models.git.GitInserts.BatchStatus;
import io.resys.thena.models.org.ImmutableOrgBatchForOne;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOneMemberCreate {

  private final String repoId;
  private final String author;
  private final String message;

  private Map<OrgParty, List<OrgRight>> addUserToGroupRoles;
  private List<OrgParty> addToGroups; 
  private List<OrgRight> addToRoles;  
  private String userName;
  private String email;
  private String externalId;

  public BatchForOneMemberCreate addUserToGroupRoles(Map<OrgParty, List<OrgRight>> groups) { this.addUserToGroupRoles = groups; return this; }
  public BatchForOneMemberCreate addToGroups(List<OrgParty> groups) { this.addToGroups = groups; return this; }
  public BatchForOneMemberCreate addToRoles(List<OrgRight> roles) {    this.addToRoles = roles; return this; }
  public BatchForOneMemberCreate userName(String userName) {     this.userName = userName; return this; }
  public BatchForOneMemberCreate email(String email) {           this.email = email; return this; }
  public BatchForOneMemberCreate externalId(String externalId) { this.externalId = externalId; return this; }
  
  public ImmutableOrgBatchForOne create() {
    RepoAssert.notEmpty(repoId,   () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author,   () -> "author can't be empty!");
    RepoAssert.notEmpty(message,  () -> "message can't be empty!");
    RepoAssert.notEmpty(email,    () -> "email can't be empty!");
    RepoAssert.notEmpty(userName, () -> "userName can't be empty!");
    RepoAssert.notNull(addToGroups,    () -> "addToGroups can't be null!");
    RepoAssert.notNull(addToRoles,     () -> "addToRoles can't be null!");
    RepoAssert.notNull(addUserToGroupRoles, () -> "addUserToGroupRoles can't be null!");
    
    final var commitId = OidUtils.gen();
    final var createdAt = OffsetDateTime.now();
    final var tree = new ArrayList<OrgCommitTree>();
    
    final var user = ImmutableOrgMember.builder()
      .id(OidUtils.gen())
      .commitId(commitId)
      .externalId(externalId)
      .userName(userName)
      .email(email)
      .build();
    tree.add(addToTree(commitId, user));
    
    
    final var memberships = new ArrayList<OrgMembership>();
    for(final var group : this.addToGroups) {
      final var membership = ImmutableOrgMembership.builder()
          .id(OidUtils.gen())
          .partyId(group.getId())
          .memberId(user.getId())
          .commitId(commitId)
          .build();
      tree.add(addToTree(commitId, membership));
      memberships.add(membership);
    }
    
    final var userRoles = new ArrayList<OrgMemberRight>();
    for(final var role : this.addToRoles) {
      final var userRole = ImmutableOrgMemberRight.builder()
          .id(OidUtils.gen())
          .memberId(user.getId())
          .rightId(role.getId())
          .commitId(commitId)
          .build();
      tree.add(addToTree(commitId, userRole));
      userRoles.add(userRole);
    }
    for(final var entry : this.addUserToGroupRoles.entrySet()) {
      for(final var role : entry.getValue()) {
        final var userRole = ImmutableOrgMemberRight.builder()
            .id(OidUtils.gen())
            .memberId(user.getId())
            .rightId(role.getId())
            .partyId(entry.getKey().getId())
            .commitId(commitId)
            .build();
        tree.add(addToTree(commitId, userRole));
        userRoles.add(userRole);
      }
    }
    
    final var logger = new StringBuilder();
    logger.append(System.lineSeparator())
      .append(" | created")
      .append(System.lineSeparator())
      .append("  + commit:          ").append(commitId).append(" tree: ").append(tree.size() + "").append(" entries")
      .append(System.lineSeparator())
      .append("  + user:            ").append(user.getId()).append("::").append(userName)
      .append(System.lineSeparator())
      .append("  + added to groups: ").append(String.join(",", addToGroups.stream().map(g -> g.getPartyName() + "::" + g.getId()).toList()))
      .append(System.lineSeparator())
      .append("  + added to roles:  ").append(String.join(",", addToRoles.stream().map(g -> g.getRightName() + "::" + g.getId()).toList()))
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
      .addMembers(user)
      .memberships(memberships)
      .memberRights(userRoles)
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
