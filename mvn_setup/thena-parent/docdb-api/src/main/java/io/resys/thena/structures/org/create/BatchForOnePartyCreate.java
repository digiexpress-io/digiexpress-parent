package io.resys.thena.structures.org.create;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.resys.thena.api.entities.org.ImmutableOrgCommit;
import io.resys.thena.api.entities.org.ImmutableOrgMembership;
import io.resys.thena.api.entities.org.ImmutableOrgParty;
import io.resys.thena.api.entities.org.ImmutableOrgPartyRight;
import io.resys.thena.api.entities.org.OrgActorStatusType;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgDocSubType;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.org.ImmutableOrgBatchForOne;
import io.resys.thena.structures.org.commitlog.OrgCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOnePartyCreate {

  private final String repoId;
  private final String author;
  private final String message;

  private OrgParty parent;
  private List<OrgMember> members; 
  private List<OrgRight> rights;  
  private String partyName;
  private String partyDescription;
  private String externalId;
  private OrgDocSubType partySubType;

  public BatchForOnePartyCreate partySubType(OrgDocSubType partySubType) { this.partySubType = partySubType; return this; }
  public BatchForOnePartyCreate parent(OrgParty parent) { this.parent = parent; return this; }
  public BatchForOnePartyCreate addMembers(List<OrgMember> users) {this.members = users; return this; }
  public BatchForOnePartyCreate addRights(List<OrgRight> roles) {this.rights = roles; return this; }
  public BatchForOnePartyCreate partyName(String groupName) { this.partyName = groupName; return this; }
  public BatchForOnePartyCreate partyDescription(String desc) {	this.partyDescription = desc; return this; }
  public BatchForOnePartyCreate externalId(String externalId) { this.externalId = externalId; return this; }
  
  public ImmutableOrgBatchForOne create() {
    RepoAssert.notEmpty(repoId,   () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author,   () -> "author can't be empty!");
    RepoAssert.notEmpty(message,  () -> "message can't be empty!");
    RepoAssert.notEmpty(partyName,() -> "partyName can't be empty!");
    RepoAssert.notEmpty(partyDescription, () -> "partyDescription can't be empty!");
    RepoAssert.notNull(members,     () -> "users can't be null!");
    RepoAssert.notNull(rights,     () -> "roles can't be null!");
    
    final var commitId = OidUtils.gen();
    final var createdAt = OffsetDateTime.now();
    final var commitBuilder = new OrgCommitBuilder(author, ImmutableOrgCommit.builder()
        .commitId(commitId)
        .commitAuthor(author)
        .commitMessage(message)
        .createdAt(createdAt)
        .commitLog("")
        .build());

    final var group = ImmutableOrgParty.builder()
      .id(OidUtils.gen())
      .commitId(commitId)
      .createdWithCommitId(commitId)
      .externalId(externalId)
      .partyName(partyName)
      .partyDescription(partyDescription)
      .parentId(Optional.ofNullable(parent).map(p -> p.getId()).orElse(null))
      .partySubType(partySubType == null ? OrgDocSubType.NORMAL : partySubType)
      .status(OrgActorStatusType.IN_FORCE)
      .build();
    commitBuilder.add(group);
    
    
    final var memberships = new ArrayList<OrgMembership>();
    for(final var user : this.members) {
      final var membership = ImmutableOrgMembership.builder()
          .id(OidUtils.gen())
          .partyId(group.getId())
          .memberId(user.getId())
          .commitId(commitId)
          .build();
      commitBuilder.add(membership);
      memberships.add(membership);
    }
    
    final var groupRoles = new ArrayList<OrgPartyRight>();
    for(final var role : this.rights) {
      final var groupRole = ImmutableOrgPartyRight.builder()
          .id(OidUtils.gen())
          .partyId(group.getId())
          .rightId(role.getId())
          .commitId(commitId)
          .build();
      commitBuilder.add(groupRole);
      groupRoles.add(groupRole);
    }
    
    final var commit = commitBuilder.close();

    final var batch = ImmutableOrgBatchForOne.builder()
      .repoId(repoId)
      .status(BatchStatus.OK)
      .commit(commit.getItem1())
      .commitTrees(commit.getItem2())
      .addParties(group)
      .memberships(memberships)
      .partyRights(groupRoles)
      .log(commit.getItem1().getCommitLog())
      .build();
    return batch;
  }
  
}
