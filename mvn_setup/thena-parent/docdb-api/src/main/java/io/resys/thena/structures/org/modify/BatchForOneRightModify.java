package io.resys.thena.structures.org.modify;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Objects;

import io.resys.thena.api.actions.OrgCommitActions.ModType;
import io.resys.thena.api.entities.org.ImmutableOrgActorStatus;
import io.resys.thena.api.entities.org.ImmutableOrgCommit;
import io.resys.thena.api.entities.org.ImmutableOrgCommitTree;
import io.resys.thena.api.entities.org.ImmutableOrgMemberRight;
import io.resys.thena.api.entities.org.ImmutableOrgPartyRight;
import io.resys.thena.api.entities.org.ImmutableOrgRight;
import io.resys.thena.api.entities.org.OrgActorStatus;
import io.resys.thena.api.entities.org.OrgActorStatus.OrgActorStatusType;
import io.resys.thena.api.entities.org.OrgCommitTree;
import io.resys.thena.api.entities.org.OrgCommitTree.OrgOperationType;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgObject;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.structures.git.GitInserts.BatchStatus;
import io.resys.thena.structures.org.ImmutableOrgBatchForOne;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOneRightModify {
  private final String repoId;
  private final String author;
  private final String message;
  private final List<ModParty> parties = new ArrayList<>(); 
  private final List<ModMember> members = new ArrayList<>();
  private final List<OrgCommitTree> tree = new ArrayList<OrgCommitTree>();
  private final List<OrgMemberRight> resultMemberRights = new ArrayList<>();
  private final List<OrgPartyRight> resultPartyRights = new ArrayList<>();
  private final List<OrgActorStatus> resultActorStatus = new ArrayList<>();
  private final List<String> identifiersForUpdates = new ArrayList<>();
  
  private final List<OrgMemberRight> memberRightsToDelete = new ArrayList<OrgMemberRight>();
  private final List<OrgPartyRight> partyRightsToDelete = new ArrayList<OrgPartyRight>();
  private final List<OrgActorStatus> statusToDelete = new ArrayList<OrgActorStatus>();

  
  private List<OrgPartyRight> currentPartyRights;
  private List<OrgMemberRight> currentMemberRights; 
  private List<OrgActorStatus> currentActorStatus;  
  private OrgRight current;

  private Optional<String> newRightName;
  private Optional<String> newRightDescription;
  private Optional<String> newExternalId;
  private OrgActorStatusType newStatus;

  public BatchForOneRightModify current(OrgRight right) {                          this.current = right; return this; }
  public BatchForOneRightModify newRightDescription(Optional<String> rightDesc) {  this.newRightDescription = rightDesc; return this; }
  public BatchForOneRightModify newRightName(Optional<String> rightName) {         this.newRightName = rightName; return this; }
  public BatchForOneRightModify newExternalId(Optional<String> externalId) {       this.newExternalId = externalId; return this; }
  public BatchForOneRightModify newStatus(OrgActorStatusType newStatus) {          this.newStatus = newStatus; return this; }

  public BatchForOneRightModify currentPartyRights(List<OrgPartyRight> partyRights) {    this.currentPartyRights = partyRights; return this; }
  public BatchForOneRightModify currentRightStatus(List<OrgActorStatus> rightStatus) {   this.currentActorStatus = rightStatus; return this; }
  public BatchForOneRightModify currentMemberRights(List<OrgMemberRight> memberRights) { this.currentMemberRights = memberRights; return this; }

  
  public BatchForOneRightModify updateParty(ModType type, OrgParty groups) { 
    this.parties.add(new ModParty(type, groups));
    return this; 
  }
  public BatchForOneRightModify updateMember(ModType type, OrgMember role) { 
    this.members.add(new ModMember(type, role)); 
    return this;
  }
  public ImmutableOrgBatchForOne create() throws NoRightChangesException {
    RepoAssert.notEmpty(repoId,   () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author,   () -> "author can't be empty!");
    RepoAssert.notEmpty(message,  () -> "message can't be empty!");
    RepoAssert.notNull(current,   () -> "right can't be null!");
    RepoAssert.notEmptyIfDefined(newRightName,        () -> "rightName can't be empty!");
    RepoAssert.notEmptyIfDefined(newRightDescription, () -> "rightDescription can't be empty!");
    RepoAssert.notNull(parties, () -> "parties can't be null!");
    RepoAssert.notNull(members,   () -> "roles can't be null!");
    RepoAssert.notNull(currentPartyRights,   () -> "partyRights can't be null!");
    RepoAssert.notNull(currentActorStatus,   () -> "rightStatus can't be null!");
    RepoAssert.notNull(currentMemberRights,  () -> "memberRights can't be null!");
    
    
    final var commitId = OidUtils.gen();
    final var createdAt = OffsetDateTime.now();
    
    // right
    final var right = visitRightsChanges(commitId);
    
    // right status
    final var rightStatus = this.currentActorStatus.stream().filter(g -> g.getMemberId() == null && g.getPartyId() == null).findFirst();
    visitRightsStatus(commitId, rightStatus.orElse(null));
    
    
    // parties
    for(final var entry : parties) {
      if(entry.getType() == ModType.ADD) {
        this.visitAddRightsToParty(entry.getParty(), commitId);  
      } else if(entry.getType() == ModType.DISABLED) {
        this.visitDisableRightFromParty(entry.getParty(), commitId);
      } else if(entry.getType() == ModType.REMOVE) {
        this.visitRemoveRightFromParty(entry.getParty(), commitId);
      } else {
        RepoAssert.fail("Unknown modification type: " + entry.getType() + "!"); 
      }
    }
    
    // roles
    for(final var entry : members) {
      if(entry.getType() == ModType.ADD) {
        this.visitAddRightsToMember(entry.getMember(), commitId);  
      } else if(entry.getType() == ModType.DISABLED) {
        this.visitDisableRightsToMember(entry.getMember(), commitId);
      } else if(entry.getType() == ModType.REMOVE) {
        this.visitRemoveRightsToMember(entry.getMember(), commitId);
      } else {
        RepoAssert.fail("Unknown modification type: " + entry.getType() + "!"); 
      }
    }

    
    final var logger = new StringBuilder();
    logger.append(System.lineSeparator())
      .append(" | updated")
      .append(System.lineSeparator())
      .append("  + commit:          ").append(commitId).append(" tree: ").append(tree.size() + "").append(" entries")
      .append(System.lineSeparator());
    
    if(!right.isEmpty()) {
      logger
        .append("  + right:            ").append(right.get().getId()).append("::").append(right.get().getRightName())
        .append(System.lineSeparator());
    } 
    logger
      .append("  + added to parties: ").append(String.join(",", parties.stream()
          .filter(g -> g.getType() == ModType.ADD)
          .map(g -> g.getParty().getPartyName() + "::" + g.getParty().getId())
          .toList()))
      .append(System.lineSeparator())

      .append("  + removed from parties: ").append(String.join(",", parties.stream()
          .filter(g -> g.getType() == ModType.REMOVE)
          .map(g -> g.getParty().getPartyName() + "::" + g.getParty().getId())
          .toList()))
      .append(System.lineSeparator())

      .append("  + disabled from parties: ").append(String.join(",", parties.stream()
          .filter(g -> g.getType() == ModType.DISABLED)
          .map(g -> g.getParty().getPartyName() + "::" + g.getParty().getId())
          .toList()))
      .append(System.lineSeparator())
      
      .append("  + added to members:  ").append(String.join(",", members.stream()
          .filter(g -> g.getType() == ModType.ADD)
          .map(g -> g.getMember().getUserName() + "::" + g.getMember().getId())
          .toList()))
      .append(System.lineSeparator())

      .append("  + removed from members:  ").append(String.join(",", members.stream()
          .filter(g -> g.getType() == ModType.REMOVE)
          .map(g -> g.getMember().getUserName() + "::" + g.getMember().getId())
          .toList()))
      .append(System.lineSeparator())

      .append("  + disabled from members:  ").append(String.join(",", members.stream()
          .filter(g -> g.getType() == ModType.DISABLED)
          .map(g -> g.getMember().getUserName() + "::" + g.getMember().getId())
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
      .rights(right.map(u -> Arrays.asList(u)).orElse(Collections.emptyList()))
      .partyRights(resultPartyRights)
      .memberRights(resultMemberRights)
      .actorStatus(resultActorStatus)
      .identifiersForUpdates(identifiersForUpdates)
      
      .memberRightsToDelete(memberRightsToDelete)
      .partyRightToDelete(partyRightsToDelete)
      .statusToDelete(statusToDelete)
      
      .log(ImmutableMessage.builder().text(logger.toString()).build())
      .build();
    
    // no changes
    if( resultActorStatus.isEmpty() && 
        resultPartyRights.isEmpty() && 
        resultMemberRights.isEmpty() && 
        
        statusToDelete.isEmpty() &&
        partyRightsToDelete.isEmpty() &&
        memberRightsToDelete.isEmpty() &&
        
        batch.getRights().isEmpty()
    ) {
      throw new NoRightChangesException();
    }
    return batch;
  }
  
  private void visitChangeTree(String commitId, IsOrgObject target, OrgCommitTree.OrgOperationType op) {
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
  
  private void visitRightsStatus(String commitId, OrgActorStatus status) {
    if(this.newStatus == null) {
      return;
    }
    if(status != null && status.getValue() == newStatus) {
      return;
    }
    if(status == null && this.newStatus == OrgActorStatusType.IN_FORCE) {
      return;
    }    
    
     
    if(status == null) {
      final var newStatus = ImmutableOrgActorStatus.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .rightId(current.getId())
        .value(this.newStatus)
        .build();
      this.resultActorStatus.add(newStatus);
      visitChangeTree(commitId, newStatus, OrgOperationType.ADD);
    } else {
      final var newStatus = ImmutableOrgActorStatus.builder().from(status).value(this.newStatus).build();
      visitChangeTree(commitId, newStatus, OrgOperationType.MOD);
      this.resultActorStatus.add(newStatus);
      identifiersForUpdates.add(newStatus.getId());      
    }
  }
  
  private Optional<OrgRight> visitRightsChanges(String commitId) {
    final var newState = ImmutableOrgRight.builder()
    .id(current.getId())
    .commitId(commitId)
    .externalId(newExternalId == null ? current.getExternalId() : newExternalId.get())
    .rightName(newRightName == null ? current.getRightName() : newRightName.get())
    .rightDescription(newRightDescription == null  ? current.getRightDescription() : newRightDescription.get())
    .build();
    
    // no changes
    if( Objects.equal(newState.getRightDescription(), current.getRightDescription()) && 
        Objects.equal(newState.getExternalId(), current.getExternalId()) &&
        Objects.equal(newState.getRightName(), current.getRightName())
        ) {
      return Optional.empty();
    }
    visitChangeTree(commitId, newState, OrgCommitTree.OrgOperationType.MOD);
    identifiersForUpdates.add(current.getId());
    return Optional.of(newState);
  }
  private void visitRemoveRightsToMember(OrgMember member, String commitId) {
    final var currentStatus = currentActorStatus.stream()
        .filter(e -> member.getId().equals(e.getMemberId()))
        .filter(e -> e.getPartyId() == null)
        .findFirst();
    
    final var currentMemberRight = currentMemberRights.stream()
        .filter(e -> member.getId().equals(e.getMemberId()))
        .findFirst();
    
    if(currentMemberRight.isPresent()) {
      memberRightsToDelete.add(currentMemberRight.get());
      visitChangeTree(commitId, currentMemberRight.get(), OrgOperationType.REM);
    }
    if(currentStatus.isPresent()) {
      statusToDelete.add(currentStatus.get());
      visitChangeTree(commitId, currentStatus.get(), OrgOperationType.REM);
    }
    
  }
  
  private void visitDisableRightsToMember(OrgMember member, String commitId) {
    final var disabled = currentActorStatus.stream()
        .filter(e -> member.getId().equals(e.getMemberId()))
        .filter(e -> e.getPartyId() == null)
        .findFirst();
    
    if(disabled.isEmpty()) {
      final var status = ImmutableOrgActorStatus.builder()
          .id(OidUtils.gen())
          .memberId(member.getId())
          .commitId(commitId)
          .rightId(current.getId())
          .value(OrgActorStatus.OrgActorStatusType.DISABLED)
          .build();
      resultActorStatus.add(status);
      visitChangeTree(commitId, status, OrgCommitTree.OrgOperationType.ADD);
      return;
    }

    // already removed
    if(disabled.get().getValue() == OrgActorStatus.OrgActorStatusType.DISABLED) {
      return;
    }

    final var status = ImmutableOrgActorStatus.builder()
        .from(disabled.get())
        .commitId(commitId)
        .value(OrgActorStatus.OrgActorStatusType.DISABLED)
        .build();
    identifiersForUpdates.add(status.getId());
    resultActorStatus.add(status);
    visitChangeTree(commitId, status, OrgCommitTree.OrgOperationType.MOD);
  }
  
  private void visitAddRightsToMember(OrgMember entry, String commitId) {
    final var exists = currentMemberRights.stream()
        .filter(e -> e.getMemberId().equals(entry.getId()))
        .filter(e -> e.getPartyId() == null)
        .count() > 0;
    if(!exists) {
      final var membership = ImmutableOrgMemberRight.builder()
          .id(OidUtils.gen())
          .rightId(current.getId())
          .memberId(entry.getId())
          .commitId(commitId)
          .build();
      resultMemberRights.add(membership);
      visitChangeTree(commitId, membership, OrgCommitTree.OrgOperationType.ADD);
    }
    
    final var currentStatus = currentActorStatus.stream()
        .filter(e -> e.getPartyId() == null)
        .filter(e -> e.getValue() == OrgActorStatus.OrgActorStatusType.DISABLED)
        .filter(e -> entry.getId().equals(e.getMemberId()))
        .findFirst();
    if(currentStatus.isEmpty()) {
      return;
    }
    if(currentStatus.get().getValue() == OrgActorStatusType.IN_FORCE) {
      return;
    }
    
    final var status = ImmutableOrgActorStatus.builder()
        .from(currentStatus.get())
        .commitId(commitId)
        .value(OrgActorStatus.OrgActorStatusType.IN_FORCE)
        .build();
    resultActorStatus.add(status);
    identifiersForUpdates.add(status.getId());
    visitChangeTree(commitId, status, OrgCommitTree.OrgOperationType.MOD);
  }
  
  private void visitRemoveRightFromParty(OrgParty entry, String commitId) {
    final var currentStatus = currentActorStatus.stream()
        .filter(e -> entry.getId().equals(e.getPartyId()))
        .filter(e -> e.getMemberId() == null)
        .findFirst();
    
    final var currentPartyRight = currentPartyRights.stream()
        .filter(e -> entry.getId().equals(e.getPartyId()))
        .findFirst();
    
    if(currentPartyRight.isPresent()) {
      partyRightsToDelete.add(currentPartyRight.get());
      visitChangeTree(commitId, currentPartyRight.get(), OrgOperationType.REM);
    }
    if(currentStatus.isPresent()) {
      statusToDelete.add(currentStatus.get());
      visitChangeTree(commitId, currentStatus.get(), OrgOperationType.REM);
    }
  }
  private void visitDisableRightFromParty(OrgParty entry, String commitId) {
    final var currentStatus = currentActorStatus.stream()
        .filter(e -> entry.getId().equals(e.getPartyId()))
        .filter(e -> e.getMemberId() == null)
        .findFirst();
    
    if(currentStatus.isEmpty()) {
      final var status = ImmutableOrgActorStatus.builder()
          .id(OidUtils.gen())
          .partyId(entry.getId())
          .commitId(commitId)
          .rightId(current.getId())
          .value(OrgActorStatus.OrgActorStatusType.DISABLED)
          .build();
      resultActorStatus.add(status);
      visitChangeTree(commitId, status, OrgCommitTree.OrgOperationType.ADD);
      return;
    }

    // already removed
    if(currentStatus.get().getValue() == OrgActorStatusType.DISABLED) {
      return;
    }

    final var status = ImmutableOrgActorStatus.builder()
        .from(currentStatus.get())
        .commitId(commitId)
        .value(OrgActorStatus.OrgActorStatusType.DISABLED)
        .build();
    identifiersForUpdates.add(status.getId());
    resultActorStatus.add(status);
    visitChangeTree(commitId, status, OrgCommitTree.OrgOperationType.MOD);
  }
  
  private void visitAddRightsToParty(OrgParty entry, String commitId) {
    final var exists = currentPartyRights.stream()
        .filter(e -> e.getPartyId().equals(entry.getId()))
        .count() > 0;
    if(!exists) {
      final var partyRight = ImmutableOrgPartyRight.builder()
          .id(OidUtils.gen())
          .partyId(entry.getId())
          .commitId(commitId)
          .rightId(current.getId())
          .build();
      resultPartyRights.add(partyRight);
      visitChangeTree(commitId, partyRight, OrgOperationType.ADD);
    }
    
    final var currentStatus = currentActorStatus.stream()
        .filter(e -> entry.getId().equals(e.getPartyId()))
        .filter(e -> e.getValue() == OrgActorStatus.OrgActorStatusType.DISABLED)
        .filter(e -> e.getMemberId() == null)
        .findFirst();
    
    if(currentStatus.isEmpty()) {
      return;
    }
    if(currentStatus.get().getValue() == OrgActorStatusType.IN_FORCE) {
      return;
    }
    final var status = ImmutableOrgActorStatus.builder()
        .from(currentStatus.get())
        .commitId(commitId)
        .value(OrgActorStatus.OrgActorStatusType.IN_FORCE)
        .build();
    resultActorStatus.add(status);
    identifiersForUpdates.add(status.getId());
    visitChangeTree(commitId, status, OrgCommitTree.OrgOperationType.MOD);
  }
  
  @Data @RequiredArgsConstructor
  private static class ModMember {
    private final ModType type;
    private final OrgMember member;
  }
  
  @Data @RequiredArgsConstructor
  private static class ModParty {
    private final ModType type;
    private final OrgParty party;
  }
  
  public static class NoRightChangesException extends Exception {
    private static final long serialVersionUID = 3041890960089273165L;
  }
}
