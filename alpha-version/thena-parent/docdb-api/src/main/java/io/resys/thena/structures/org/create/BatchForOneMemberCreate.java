package io.resys.thena.structures.org.create;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.resys.thena.api.entities.org.ImmutableOrgCommit;
import io.resys.thena.api.entities.org.ImmutableOrgMember;
import io.resys.thena.api.entities.org.ImmutableOrgMemberRight;
import io.resys.thena.api.entities.org.ImmutableOrgMembership;
import io.resys.thena.api.entities.org.OrgActorStatusType;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.org.ImmutableOrgBatchForOne;
import io.resys.thena.structures.org.commitlog.OrgCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
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
    final var commitBuilder = new OrgCommitBuilder(author, ImmutableOrgCommit.builder()
        .commitId(commitId)
        .commitAuthor(author)
        .commitMessage(message)
        .createdAt(createdAt)
        .commitLog("")
        .build());
    
    final var user = ImmutableOrgMember.builder()
      .id(OidUtils.gen())
      .createdWithCommitId(commitId)
      .commitId(commitId)
      .externalId(externalId)
      .userName(userName)
      .email(email)
      .status(OrgActorStatusType.IN_FORCE)
      .build();
    commitBuilder.add(user);
    
    
    final var memberships = new ArrayList<OrgMembership>();
    for(final var group : this.addToParty) {
      final var membership = ImmutableOrgMembership.builder()
          .id(OidUtils.gen())
          .partyId(group.getId())
          .memberId(user.getId())
          .commitId(commitId)
          .build();
      commitBuilder.add(membership);
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
      commitBuilder.add(userRole);
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
        commitBuilder.add(userRole);
        userRoles.add(userRole);
      }
    }

    final var commit = commitBuilder.close();
    

    final var batch = ImmutableOrgBatchForOne.builder()
      .repoId(repoId)
      .status(BatchStatus.OK)
      .commit(commit.getItem1())
      .addAllCommitTrees(commit.getItem2())
      .addMembers(user)
      .memberships(memberships)
      .memberRights(userRoles)
      .log(commit.getItem1().getCommitLog())
      .build();
    return batch;
  }
}
