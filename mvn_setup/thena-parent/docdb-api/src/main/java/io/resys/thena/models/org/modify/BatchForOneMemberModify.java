package io.resys.thena.models.org.modify;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Objects;

import io.resys.thena.api.actions.OrgCommitActions.ModType;
import io.resys.thena.api.models.ImmutableMessage;
import io.resys.thena.api.models.ImmutableOrgActorStatus;
import io.resys.thena.api.models.ImmutableOrgCommit;
import io.resys.thena.api.models.ImmutableOrgCommitTree;
import io.resys.thena.api.models.ImmutableOrgMember;
import io.resys.thena.api.models.ImmutableOrgMemberRight;
import io.resys.thena.api.models.ImmutableOrgMembership;
import io.resys.thena.api.models.ThenaOrgObject.IsOrgObject;
import io.resys.thena.api.models.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.api.models.ThenaOrgObject.OrgCommitTree;
import io.resys.thena.api.models.ThenaOrgObject.OrgMember;
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
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOneMemberModify {
  private final String repoId;
  private final String author;
  private final String message;
  private final List<ModGroup> modifyMemberships = new ArrayList<>(); 
  private final List<ModRole> modifyMemberRights = new ArrayList<>();
  private final List<OrgCommitTree> tree = new ArrayList<OrgCommitTree>();
  private final List<OrgMembership> newMemberships = new ArrayList<>();
  private final List<OrgMemberRight> newMemberRights = new ArrayList<>();
  private final Map<OrgParty, List<OrgRight>> addToPartyWithRights = new LinkedHashMap<>();
  private final List<OrgActorStatus> newActorStatus = new ArrayList<>();
  
  private final List<OrgMemberRight> memberRightsToDelete = new ArrayList<OrgMemberRight>();
  private final List<OrgMembership> membershipsToDelete = new ArrayList<OrgMembership>();
  private final List<OrgActorStatus> statusToDelete = new ArrayList<OrgActorStatus>();
  

  private OrgMember current;
  private final List<OrgMemberRight> currentMemberRights = new ArrayList<>();
  private final List<OrgMembership> currentMemberships = new ArrayList<>();
  private final List<OrgActorStatus> currentActorStatus = new ArrayList<>();
  private final List<String> identifiersForUpdates = new ArrayList<>();

  private Optional<String> newMemberName;
  private Optional<String> newEmail;
  private Optional<String> newExternalId;
  private OrgActorStatusType newStatus;
  
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
  public BatchForOneMemberModify newStatus(OrgActorStatusType newStatus) { 
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
  public ImmutableOrgBatchForOne create() throws NoChangesException {
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
    
    // member
    final var member = visitUserChanges(commitId);
    
    // user status
    final var memberStatus = this.currentActorStatus.stream().filter(g -> g.getRightId() == null && g.getPartyId() == null).findFirst();
    visitUserStatus(commitId, memberStatus.orElse(null));
    
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
    
    final var logger = new StringBuilder(System.lineSeparator())
      .append(" | updated")
      .append(System.lineSeparator())
      .append("  + commit:          ").append(commitId).append(" tree: ").append(tree.size() + "").append(" entries")
      .append(System.lineSeparator());
    
    if(!member.isEmpty()) {
      logger
        .append("  + member:            ").append(member.get().getId()).append("::").append(newMemberName)
        .append(System.lineSeparator());
    } 
    logger
      .append("  + added to parties: ").append(String.join(",", modifyMemberships.stream()
          .filter(g -> g.getType() == ModType.ADD)
          .map(g -> g.getGroup().getPartyName() + "::" + g.getGroup().getId())
          .toList()))
      .append(System.lineSeparator())

      .append("  + disabled in parties: ").append(String.join(",", modifyMemberships.stream()
          .filter(g -> g.getType() == ModType.DISABLED)
          .map(g -> g.getGroup().getPartyName() + "::" + g.getGroup().getId())
          .toList()))
      .append(System.lineSeparator())
      
      .append("  + removed from parties: ").append(String.join(",", modifyMemberships.stream()
          .filter(g -> g.getType() == ModType.REMOVE)
          .map(g -> g.getGroup().getPartyName() + "::" + g.getGroup().getId())
          .toList()))
      .append(System.lineSeparator())
      
      .append("  + added rights:  ").append(String.join(",", modifyMemberRights.stream()
          .filter(g -> g.getType() == ModType.ADD)
          .map(g -> g.getRole().getRightName() + "::" + g.getRole().getId())
          .toList()))
      
      .append("  + disabled rights:  ").append(String.join(",", modifyMemberRights.stream()
          .filter(g -> g.getType() == ModType.DISABLED)
          .map(g -> g.getRole().getRightName() + "::" + g.getRole().getId())
          .toList()))
      
      .append("  + removed rights:  ").append(String.join(",", modifyMemberRights.stream()
          .filter(g -> g.getType() == ModType.REMOVE)
          .map(g -> g.getRole().getRightName() + "::" + g.getRole().getId())
          .toList()))
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
      .members(member.map(u -> Arrays.asList(u)).orElse(Collections.emptyList()))
      .memberships(newMemberships)
      .memberRights(newMemberRights)
      .actorStatus(newActorStatus)
      .identifiersForUpdates(identifiersForUpdates)
      
      .statusToDelete(statusToDelete)
      .memberRightsToDelete(memberRightsToDelete)
      .membershipsToDelete(membershipsToDelete)
      
      .log(ImmutableMessage.builder().text(logger.toString()).build())
      .build();
       
    // no changes
    if( newActorStatus.isEmpty() &&
        newMemberRights.isEmpty() && 
        newMemberships.isEmpty() &&
        
        statusToDelete.isEmpty() &&
        memberRightsToDelete.isEmpty() &&
        membershipsToDelete.isEmpty() &&
        
        batch.getMembers().isEmpty()) {
      
      throw new NoChangesException();
    }
    return batch;
  }
  
  private void visitChangeTree(String commitId, IsOrgObject target, OrgOperationType op) {
    final var entry = ImmutableOrgCommitTree.builder()
        .actorId(target.getId())
        .actorType(target.getDocType().name())
        .commitId(commitId)
        .operationType(op)
        .id(OidUtils.gen())
        .value(JsonObject.mapFrom(target))
        .build();
    tree.add(entry);
  }
  
  private Optional<OrgMember> visitUserChanges(String commitId) {
    final var newState = ImmutableOrgMember.builder()
    .id(current.getId())
    .commitId(commitId)
    .externalId(newExternalId == null ? current.getExternalId() : newExternalId.get())
    .userName(newMemberName == null ? current.getUserName() : newMemberName.get())
    .email(newEmail == null  ? current.getEmail() : newEmail.get())
    .build();
    
    // no changes
    if( Objects.equal(newState.getEmail(), current.getEmail()) && 
        Objects.equal(newState.getExternalId(), current.getExternalId()) &&
        Objects.equal(newState.getUserName(), current.getUserName())
        ) {
      return Optional.empty();
    }
    visitChangeTree(commitId, newState, OrgOperationType.MOD);
    identifiersForUpdates.add(current.getId());
    return Optional.of(newState);
  }
  
  private void visitUserStatus(String commitId, OrgActorStatus status) {
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
        .memberId(current.getId())
        .value(this.newStatus)
        .build();
      this.newActorStatus.add(newStatus);
      visitChangeTree(commitId, newStatus, OrgOperationType.ADD);
    } else {
      final var newStatus = ImmutableOrgActorStatus.builder().from(status).value(this.newStatus).build();
      visitChangeTree(commitId, newStatus, OrgOperationType.MOD);
      identifiersForUpdates.add(newStatus.getId());      
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
      newMemberRights.add(membership);
      visitChangeTree(commitId, membership, OrgOperationType.ADD);
    }
    
    if(status == null) {
      final var roleStatus = ImmutableOrgActorStatus.builder()
          .id(OidUtils.gen())
          .rightId(entry.getId())
          .memberId(current.getId())
          .commitId(commitId)
          .value(OrgActorStatusType.DISABLED)
          .build();      
      newActorStatus.add(roleStatus);
      visitChangeTree(commitId, roleStatus, OrgOperationType.ADD);
      return;
    }
    
    // already removed
    if(status.getValue() == OrgActorStatusType.DISABLED) {
      return;
    }

    final var roleStatus = ImmutableOrgActorStatus.builder()
        .id(status.getId())
        .rightId(entry.getId())
        .memberId(current.getId())
        .commitId(commitId)
        .value(OrgActorStatusType.DISABLED)
        .build();
    identifiersForUpdates.add(status.getId());
    newActorStatus.add(roleStatus);
    visitChangeTree(commitId, roleStatus, OrgOperationType.MOD);
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
      memberRightsToDelete.add(memberRight);
      visitChangeTree(commitId, memberRight, OrgOperationType.REM);
    });
    
    
    // nothing to remove
    if(status == null) {
      return;
    }
    statusToDelete.add(status);
    visitChangeTree(commitId, status, OrgOperationType.REM);
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
      newMemberRights.add(membership);
      visitChangeTree(commitId, membership, OrgOperationType.ADD);
    }
    
    if(status != null) {
      final var roleStatus = ImmutableOrgActorStatus.builder()
          .id(status.getId())
          .rightId(entry.getId())
          .memberId(current.getId())
          .commitId(commitId)
          .partyId(group.map(g -> g.getId()).orElse(null))
          .value(OrgActorStatusType.IN_FORCE)
          .build();
      identifiersForUpdates.add(status.getId());
      newActorStatus.add(roleStatus);
      visitChangeTree(commitId, roleStatus, OrgOperationType.MOD);
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
      newMemberships.add(membership);
      visitChangeTree(commitId, membership, OrgOperationType.ADD);
    }
    
    if(status == null) {
      final var groupStatus = ImmutableOrgActorStatus.builder()
          .id(OidUtils.gen())
          .partyId(entry.getId())
          .memberId(current.getId())
          .commitId(commitId)
          .value(OrgActorStatusType.DISABLED)
          .build();
      newActorStatus.add(groupStatus);
      visitChangeTree(commitId, groupStatus, OrgOperationType.ADD);
      return;
    }
    
    // already disabled
    if(status.getValue() == OrgActorStatusType.DISABLED) {
      return;
    }

    final var groupStatus = ImmutableOrgActorStatus.builder()
        .id(status.getId())
        .partyId(entry.getId())
        .memberId(current.getId())
        .commitId(commitId)
        .value(OrgActorStatusType.DISABLED)
        .build();
    identifiersForUpdates.add(status.getId());
    newActorStatus.add(groupStatus);
    visitChangeTree(commitId, groupStatus, OrgOperationType.MOD);
  }
  
  private void visitRemoveMembership(OrgParty entry, OrgMembership currentMembership, OrgActorStatus status, String commitId) {
    
    if(currentMembership != null) {
      membershipsToDelete.add(currentMembership);
      visitChangeTree(commitId, currentMembership, OrgOperationType.REM);
    }
    if(status != null) {
      statusToDelete.add(status);
      visitChangeTree(commitId, status, OrgOperationType.REM);
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
      newMemberships.add(membership);
      visitChangeTree(commitId, membership, OrgOperationType.ADD);
    }
    
    if(status != null && status.getValue() != OrgActorStatusType.IN_FORCE) {
      final var groupStatus = ImmutableOrgActorStatus.builder()
          .id(status.getId())
          .partyId(entry.getId())
          .memberId(current.getId())
          .commitId(commitId)
          .value(OrgActorStatusType.IN_FORCE)
          .build();
      newActorStatus.add(groupStatus);
      identifiersForUpdates.add(status.getId());
      visitChangeTree(commitId, groupStatus, OrgOperationType.MOD);
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
  
  public static class NoChangesException extends Exception {
    private static final long serialVersionUID = 3041890960089273165L;
    
  }
}
