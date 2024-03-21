package io.resys.thena.docdb.models.org.create;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ImmutableOrgCommit;
import io.resys.thena.docdb.api.models.ImmutableOrgCommitTree;
import io.resys.thena.docdb.api.models.ImmutableOrgMemberRight;
import io.resys.thena.docdb.api.models.ImmutableOrgPartyRight;
import io.resys.thena.docdb.api.models.ImmutableOrgRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.IsOrgObject;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgCommitTree;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgOperationType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.docdb.models.git.GitInserts.BatchStatus;
import io.resys.thena.docdb.models.org.ImmutableOrgBatchForOne;
import io.resys.thena.docdb.support.OidUtils;
import io.resys.thena.docdb.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOneRightCreate {

  private final String repoId;
  private final String author;
  private final String message;

  private List<OrgParty> parties; 
  private List<OrgMember> users;  
  private String roleName;
  private String roleDesc;
  private String externalId;

  public BatchForOneRightCreate parties(List<OrgParty> parties) { 		this.parties = parties; return this; }
  public BatchForOneRightCreate users(List<OrgMember> users) {    		this.users = users; return this; }
  public BatchForOneRightCreate partyName(String roleName) {     		this.roleName = roleName; return this; }
  public BatchForOneRightCreate partyDescription(String roleDesc) { this.roleDesc = roleDesc; return this; }
  public BatchForOneRightCreate externalId(String externalId) { 		this.externalId = externalId; return this; }
  
  public ImmutableOrgBatchForOne create() {
    RepoAssert.notEmpty(repoId,   () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author,   () -> "author can't be empty!");
    RepoAssert.notEmpty(message,  () -> "message can't be empty!");
    RepoAssert.notEmpty(roleName, () -> "roleName can't be empty!");
    RepoAssert.notEmpty(roleDesc, () -> "roleDesc can't be empty!");
    RepoAssert.notNull(parties,    () -> "parties can't be null!");
    RepoAssert.notNull(users,     () -> "roles can't be null!");
    
    final var commitId = OidUtils.gen();
    final var createdAt = OffsetDateTime.now();
    final var tree = new ArrayList<OrgCommitTree>();
    
    final var role = ImmutableOrgRight.builder()
      .id(OidUtils.gen())
      .commitId(commitId)
      .externalId(externalId)
      .rightName(roleName)
      .rightDescription(roleDesc)
      .build();
    tree.add(addToTree(commitId, role));
    
    
    final var groupRoles = new ArrayList<OrgPartyRight>();
    for(final var group : this.parties) {
      final var membership = ImmutableOrgPartyRight.builder()
          .id(OidUtils.gen())
          .partyId(group.getId())
          .rightId(role.getId())
          .commitId(commitId)
          .build();
      tree.add(addToTree(commitId, membership));
      groupRoles.add(membership);
    }
    
    final var userRoles = new ArrayList<OrgMemberRight>();
    for(final var user : this.users) {
      final var userRole = ImmutableOrgMemberRight.builder()
          .id(OidUtils.gen())
          .memberId(user.getId())
          .rightId(role.getId())
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
      .append("  + added to parties: ").append(String.join(",", parties.stream().map(g -> g.getPartyName() + "::" + g.getId()).toList()))
      .append(System.lineSeparator())
      .append("  + added to members:  ").append(String.join(",", users.stream().map(g -> g.getUserName() + "::" + g.getId()).toList()))
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
      .addRights(role)
      .memberRights(userRoles)
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
