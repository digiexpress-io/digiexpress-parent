package io.resys.thena.structures.org.create;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.resys.thena.api.entities.org.ImmutableOrgCommit;
import io.resys.thena.api.entities.org.ImmutableOrgCommitTree;
import io.resys.thena.api.entities.org.ImmutableOrgMembership;
import io.resys.thena.api.entities.org.ImmutableOrgParty;
import io.resys.thena.api.entities.org.ImmutableOrgPartyRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgObject;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgCommitTree;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMember;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMembership;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgOperationType;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgParty;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgRight;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.structures.git.GitInserts.BatchStatus;
import io.resys.thena.structures.org.ImmutableOrgBatchForOne;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOnePartyCreate {

  private final String repoId;
  private final String author;
  private final String message;

  private OrgParty parent;
  private List<OrgMember> users; 
  private List<OrgRight> roles;  
  private String groupName;
  private String groupDescription;
  private String externalId;

  public BatchForOnePartyCreate parent(OrgParty parent) { 		this.parent = parent; return this; }
  public BatchForOnePartyCreate users(List<OrgMember> users) { 	this.users = users; return this; }
  public BatchForOnePartyCreate roles(List<OrgRight> roles) {    this.roles = roles; return this; }
  public BatchForOnePartyCreate groupName(String groupName) {   this.groupName = groupName; return this; }
  public BatchForOnePartyCreate groupDescription(String desc) {	this.groupDescription = desc; return this; }
  public BatchForOnePartyCreate externalId(String externalId) { this.externalId = externalId; return this; }
  
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
    
    final var group = ImmutableOrgParty.builder()
      .id(OidUtils.gen())
      .commitId(commitId)
      .externalId(externalId)
      .partyName(groupName)
      .partyDescription(groupDescription)
      .parentId(Optional.ofNullable(parent).map(p -> p.getId()).orElse(null))
      .build();
    tree.add(addToTree(commitId, group));
    
    
    final var memberships = new ArrayList<OrgMembership>();
    for(final var user : this.users) {
      final var membership = ImmutableOrgMembership.builder()
          .id(OidUtils.gen())
          .partyId(group.getId())
          .memberId(user.getId())
          .commitId(commitId)
          .build();
      tree.add(addToTree(commitId, membership));
      memberships.add(membership);
    }
    
    final var groupRoles = new ArrayList<OrgPartyRight>();
    for(final var role : this.roles) {
      final var groupRole = ImmutableOrgPartyRight.builder()
          .id(OidUtils.gen())
          .partyId(group.getId())
          .rightId(role.getId())
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
      .append("  + added members:    ").append(String.join(",", users.stream().map(g -> g.getUserName() + "::" + g.getId()).toList()))
      .append(System.lineSeparator())
      .append("  + added to rights: ").append(String.join(",", roles.stream().map(g -> g.getRightName() + "::" + g.getId()).toList()))
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
      .addParties(group)
      .memberships(memberships)
      .partyRights(groupRoles)
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
