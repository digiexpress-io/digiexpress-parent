package io.resys.thena.structures.org.modify;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Objects;

import io.resys.thena.api.actions.OrgCommitActions.ModType;
import io.resys.thena.api.entities.org.ImmutableOrgActorStatus;
import io.resys.thena.api.entities.org.ImmutableOrgCommit;
import io.resys.thena.api.entities.org.ImmutableOrgMemberRight;
import io.resys.thena.api.entities.org.ImmutableOrgPartyRight;
import io.resys.thena.api.entities.org.ImmutableOrgRight;
import io.resys.thena.api.entities.org.OrgActorStatus;
import io.resys.thena.api.entities.org.OrgActorStatus.OrgActorStatusType;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgDocSubType;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.org.ImmutableOrgBatchForOne;
import io.resys.thena.structures.org.commitlog.OrgCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOneRightModify {
  private final String repoId;
  private final String author;
  private final String message;
  private final List<ModParty> parties = new ArrayList<>(); 
  private final List<ModMember> members = new ArrayList<>();
  
  private List<OrgPartyRight> currentPartyRights;
  private List<OrgMemberRight> currentMemberRights; 
  private List<OrgActorStatus> currentActorStatus;  
  private OrgRight current;
  private OrgCommitBuilder commitBuilder;
  private final ImmutableOrgBatchForOne.Builder batch = ImmutableOrgBatchForOne.builder();
  
  private Optional<String> newRightName;
  private Optional<String> newRightDescription;
  private Optional<String> newExternalId;
  private OrgActorStatusType newStatus;
  private Optional<OrgDocSubType> newRightSubType;

  public BatchForOneRightModify current(OrgRight right) {                          this.current = right; return this; }
  public BatchForOneRightModify newRightDescription(Optional<String> rightDesc) {  this.newRightDescription = rightDesc; return this; }
  public BatchForOneRightModify newRightName(Optional<String> rightName) {         this.newRightName = rightName; return this; }
  public BatchForOneRightModify newExternalId(Optional<String> externalId) {       this.newExternalId = externalId; return this; }
  public BatchForOneRightModify newStatus(OrgActorStatusType newStatus) {          this.newStatus = newStatus; return this; }
  public BatchForOneRightModify newRightSubType(Optional<OrgDocSubType> newRightSubType) {   this.newRightSubType = newRightSubType; return this; }

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
    RepoAssert.notNullIfDefined(newRightSubType,  () -> "newRightSubType can't be null!");
    
    final var commitId = OidUtils.gen();
    final var createdAt = OffsetDateTime.now();
    this.commitBuilder = new OrgCommitBuilder(author, ImmutableOrgCommit.builder()
        .commitId(commitId)
        .commitAuthor(author)
        .commitMessage(message)
        .createdAt(createdAt)
        .commitLog("")
        .build());
    
    // right
    visitRightsChanges(commitId);
    
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

    
    
    final var commit = this.commitBuilder.close();

    final var batch = this.batch
      .repoId(repoId)
      .status(BatchStatus.OK)
      .commit(commit.getItem1())
      .commitTrees(commit.getItem2())
      .log(commit.getItem1().getCommitLog())
      .build();
    
    // no changes
    if(batch.isEmpty()) {
      throw new NoRightChangesException();
    }
    return batch;
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
      this.batch.addActorStatus(newStatus);
      this.commitBuilder.add(newStatus);
    } else {
      final var newStatus = ImmutableOrgActorStatus.builder().from(status).value(this.newStatus).build();
      this.batch.addActorStatusToUpdate(newStatus);      
      this.commitBuilder.merge(status, newStatus);
    }
  }
  
  private void visitRightsChanges(String commitId) {
    final var newState = ImmutableOrgRight.builder()
    .id(current.getId())
    .commitId(commitId)
    .createdWithCommitId(current.getCreatedWithCommitId())
    .externalId(newExternalId == null ? current.getExternalId() : newExternalId.get())
    .rightName(newRightName == null ? current.getRightName() : newRightName.get())
    .rightDescription(newRightDescription == null  ? current.getRightDescription() : newRightDescription.get())
    .rightSubType(newRightSubType == null  ? current.getRightSubType() : newRightSubType.get())
    .build();
    
    // no changes
    if( Objects.equal(newState.getRightDescription(), current.getRightDescription()) && 
        Objects.equal(newState.getExternalId(), current.getExternalId()) &&
        Objects.equal(newState.getRightSubType(), current.getRightSubType()) &&
        Objects.equal(newState.getRightName(), current.getRightName())
        ) {
      return ;
    }
    this.batch.addRightsToUpdate(newState);
    this.commitBuilder.merge(current, newState);
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
      this.batch.addMemberRightsToDelete(currentMemberRight.get());
      this.commitBuilder.rm(currentMemberRight.get());
    }
    if(currentStatus.isPresent()) {
      this.batch.addStatusToDelete(currentStatus.get());
      this.commitBuilder.rm(currentStatus.get());
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
      this.batch.addActorStatus(status);
      this.commitBuilder.add(status);
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
    
    this.batch.addActorStatusToUpdate(status);
    this.commitBuilder.merge(disabled.get(), status);
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
      this.batch.addMemberRights(membership);
      this.commitBuilder.add(membership);
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
    this.batch.addActorStatusToUpdate(status);
    this.commitBuilder.merge(currentStatus.get(), status);
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
      this.batch.addPartyRightToDelete(currentPartyRight.get());
      this.commitBuilder.rm(currentPartyRight.get());
    }
    if(currentStatus.isPresent()) {
      this.batch.addStatusToDelete(currentStatus.get());
      this.commitBuilder.rm(currentStatus.get());
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
      this.batch.addActorStatus(status);
      this.commitBuilder.add(status);
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
    this.batch.addActorStatusToUpdate(status);
    this.commitBuilder.merge(currentStatus.get(), status);
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
      this.batch.addPartyRights(partyRight);
      this.commitBuilder.add(partyRight);
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
    this.batch.addActorStatus(status);
    this.commitBuilder.merge(currentStatus.get(), status);
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
