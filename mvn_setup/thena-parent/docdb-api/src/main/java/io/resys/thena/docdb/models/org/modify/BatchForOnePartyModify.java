package io.resys.thena.docdb.models.org.modify;

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

import io.resys.thena.docdb.api.actions.OrgCommitActions.ModType;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ImmutableOrgActorStatus;
import io.resys.thena.docdb.api.models.ImmutableOrgCommit;
import io.resys.thena.docdb.api.models.ImmutableOrgCommitTree;
import io.resys.thena.docdb.api.models.ImmutableOrgMemberRight;
import io.resys.thena.docdb.api.models.ImmutableOrgMembership;
import io.resys.thena.docdb.api.models.ImmutableOrgParty;
import io.resys.thena.docdb.api.models.ImmutableOrgPartyRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.IsOrgObject;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgCommitTree;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgOperationType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRight;
import io.resys.thena.docdb.models.git.GitInserts.BatchStatus;
import io.resys.thena.docdb.models.org.ImmutableOrgBatchForOne;
import io.resys.thena.docdb.support.OidUtils;
import io.resys.thena.docdb.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOnePartyModify {
  private final String repoId;
  private final String author;
  private final String message;
  private final List<ModMember> modifyMemberships = new ArrayList<>(); 
  private final List<ModRight> modifyPartyRights = new ArrayList<>();
  private final List<OrgCommitTree> tree = new ArrayList<OrgCommitTree>();
  private final List<OrgMembership> newMemberships = new ArrayList<>();
  private final List<OrgMemberRight> newMemberRights = new ArrayList<>();
  private final List<OrgPartyRight> newPartyRights = new ArrayList<>();
  private final Map<OrgMember, List<OrgRight>> addMemberWithRights = new LinkedHashMap<>();
  private final List<OrgActorStatus> newActorStatus = new ArrayList<>();

  private final List<OrgPartyRight> partyRightsToDelete = new ArrayList<OrgPartyRight>();
  private final List<OrgMemberRight> memberRightsToDelete = new ArrayList<OrgMemberRight>();
  private final List<OrgMembership> membershipsToDelete = new ArrayList<OrgMembership>();
  private final List<OrgActorStatus> statusToDelete = new ArrayList<OrgActorStatus>();
  

  private OrgParty current;
  private final List<OrgPartyRight> currentPartyRights = new ArrayList<>();
  private final List<OrgMemberRight> currentMemberRights = new ArrayList<>();
  private final List<OrgMembership> currentMemberships = new ArrayList<>();
  private final List<OrgActorStatus> currentActorStatus = new ArrayList<>();
  private final List<String> identifiersForUpdates = new ArrayList<>();

  private Optional<String> newPartyName;
  private Optional<String> newPartyDesc;
  private Optional<String> newExternalId;
  private Optional<OrgParty> newParentPartyId;
  private OrgActorStatusType newStatus;
  
  public BatchForOnePartyModify current(OrgParty current) {
    this.current = current; 
    return this; 
  }
  public BatchForOnePartyModify currentMemberships(List<OrgMembership> currentMemberships) { 
    this.currentMemberships.addAll(currentMemberships);
    return this; 
  }
  public BatchForOnePartyModify currentMemberRights(List<OrgMemberRight> currentMemberRights) { 
    this.currentMemberRights.addAll(currentMemberRights);
    return this; 
  }
  public BatchForOnePartyModify currentPartyRights(List<OrgPartyRight> currentPartyRights) { 
    this.currentPartyRights.addAll(currentPartyRights);
    return this; 
  }
  public BatchForOnePartyModify currentStatus(List<OrgActorStatus> status) { 
    this.currentActorStatus.addAll(status);
    return this; 
  }
  public BatchForOnePartyModify newPartyName(Optional<String> newPartyName) {
    this.newPartyName = newPartyName; 
    return this; 
  }
  public BatchForOnePartyModify newPartyDesc(Optional<String> newDesc) {
    this.newPartyDesc = newDesc; 
    return this; 
  }
  public BatchForOnePartyModify newParentPartyId(Optional<OrgParty> newParentPartyId) {
    this.newParentPartyId = newParentPartyId; 
    return this; 
  }
  public BatchForOnePartyModify newExternalId(Optional<String> externalId) { 
    this.newExternalId = externalId;
    return this; 
  }
  public BatchForOnePartyModify newStatus(OrgActorStatusType newStatus) { 
    this.newStatus = newStatus;
    return this; 
  }
  public BatchForOnePartyModify modifyMembership(ModType type, OrgMember member) { 
    this.modifyMemberships.add(new ModMember(type, member));
    return this; 
  }
  public BatchForOnePartyModify modifyPartyRights(ModType type, OrgRight role) { 
    this.modifyPartyRights.add(new ModRight(type, role)); 
    return this;
  }
  public BatchForOnePartyModify modifyMemberRightsInParty(ModType type, Map<OrgMember, List<OrgRight>> addMemberWithRights) { 
    if(type == ModType.ADD) {
      this.addMemberWithRights.putAll(addMemberWithRights);
    } else {
      throw new RuntimeException("not implemented!!");
    }
    return this;
  }
  public ImmutableOrgBatchForOne create() throws NoPartyChangesException {
    RepoAssert.notEmpty(repoId,   () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author,   () -> "author can't be empty!");
    RepoAssert.notEmpty(message,  () -> "message can't be empty!");
    RepoAssert.notNull(current,   () -> "member can't be null!");
    RepoAssert.notEmptyIfDefined(newPartyName, () -> "newPartyName can't be empty!");
    RepoAssert.notEmptyIfDefined(newPartyDesc, () -> "newPartyDesc can't be empty!");
    
    RepoAssert.notNull(modifyMemberships, () -> "parties can't be null!");
    RepoAssert.notNull(modifyPartyRights, () -> "rights can't be null!");
    
    
    for(final var member : addMemberWithRights.keySet()) {
      final var exists = this.modifyMemberships.stream()
          .filter(e -> e.getType() == ModType.ADD)
          .filter(e -> e.getMember().getId().equals(member.getId())).count() > 0;
      if(!exists) {
        modifyMembership(ModType.ADD, member);
      }
    }
    
    final var commitId = OidUtils.gen();
    final var createdAt = OffsetDateTime.now();
    
    // party
    final var party = visitPartyChanges(commitId);
    final var partyStatus = this.currentActorStatus.stream().filter(g -> g.getRightId() == null && g.getMemberId() == null).findFirst();
    visitPartyStatus(commitId, partyStatus.orElse(null));
    
    // parties
    final var memebrShipInParty = this.currentMemberships.stream().collect(Collectors.toMap(e -> e.getMemberId(), e -> e));
    final var statusInParty = this.currentActorStatus.stream()
        .filter(g -> g.getMemberId() != null && g.getRightId() == null)
        .collect(Collectors.toMap(e -> e.getMemberId(), e -> e));
    
    for(final var entry : modifyMemberships) {
      final var status = statusInParty.get(entry.getMember().getId());
      final var membership = memebrShipInParty.get(entry.getMember().getId());
      if(entry.getType() == ModType.ADD) {
        this.visitAddMembership(entry.getMember(), membership, status, commitId);  
      } else if(entry.getType() == ModType.DISABLED) {
        this.visitDisableMembership(entry.getMember(), membership, status, commitId);
      } else if(entry.getType() == ModType.REMOVE) {
        this.visitRemoveMembership(entry.getMember(), membership, status, commitId);
      } else {
        RepoAssert.fail("Unknown modification type: " + entry.getType() + "!"); 
      }
    }
    
    // rights
    final var statusInRights = this.currentActorStatus.stream()
        .filter(g -> g.getRightId() != null && g.getMemberId() == null)
        .collect(Collectors.toMap(e -> e.getRightId(), e -> e));
    final var rightsInParty = this.currentPartyRights.stream().collect(Collectors.toMap(e -> e.getRightId(), e -> e));
    for(final var entry : modifyPartyRights) {
      final var status = statusInRights.get(entry.getRight().getId());
      final var rights = rightsInParty.get(entry.getRight().getId());
      
      if(entry.getType() == ModType.ADD) {
        this.visitAddRightForParty(entry.getRight(), rights, status, commitId);  
      } else if(entry.getType() == ModType.DISABLED) {
        this.visitDisableRightForMember(entry.getRight(), rights, status, commitId);
      } else if(entry.getType() == ModType.REMOVE) {
        this.visitRemoveRightForParty(entry.getRight(), rights, status, commitId);
      } else {
        RepoAssert.fail("Unknown modification type: " + entry.getType() + "!"); 
      }
    }
    
    final var rightsByRightIdId = this.currentMemberRights.stream().collect(Collectors.groupingBy(OrgMemberRight::getRightId));
    for(final Map.Entry<OrgMember, List<OrgRight>> entry : this.addMemberWithRights.entrySet()) {
      for(final OrgRight role : entry.getValue()) {
        final OrgMember member = entry.getKey();
        final List<OrgMemberRight> rights = Optional.ofNullable(rightsByRightIdId.get(role.getId())).orElse(Collections.emptyList());
        final OrgActorStatus status = this.currentActorStatus.stream()
            .filter(g -> role.getId().equals(g.getRightId()))
            .filter(g -> entry.getKey().getId().equals(g.getMemberId()))
            .findFirst().orElse(null);
        this.visitAddRightForMember(role, rights, status, member, commitId);
      }
    }
    
    final var logger = new StringBuilder(System.lineSeparator())
      .append(" | updated")
      .append(System.lineSeparator())
      .append("  + commit:          ").append(commitId).append(" tree: ").append(tree.size() + "").append(" entries")
      .append(System.lineSeparator());
    
    if(!party.isEmpty()) {
      logger
        .append("  + party:            ").append(party.get().getId()).append("::").append(party.get().getPartyName())
        .append(System.lineSeparator());
    } 
    logger
      .append("  + added members: ").append(String.join(",", modifyMemberships.stream()
          .filter(g -> g.getType() == ModType.ADD)
          .map(g -> g.getMember().getUserName() + "::" + g.getMember().getId())
          .toList()))
      .append(System.lineSeparator())

      .append("  + disabled in members: ").append(String.join(",", modifyMemberships.stream()
          .filter(g -> g.getType() == ModType.DISABLED)
          .map(g -> g.getMember().getUserName() + "::" + g.getMember().getId())
          .toList()))
      .append(System.lineSeparator())
      
      .append("  + removed from members: ").append(String.join(",", modifyMemberships.stream()
          .filter(g -> g.getType() == ModType.REMOVE)
          .map(g -> g.getMember().getUserName() + "::" + g.getMember().getId())
          .toList()))
      .append(System.lineSeparator())
      
      .append("  + added rights:  ").append(String.join(",", modifyPartyRights.stream()
          .filter(g -> g.getType() == ModType.ADD)
          .map(g -> g.getRight().getRightName() + "::" + g.getRight().getId())
          .toList()))
      
      .append("  + disabled rights:  ").append(String.join(",", modifyPartyRights.stream()
          .filter(g -> g.getType() == ModType.DISABLED)
          .map(g -> g.getRight().getRightName() + "::" + g.getRight().getId())
          .toList()))
      
      .append("  + removed rights:  ").append(String.join(",", modifyPartyRights.stream()
          .filter(g -> g.getType() == ModType.REMOVE)
          .map(g -> g.getRight().getRightName() + "::" + g.getRight().getId())
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
      .parties(party.map(u -> Arrays.asList(u)).orElse(Collections.emptyList()))
      .memberships(newMemberships)
      .memberRights(newMemberRights)
      .actorStatus(newActorStatus)
      .identifiersForUpdates(identifiersForUpdates)
      
      .statusToDelete(statusToDelete)
      .memberRightsToDelete(memberRightsToDelete)
      .membershipsToDelete(membershipsToDelete)
      .partyRightToDelete(partyRightsToDelete)
      
      .log(ImmutableMessage.builder().text(logger.toString()).build())
      .build();
       
    // no changes
    if( newActorStatus.isEmpty() &&
        newMemberRights.isEmpty() && 
        newMemberships.isEmpty() &&
        newPartyRights.isEmpty() &&
        
        statusToDelete.isEmpty() &&
        memberRightsToDelete.isEmpty() &&
        membershipsToDelete.isEmpty() &&
        partyRightsToDelete.isEmpty() &&
        
        batch.getParties().isEmpty()) {
      
      throw new NoPartyChangesException();
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
  
  private Optional<OrgParty> visitPartyChanges(String commitId) {
    final var newState = ImmutableOrgParty.builder()
    .id(current.getId())
    .commitId(commitId)
    .externalId(newExternalId == null ? current.getExternalId() : newExternalId.get())
    .partyName(newPartyName == null ? current.getPartyName() : newPartyName.get())
    .partyDescription(newPartyDesc == null  ? current.getPartyDescription() : newPartyDesc.get())
    .parentId(newParentPartyId == null  ? current.getParentId() : newParentPartyId.get().getId())
    .build();
    
    // no changes
    if( Objects.equal(newState.getPartyDescription(), current.getPartyDescription()) && 
        Objects.equal(newState.getExternalId(), current.getExternalId()) &&
        Objects.equal(newState.getParentId(), current.getParentId()) &&
        Objects.equal(newState.getPartyName(), current.getPartyName())
        ) {
      return Optional.empty();
    }
    visitChangeTree(commitId, newState, OrgOperationType.MOD);
    identifiersForUpdates.add(current.getId());
    return Optional.of(newState);
  }
  
  private void visitPartyStatus(String commitId, OrgActorStatus status) {
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
        .partyId(current.getId())
        .value(this.newStatus)
        .build();
      this.newActorStatus.add(newStatus);
      visitChangeTree(commitId, newStatus, OrgOperationType.ADD);
    } else {
      final var newStatus = ImmutableOrgActorStatus.builder().from(status).value(this.newStatus).build();
      this.newActorStatus.add(newStatus);
      visitChangeTree(commitId, newStatus, OrgOperationType.MOD);
      identifiersForUpdates.add(newStatus.getId());      
    }
  }

  private void visitDisableRightForMember(
      OrgRight entry, 
      OrgPartyRight partyRights, 
      OrgActorStatus status,
      String commitId) {
    
    final var exists = partyRights != null;
        
    if(!exists) {
      final var membership = ImmutableOrgPartyRight.builder()
          .id(OidUtils.gen())
          .rightId(entry.getId())
          .partyId(current.getId())
          .commitId(commitId)
          .build();
      newPartyRights.add(membership);
      visitChangeTree(commitId, membership, OrgOperationType.ADD);
    }
    
    if(status == null) {
      final var roleStatus = ImmutableOrgActorStatus.builder()
          .id(OidUtils.gen())
          .rightId(entry.getId())
          .partyId(current.getId())
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
        .partyId(current.getId())
        .commitId(commitId)
        .value(OrgActorStatusType.DISABLED)
        .build();
    identifiersForUpdates.add(status.getId());
    newActorStatus.add(roleStatus);
    visitChangeTree(commitId, roleStatus, OrgOperationType.MOD);
  }
  
  private void visitRemoveRightForParty(
      OrgRight entry, 
      OrgPartyRight partyRights, 
      OrgActorStatus status,
      String commitId) {
    
    if(partyRights != null) {
      partyRightsToDelete.add(partyRights);
      visitChangeTree(commitId, partyRights, OrgOperationType.REM);
    }
    
    // nothing to remove
    if(status == null) {
      return;
    }
    statusToDelete.add(status);
    visitChangeTree(commitId, status, OrgOperationType.REM);
  }
  
  private void visitAddRightForParty(
      OrgRight entry, 
      OrgPartyRight partyRights, 
      OrgActorStatus status,
      String commitId) {
    
    final var exists = partyRights != null;
    if(!exists) {
      final var membership = ImmutableOrgPartyRight.builder()
          .id(OidUtils.gen())
          .rightId(entry.getId())
          .commitId(commitId)
          .partyId(current.getId())
          .build();
      newPartyRights.add(membership);
      visitChangeTree(commitId, membership, OrgOperationType.ADD);
    }
    
    if(status != null) {
      final var roleStatus = ImmutableOrgActorStatus.builder()
          .id(status.getId())
          .rightId(entry.getId())
          .partyId(current.getId())
          .commitId(commitId)
          .value(OrgActorStatusType.IN_FORCE)
          .build();
      identifiersForUpdates.add(status.getId());
      newActorStatus.add(roleStatus);
      visitChangeTree(commitId, roleStatus, OrgOperationType.MOD);
    }
  }
  
  private void visitDisableMembership(OrgMember member, OrgMembership currentMembership, OrgActorStatus status, String commitId) {
    if(currentMembership == null) {
      final var membership = ImmutableOrgMembership.builder()
          .id(OidUtils.gen())
          .partyId(current.getId())
          .memberId(member.getId())
          .commitId(commitId)
          .build();
      newMemberships.add(membership);
      visitChangeTree(commitId, membership, OrgOperationType.ADD);
    }
    
    if(status == null) {
      final var groupStatus = ImmutableOrgActorStatus.builder()
          .id(OidUtils.gen())
          .partyId(current.getId())
          .memberId(member.getId())
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
        .partyId(current.getId())
        .memberId(member.getId())
        .commitId(commitId)
        .value(OrgActorStatusType.DISABLED)
        .build();
    identifiersForUpdates.add(status.getId());
    newActorStatus.add(groupStatus);
    visitChangeTree(commitId, groupStatus, OrgOperationType.MOD);
  }
  
  private void visitRemoveMembership(OrgMember entry, OrgMembership currentMembership, OrgActorStatus status, String commitId) {
    
    if(currentMembership != null) {
      membershipsToDelete.add(currentMembership);
      visitChangeTree(commitId, currentMembership, OrgOperationType.REM);
    }
    if(status != null) {
      statusToDelete.add(status);
      visitChangeTree(commitId, status, OrgOperationType.REM);
    }
  }
  
  private void visitAddMembership(OrgMember member, OrgMembership currentMembership, OrgActorStatus status, String commitId) {
    if(currentMembership == null) {
      final var membership = ImmutableOrgMembership.builder()
          .id(OidUtils.gen())
          .partyId(current.getId())
          .memberId(member.getId())
          .commitId(commitId)
          .build();
      newMemberships.add(membership);
      visitChangeTree(commitId, membership, OrgOperationType.ADD);
    }
    
    if(status != null && status.getValue() != OrgActorStatusType.IN_FORCE) {
      final var groupStatus = ImmutableOrgActorStatus.builder()
          .from(status)
          .commitId(commitId)
          .value(OrgActorStatusType.IN_FORCE)
          .build();
      newActorStatus.add(groupStatus);
      identifiersForUpdates.add(status.getId());
      visitChangeTree(commitId, groupStatus, OrgOperationType.MOD);
    }
  }
  
  private void visitAddRightForMember(
      OrgRight entry, 
      List<OrgMemberRight> memberRights, 
      OrgActorStatus status, 
      OrgMember member, String commitId) {
    
    final var exists = memberRights.stream()
        .filter(e -> 
          (e.getPartyId() == null) ||
          (e.getPartyId() != null && current.getParentId().equals(e.getPartyId()))
        )
        .count() > 0;
    if(!exists) {
      final var membership = ImmutableOrgMemberRight.builder()
          .id(OidUtils.gen())
          .rightId(entry.getId())
          .memberId(member.getId())
          .commitId(commitId)
          .partyId(current.getId())
          .build();
      newMemberRights.add(membership);
      visitChangeTree(commitId, membership, OrgOperationType.ADD);
    }
    
    if(status != null) {
      final var roleStatus = ImmutableOrgActorStatus.builder()
          .id(status.getId())
          .rightId(entry.getId())
          .memberId(member.getId())
          .commitId(commitId)
          .partyId(current.getId())
          .value(OrgActorStatusType.IN_FORCE)
          .build();
      identifiersForUpdates.add(status.getId());
      newActorStatus.add(roleStatus);
      visitChangeTree(commitId, roleStatus, OrgOperationType.MOD);
    }
  }
  
  
  @Data @RequiredArgsConstructor
  private static class ModRight {
    private final ModType type;
    private final OrgRight right;
  }
  
  @Data @RequiredArgsConstructor
  private static class ModMember {
    private final ModType type;
    private final OrgMember member;
  }
  
  public static class NoPartyChangesException extends Exception {
    private static final long serialVersionUID = 3041890960089273165L;
    
  }
}
