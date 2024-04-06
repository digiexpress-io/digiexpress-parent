package io.resys.thena.structures.org.create;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.resys.thena.api.entities.org.ImmutableOrgCommit;
import io.resys.thena.api.entities.org.ImmutableOrgCommitTree;
import io.resys.thena.api.entities.org.ImmutableOrgMember;
import io.resys.thena.api.entities.org.ImmutableOrgMemberRight;
import io.resys.thena.api.entities.org.ImmutableOrgMembership;
import io.resys.thena.api.entities.org.OrgCommitTree;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgObject;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.org.ImmutableOrgBatchForOne;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOneMemberCreate {

  private final String repoId;
  private final String author;
  private final String message;

  private Map<OrgParty, List<OrgRight>> addToPartyWithRights;
  private List<OrgParty> addToParty; 
  private List<OrgRight> addToRights;  
  private String userName;
  private String email;
  private String externalId;

  public BatchForOneMemberCreate addToPartyWithRights(Map<OrgParty, List<OrgRight>> groups) { this.addToPartyWithRights = groups; return this; }
  public BatchForOneMemberCreate addToParty(List<OrgParty> groups) { this.addToParty = groups; return this; }
  public BatchForOneMemberCreate addToRights(List<OrgRight> roles) {    this.addToRights = roles; return this; }
  public BatchForOneMemberCreate userName(String userName) {     this.userName = userName; return this; }
  public BatchForOneMemberCreate email(String email) {           this.email = email; return this; }
  public BatchForOneMemberCreate externalId(String externalId) { this.externalId = externalId; return this; }
  
  public ImmutableOrgBatchForOne create() {
    RepoAssert.notEmpty(repoId,   () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author,   () -> "author can't be empty!");
    RepoAssert.notEmpty(message,  () -> "message can't be empty!");
    RepoAssert.notEmpty(email,    () -> "email can't be empty!");
    RepoAssert.notEmpty(userName, () -> "userName can't be empty!");
    RepoAssert.notNull(addToParty,    () -> "addToParty can't be null!");
    RepoAssert.notNull(addToRights,     () -> "addToRights can't be null!");
    RepoAssert.notNull(addToPartyWithRights, () -> "addToPartyWithRights can't be null!");
    
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
    for(final var group : this.addToParty) {
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
    for(final var role : this.addToRights) {
      final var userRole = ImmutableOrgMemberRight.builder()
          .id(OidUtils.gen())
          .memberId(user.getId())
          .rightId(role.getId())
          .commitId(commitId)
          .build();
      tree.add(addToTree(commitId, userRole));
      userRoles.add(userRole);
    }
    for(final var entry : this.addToPartyWithRights.entrySet()) {
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
      .append("  + added to groups: ").append(String.join(",", addToParty.stream().map(g -> g.getPartyName() + "::" + g.getId()).toList()))
      .append(System.lineSeparator())
      .append("  + added to roles:  ").append(String.join(",", addToRights.stream().map(g -> g.getRightName() + "::" + g.getId()).toList()))
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
        .operationType(OrgCommitTree.OrgOperationType.ADD)
        .id(OidUtils.gen())
        .value(JsonObject.mapFrom(target))
        .build();
  }
}
