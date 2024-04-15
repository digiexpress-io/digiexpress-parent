package io.resys.thena.structures.org.modify;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Objects;

import io.resys.thena.api.actions.OrgCommitActions.ModType;
import io.resys.thena.api.entities.org.ImmutableOrgActorStatus;
import io.resys.thena.api.entities.org.ImmutableOrgCommit;
import io.resys.thena.api.entities.org.ImmutableOrgMember;
import io.resys.thena.api.entities.org.ImmutableOrgMemberRight;
import io.resys.thena.api.entities.org.ImmutableOrgMembership;
import io.resys.thena.api.entities.org.OrgActorStatus;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.org.ImmutableOrgBatchForOne;
import io.resys.thena.structures.org.commitlog.OrgCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOneMemberModify {
  private final String repoId;
  private final String author;
  private final String message;
  private final List<ModGroup> modifyMemberships = new ArrayList<>(); 
  private final List<ModRole> modifyMemberRights = new ArrayList<>();
  private final Map<OrgParty, List<OrgRight>> addToPartyWithRights = new LinkedHashMap<>();
  
  private OrgMember current;
  private OrgCommitBuilder commitBuilder;
  private final ImmutableOrgBatchForOne.Builder batch = ImmutableOrgBatchForOne.builder();
  private final List<OrgMemberRight> currentMemberRights = new ArrayList<>();
  private final List<OrgMembership> currentMemberships = new ArrayList<>();
  private final List<OrgActorStatus> currentActorStatus = new ArrayList<>();


  private Optional<String> newMemberName;
  private Optional<String> newEmail;
  private Optional<String> newExternalId;
  private OrgActorStatus.OrgActorStatusType newStatus;
  
  public BatchForOneMemberModify current(OrgMember current) {
    this.current = current; 
    return this; 
  }
  public BatchForOneMemberModify currentMemberships(List<OrgMembership> currentMemberships) { 
    this.currentMemberships.addAll(currentMemberships);
    return this; 
  }
  public BatchForOneMemberModify currentMemberRights(List<OrgMemberRight> currentMemberRights) { 
    this.currentMemberRights.addAll(currentMemberRights);
    return this; 
  }
  public BatchForOneMemberModify currentStatus(List<OrgActorStatus> status) { 
    this.currentActorStatus.addAll(status);
    return this; 
  }
  public BatchForOneMemberModify newUserName(Optional<String> memberName) {
    this.newMemberName = memberName; 
    return this; 
  }
  public BatchForOneMemberModify newEmail(Optional<String> email) {
    this.newEmail = email; 
    return this; 
  }
  public BatchForOneMemberModify newExternalId(Optional<String> externalId) { 
    this.newExternalId = externalId;
    return this; 
  }
  public BatchForOneMemberModify newStatus(OrgActorStatus.OrgActorStatusType newStatus) { 
    this.newStatus = newStatus;
    return this; 
  }
  public BatchForOneMemberModify modifyMembership(ModType type, OrgParty parties) { 
    this.modifyMemberships.add(new ModGroup(type, parties));
    return this; 
  }
  public BatchForOneMemberModify modifyMemberRights(ModType type, OrgRight role) { 
    this.modifyMemberRights.add(new ModRole(type, role)); 
    return this;
  }
  public BatchForOneMemberModify modifyMemberRightsInParty(ModType type, Map<OrgParty, List<OrgRight>> addUserToGroupRoles) { 
    if(type == ModType.ADD) {
      this.addToPartyWithRights.putAll(addUserToGroupRoles);
    } else {
      throw new RuntimeException("not implemented!!");
    }
    return this;
  }
  public ImmutableOrgBatchForOne create() throws NoMemberChangesException {
    RepoAssert.notEmpty(repoId,   () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author,   () -> "author can't be empty!");
    RepoAssert.notEmpty(message,  () -> "message can't be empty!");
    RepoAssert.notNull(current,   () -> "member can't be null!");
    RepoAssert.notEmptyIfDefined(newEmail,    () -> "email can't be empty!");
    RepoAssert.notEmptyIfDefined(newMemberName, () -> "memberName can't be empty!");
    
    RepoAssert.notNull(modifyMemberships,    () -> "parties can't be null!");
    RepoAssert.notNull(modifyMemberRights,     () -> "rights can't be null!");
    
    final var commitId = OidUtils.gen();
    final var createdAt = OffsetDateTime.now();
    this.commitBuilder = new OrgCommitBuilder(author, ImmutableOrgCommit.builder()
        .commitId(commitId)
        .commitAuthor(author)
        .commitMessage(message)
        .createdAt(createdAt)
        .commitLog("")
        .build());
    
    
    // member
    visitMemberChanges(commitId);
    
    // member status
    final var memberStatus = this.currentActorStatus.stream().filter(g -> g.getRightId() == null && g.getPartyId() == null).findFirst();
    visitMemberStatus(commitId, memberStatus.orElse(null));
    
    // parties
    final var memebrShipInParty = this.currentMemberships.stream().filter(g -> g.getPartyId() != null).collect(Collectors.toMap(e -> e.getPartyId(), e -> e));
    final var statusInParty = this.currentActorStatus.stream().filter(g -> g.getPartyId() != null && g.getRightId() == null).collect(Collectors.toMap(e -> e.getPartyId(), e -> e));
    for(final var entry : modifyMemberships) {
      final var status = statusInParty.get(entry.getGroup().getId());
      final var membership = memebrShipInParty.get(entry.getGroup().getId());
      if(entry.getType() == ModType.ADD) {
        this.visitAddMembership(entry.getGroup(), membership, status, commitId);  
      } else if(entry.getType() == ModType.DISABLED) {
        this.visitDisableMembership(entry.getGroup(), membership, status, commitId);
      } else if(entry.getType() == ModType.REMOVE) {
        this.visitRemoveMembership(entry.getGroup(), membership, status, commitId);
      } else {
        RepoAssert.fail("Unknown modification type: " + entry.getType() + "!"); 
      }
    }
    
    // rights
    final var statusInRights = this.currentActorStatus.stream().filter(g -> g.getRightId() != null && g.getPartyId() == null).collect(Collectors.toMap(e -> e.getRightId(), e -> e));
    final var rightsByRightIdId = this.currentMemberRights.stream().collect(Collectors.groupingBy(OrgMemberRight::getRightId));
    for(final var entry : modifyMemberRights) {
      final var status = statusInRights.get(entry.getRole().getId());
      final var rights = Optional.ofNullable(rightsByRightIdId.get(entry.getRole().getId())).orElse(Collections.emptyList());
      
      if(entry.getType() == ModType.ADD) {
        this.visitAddRightForMember(entry.getRole(), rights, status, Optional.empty(), commitId);  
      } else if(entry.getType() == ModType.DISABLED) {
        this.visitDisableRightForMember(entry.getRole(), rights, status, Optional.empty(), commitId);
      } else if(entry.getType() == ModType.REMOVE) {
        this.visitRemoveRightForMember(entry.getRole(), rights, status, Optional.empty(), commitId);
      } else {
        RepoAssert.fail("Unknown modification type: " + entry.getType() + "!"); 
      }
    }
    
    for(final var entry : this.addToPartyWithRights.entrySet()) {
      for(final OrgRight role : entry.getValue()) {
        final var party = Optional.of(entry.getKey());
        final var rights = Optional.ofNullable(rightsByRightIdId.get(role.getId())).orElse(Collections.emptyList());
        final var status = this.currentActorStatus.stream()
            .filter(g -> role.getId().equals(g.getRightId()))
            .filter(g -> entry.getKey().getId().equals(g.getPartyId()))
            .findFirst().orElse(null);

        this.visitAddRightForMember(role, rights, status, party, commitId);
      }
    }
    ;
    
    final var commit = commitBuilder.close();

    final var batch = this.batch
      .repoId(repoId)
      .status(BatchStatus.OK)
      .commit(commit.getItem1())
      .commitTrees(commit.getItem2())
      .log(commit.getItem1().getCommitLog())
      .build();
       
    // no changes
    if(batch.isEmpty()) {
      
      throw new NoMemberChangesException();
    }
    return batch;
  }
  
  private void visitMemberChanges(String commitId) {
    final var newState = ImmutableOrgMember.builder()
    .id(current.getId())
    .commitId(commitId)
    .createdWithCommitId(commitId)
    .externalId(newExternalId == null ? current.getExternalId() : newExternalId.get())
    .userName(newMemberName == null ? current.getUserName() : newMemberName.get())
    .email(newEmail == null  ? current.getEmail() : newEmail.get())
    .build();
    
    // no changes
    if( Objects.equal(newState.getEmail(), current.getEmail()) && 
        Objects.equal(newState.getExternalId(), current.getExternalId()) &&
        Objects.equal(newState.getUserName(), current.getUserName())
        ) {
      return;
    }
    this.commitBuilder.merge(current, newState);
    this.batch.addMembersToUpdate(newState);
  }
  
  private void visitMemberStatus(String commitId, OrgActorStatus status) {
    if(this.newStatus == null) {
      return;
    }
    if(status != null && status.getValue() == newStatus) {
      return;
    }
     
    if(status == null) {
      final var newStatus = ImmutableOrgActorStatus.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .createdWithCommitId(commitId)
        .memberId(current.getId())
        .value(this.newStatus)
        .build();
      this.batch.addActorStatus(newStatus);
      this.commitBuilder.add(newStatus);
    } else {
      final var newStatus = ImmutableOrgActorStatus.builder().from(status).value(this.newStatus).build();
      this.commitBuilder.merge(status, newStatus);
      this.batch.addActorStatusToUpdate(newStatus);      
    }
  }

  private void visitDisableRightForMember(
      OrgRight entry, 
      List<OrgMemberRight> memberRights, 
      OrgActorStatus status, 
      Optional<OrgParty> group, String commitId) {
    
    final var partyId = group.map(g -> g.getId()).orElse(null);
    final var exists = memberRights.stream()
        .filter(e -> 
          (e.getPartyId() == null && partyId == null) ||
          (e.getPartyId() != null && partyId != null && partyId.equals(e.getPartyId()))
        )
        .count() > 0;
        
    if(!exists) {
      final var membership = ImmutableOrgMemberRight.builder()
          .id(OidUtils.gen())
          .rightId(entry.getId())
          .memberId(current.getId())
          .commitId(commitId)
          .partyId(partyId)
          .build();
      this.batch.addMemberRights(membership);
      this.commitBuilder.add(membership);
    }
    
    if(status == null) {
      final var roleStatus = ImmutableOrgActorStatus.builder()
          .id(OidUtils.gen())
          .rightId(entry.getId())
          .memberId(current.getId())
          .commitId(commitId)
          .value(OrgActorStatus.OrgActorStatusType.DISABLED)
          .build();      
      this.batch.addActorStatus(roleStatus);
      this.commitBuilder.add(roleStatus);
      return;
    }
    
    // already removed
    if(status.getValue() == OrgActorStatus.OrgActorStatusType.DISABLED) {
      return;
    }

    final var roleStatus = ImmutableOrgActorStatus.builder()
        .id(status.getId())
        .rightId(entry.getId())
        .memberId(current.getId())
        .commitId(commitId)
        .value(OrgActorStatus.OrgActorStatusType.DISABLED)
        .build();
    
    this.batch.addActorStatusToUpdate(roleStatus);
    this.commitBuilder.merge(status, roleStatus);
  }
  
  private void visitRemoveRightForMember(
      OrgRight entry, 
      List<OrgMemberRight> memberRights, 
      OrgActorStatus status, 
      Optional<OrgParty> group, String commitId) {
    
    final var partyId = group.map(g -> g.getId()).orElse(null);
    memberRights.stream()
    .filter(e -> 
      (e.getPartyId() == null && partyId == null) ||
      (e.getPartyId() != null && partyId != null && partyId.equals(e.getPartyId()))
    )
    .forEach(memberRight -> {
      this.batch.addMemberRightsToDelete(memberRight);
      this.commitBuilder.rm(memberRight);
    });
    
    
    // nothing to remove
    if(status == null) {
      return;
    }
    this.batch.addStatusToDelete(status);
    this.commitBuilder.rm(status);
  }
  
  private void visitAddRightForMember(
      OrgRight entry, 
      List<OrgMemberRight> memberRights, 
      OrgActorStatus status, 
      Optional<OrgParty> group, String commitId) {
    
    final var partyId = group.map(g -> g.getId()).orElse(null);
    final var exists = memberRights.stream()
        .filter(e -> 
          (e.getPartyId() == null && partyId == null) ||
          (e.getPartyId() != null && partyId != null && partyId.equals(e.getPartyId()))
        )
        .count() > 0;
    if(!exists) {
      final var membership = ImmutableOrgMemberRight.builder()
          .id(OidUtils.gen())
          .rightId(entry.getId())
          .memberId(current.getId())
          .commitId(commitId)
          .partyId(partyId)
          .build();
      this.batch.addMemberRights(membership);
      this.commitBuilder.add(membership);
    }
    
    if(status != null) {
      final var roleStatus = ImmutableOrgActorStatus.builder()
          .id(status.getId())
          .rightId(entry.getId())
          .memberId(current.getId())
          .commitId(commitId)
          .partyId(group.map(g -> g.getId()).orElse(null))
          .value(OrgActorStatus.OrgActorStatusType.IN_FORCE)
          .build();
      this.batch.addActorStatusToUpdate(roleStatus);
      this.commitBuilder.merge(status, roleStatus);
    }
  }
  
  private void visitDisableMembership(OrgParty entry, OrgMembership currentMembership, OrgActorStatus status, String commitId) {
    if(currentMembership == null) {
      final var membership = ImmutableOrgMembership.builder()
          .id(OidUtils.gen())
          .partyId(entry.getId())
          .memberId(current.getId())
          .commitId(commitId)
          .build();
      this.batch.addMemberships(membership);
      this.commitBuilder.add(membership);
    }
    
    if(status == null) {
      final var groupStatus = ImmutableOrgActorStatus.builder()
          .id(OidUtils.gen())
          .createdWithCommitId(commitId)
          .partyId(entry.getId())
          .memberId(current.getId())
          .commitId(commitId)
          .value(OrgActorStatus.OrgActorStatusType.DISABLED)
          .build();
      this.batch.addActorStatus(groupStatus);
      this.commitBuilder.add(groupStatus);
      return;
    }
    
    // already disabled
    if(status.getValue() == OrgActorStatus.OrgActorStatusType.DISABLED) {
      return;
    }

    final var groupStatus = ImmutableOrgActorStatus.builder()
        .id(status.getId())
        .partyId(entry.getId())
        .memberId(current.getId())
        .commitId(commitId)
        .value(OrgActorStatus.OrgActorStatusType.DISABLED)
        .build();
    this.batch.addActorStatusToUpdate(groupStatus);
    this.commitBuilder.merge(status, groupStatus);
  }
  
  private void visitRemoveMembership(OrgParty entry, OrgMembership currentMembership, OrgActorStatus status, String commitId) {
    if(currentMembership != null) {
      this.batch.addMembershipsToDelete(currentMembership);
      this.commitBuilder.rm(currentMembership);
    }
    if(status != null) {
      this.batch.addStatusToDelete(status);
      this.commitBuilder.rm(status);
    }
  }
  
  private void visitAddMembership(OrgParty entry, OrgMembership currentMembership, OrgActorStatus status, String commitId) {
    if(currentMembership == null) {
      final var membership = ImmutableOrgMembership.builder()
          .id(OidUtils.gen())
          .partyId(entry.getId())
          .memberId(current.getId())
          .commitId(commitId)
          .build();
      this.batch.addMemberships(membership);
      this.commitBuilder.add(membership);
    }
    
    if(status != null && status.getValue() != OrgActorStatus.OrgActorStatusType.IN_FORCE) {
      final var groupStatus = ImmutableOrgActorStatus.builder()
          .id(status.getId())
          .partyId(entry.getId())
          .memberId(current.getId())
          .commitId(commitId)
          .value(OrgActorStatus.OrgActorStatusType.IN_FORCE)
          .build();
      this.batch.addStatusToDelete(groupStatus);
      this.commitBuilder.merge(status, groupStatus);
    }
  }
  
  @Data @RequiredArgsConstructor
  private static class ModRole {
    private final ModType type;
    private final OrgRight role;
  }
  
  @Data @RequiredArgsConstructor
  private static class ModGroup {
    private final ModType type;
    private final OrgParty group;
  }
  
  public static class NoMemberChangesException extends Exception {
    private static final long serialVersionUID = 3041890960089273165L;
    
  }
}
