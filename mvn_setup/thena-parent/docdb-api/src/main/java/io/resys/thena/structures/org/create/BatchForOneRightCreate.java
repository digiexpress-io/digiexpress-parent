package io.resys.thena.structures.org.create;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import io.resys.thena.api.entities.org.ImmutableOrgCommit;
import io.resys.thena.api.entities.org.ImmutableOrgMemberRight;
import io.resys.thena.api.entities.org.ImmutableOrgPartyRight;
import io.resys.thena.api.entities.org.ImmutableOrgRight;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgDocSubType;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.org.ImmutableOrgBatchForOne;
import io.resys.thena.structures.org.commitlog.OrgCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOneRightCreate {

  private final String repoId;
  private final String author;
  private final String message;

  private List<OrgParty> parties; 
  private List<OrgMember> users;  
  private String rightName;
  private String rightDesc;
  private String externalId;
  private OrgDocSubType rightSubType;

  public BatchForOneRightCreate rightSubType(OrgDocSubType rightSubType) { this.rightSubType = rightSubType; return this; }
  public BatchForOneRightCreate parties(List<OrgParty> parties) { 		this.parties = parties; return this; }
  public BatchForOneRightCreate users(List<OrgMember> users) {    		this.users = users; return this; }
  public BatchForOneRightCreate rightName(String rightName) {     		this.rightName = rightName; return this; }
  public BatchForOneRightCreate rightDescription(String rightDescription) { this.rightDesc = rightDescription; return this; }
  public BatchForOneRightCreate externalId(String externalId) { 		this.externalId = externalId; return this; }
  
  public ImmutableOrgBatchForOne create() {
    RepoAssert.notEmpty(repoId,   () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author,   () -> "author can't be empty!");
    RepoAssert.notEmpty(message,  () -> "message can't be empty!");
    RepoAssert.notEmpty(rightName, () -> "rightName can't be empty!");
    RepoAssert.notEmpty(rightDesc, () -> "rightDescription can't be empty!");
    RepoAssert.notNull(parties,   () -> "parties can't be null!");
    RepoAssert.notNull(users,     () -> "roles can't be null!");
    
    final var commitId = OidUtils.gen();
    final var createdAt = OffsetDateTime.now();
    final var commitBuilder = new OrgCommitBuilder(author, ImmutableOrgCommit.builder()
        .commitId(commitId)
        .commitAuthor(author)
        .commitMessage(message)
        .createdAt(createdAt)
        .commitLog("")
        .build());
    
    
    final var role = ImmutableOrgRight.builder()
      .id(OidUtils.gen())
      .commitId(commitId)
      .createdWithCommitId(commitId)
      .externalId(externalId)
      .rightName(rightName)
      .rightDescription(rightDesc)
      .rightSubType(rightSubType == null ? OrgDocSubType.NORMAL : rightSubType)
      .build();
    commitBuilder.add(role);
    
    
    final var groupRoles = new ArrayList<OrgPartyRight>();
    for(final var group : this.parties) {
      final var membership = ImmutableOrgPartyRight.builder()
          .id(OidUtils.gen())
          .partyId(group.getId())
          .rightId(role.getId())
          .commitId(commitId)
          .build();
      commitBuilder.add(membership);
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
      commitBuilder.add(userRole);
      userRoles.add(userRole);
    }
  
    final var commit = commitBuilder.close();
  
    final var batch = ImmutableOrgBatchForOne.builder()
      .repoId(repoId)
      .status(BatchStatus.OK)
      .commit(commit.getItem1())
      .commitTrees(commit.getItem2())
      .addRights(role)
      .memberRights(userRoles)
      .partyRights(groupRoles)
      .log(commit.getItem1().getCommitLog())
      .build();
    return batch;
  }
  
}
