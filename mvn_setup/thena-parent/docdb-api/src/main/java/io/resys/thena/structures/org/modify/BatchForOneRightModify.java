package io.resys.thena.structures.org.modify;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Objects;

import io.resys.thena.api.actions.OrgCommitActions.ModType;
import io.resys.thena.api.models.ImmutableMessage;
import io.resys.thena.api.models.ImmutableOrgActorStatus;
import io.resys.thena.api.models.ImmutableOrgCommit;
import io.resys.thena.api.models.ImmutableOrgCommitTree;
import io.resys.thena.api.models.ImmutableOrgMemberRight;
import io.resys.thena.api.models.ImmutableOrgPartyRight;
import io.resys.thena.api.models.ImmutableOrgRight;
import io.resys.thena.api.models.ThenaOrgObject.IsOrgObject;
import io.resys.thena.api.models.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.api.models.ThenaOrgObject.OrgCommitTree;
import io.resys.thena.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.api.models.ThenaOrgObject.OrgMemberRight;
import io.resys.thena.api.models.ThenaOrgObject.OrgOperationType;
import io.resys.thena.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.api.models.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.api.models.ThenaOrgObject.OrgRight;
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
  
  private List<OrgPartyRight> inputPartyRights;
  private List<OrgMemberRight> inputMemberRights; 
  private List<OrgActorStatus> inputRightStatus; 
 
  private OrgRight current;

  private Optional<String> rightName;
  private Optional<String> rightDescription;
  private Optional<String> externalId;

  public BatchForOneRightModify current(OrgRight right) {                       this.current = right; return this; }
  public BatchForOneRightModify rightDescription(Optional<String> rightDesc) {  this.rightDescription = rightDesc; return this; }
  public BatchForOneRightModify rightName(Optional<String> rightName) {         this.rightName = rightName; return this; }
  public BatchForOneRightModify externalId(Optional<String> externalId) {       this.externalId = externalId; return this; }

  public BatchForOneRightModify partyRights(List<OrgPartyRight> partyRights) {    this.inputPartyRights = partyRights; return this; }
  public BatchForOneRightModify rightStatus(List<OrgActorStatus> rightStatus) {   this.inputRightStatus = rightStatus; return this; }
  public BatchForOneRightModify memberRights(List<OrgMemberRight> memberRights) { this.inputMemberRights = memberRights; return this; }

  
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
    RepoAssert.notEmptyIfDefined(rightName,        () -> "rightName can't be empty!");
    RepoAssert.notEmptyIfDefined(rightDescription, () -> "rightDescription can't be empty!");
    RepoAssert.notNull(parties, () -> "parties can't be null!");
    RepoAssert.notNull(members,   () -> "roles can't be null!");
    RepoAssert.notNull(inputPartyRights,   () -> "partyRights can't be null!");
    RepoAssert.notNull(inputRightStatus,   () -> "rightStatus can't be null!");
    RepoAssert.notNull(inputMemberRights,  () -> "memberRights can't be null!");
    
    
    final var commitId = OidUtils.gen();
    final var createdAt = OffsetDateTime.now();
    
    // user
    final var right = visitRightsChanges(commitId);
    
    // parties
    for(final var entry : parties) {
      if(entry.getType() == ModType.ADD) {
        this.visitAddRightsToParty(entry.getParty(), commitId);  
      } else if(entry.getType() == ModType.DISABLED) {
        this.visitDisableRightFromParty(entry.getParty(), commitId);
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
          .filter(g -> g.getType() == ModType.DISABLED)
          .map(g -> g.getParty().getPartyName() + "::" + g.getParty().getId())
          .toList()))
      .append(System.lineSeparator())
      
      .append("  + added to members:  ").append(String.join(",", members.stream()
          .filter(g -> g.getType() == ModType.ADD)
          .map(g -> g.getMember().getUserName() + "::" + g.getMember().getId())
          .toList()))
      
      .append("  + removed from members:  ").append(String.join(",", members.stream()
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
      .log(ImmutableMessage.builder().text(logger.toString()).build())
      .build();
    
    // no changes
    if(resultActorStatus.isEmpty() && resultPartyRights.isEmpty() && resultMemberRights.isEmpty() && batch.getRights().isEmpty()) {
      throw new NoRightChangesException();
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
  
  private Optional<OrgRight> visitRightsChanges(String commitId) {
    final var newState = ImmutableOrgRight.builder()
    .id(current.getId())
    .commitId(commitId)
    .externalId(externalId == null ? current.getExternalId() : externalId.get())
    .rightName(rightName == null ? current.getRightName() : rightName.get())
    .rightDescription(rightDescription == null  ? current.getRightDescription() : rightDescription.get())
    .build();
    
    // no changes
    if( Objects.equal(newState.getRightDescription(), current.getRightDescription()) && 
        Objects.equal(newState.getExternalId(), current.getExternalId()) &&
        Objects.equal(newState.getRightName(), current.getRightName())
        ) {
      return Optional.empty();
    }
    visitChangeTree(commitId, newState, OrgOperationType.MOD);
    identifiersForUpdates.add(current.getId());
    return Optional.of(newState);
  }

  private void visitDisableRightsToMember(OrgMember member, String commitId) {
    final var disabled = inputRightStatus.stream()
        .filter(e -> member.getId().equals(e.getMemberId()))
        .filter(e -> e.getPartyId() == null)
        .findFirst();
    
    if(disabled.isEmpty()) {
      final var status = ImmutableOrgActorStatus.builder()
          .id(OidUtils.gen())
          .memberId(member.getId())
          .commitId(commitId)
          .rightId(current.getId())
          .value(OrgActorStatusType.DISABLED)
          .build();
      resultActorStatus.add(status);
      visitChangeTree(commitId, status, OrgOperationType.ADD);
      return;
    }

    // already removed
    if(disabled.get().getValue() == OrgActorStatusType.DISABLED) {
      return;
    }

    final var status = ImmutableOrgActorStatus.builder()
        .from(disabled.get())
        .commitId(commitId)
        .value(OrgActorStatusType.DISABLED)
        .build();
    identifiersForUpdates.add(status.getId());
    resultActorStatus.add(status);
    visitChangeTree(commitId, status, OrgOperationType.MOD);
  }
  
  private void visitAddRightsToMember(OrgMember entry, String commitId) {
    if(inputMemberRights.stream().filter(e -> e.getMemberId().equals(entry.getId())).count() == 0) {
      final var membership = ImmutableOrgMemberRight.builder()
          .id(OidUtils.gen())
          .rightId(current.getId())
          .memberId(entry.getId())
          .commitId(commitId)
          .build();
      resultMemberRights.add(membership);
      visitChangeTree(commitId, membership, OrgOperationType.ADD);
    }
    
    
    final var disabled = inputRightStatus.stream()
        .filter(e -> e.getPartyId() == null)
        .filter(e -> e.getValue() == OrgActorStatusType.DISABLED)
        .filter(e -> entry.getId().equals(e.getMemberId()))
        .findFirst();
    if(disabled.isEmpty()) {
      return;
    }
    
    final var status = ImmutableOrgActorStatus.builder()
        .from(disabled.get())
        .commitId(commitId)
        .value(OrgActorStatusType.IN_FORCE)
        .build();
    resultActorStatus.add(status);
    identifiersForUpdates.add(status.getId());
    visitChangeTree(commitId, status, OrgOperationType.MOD);
  }
  
  private void visitDisableRightFromParty(OrgParty entry, String commitId) {
    final var disabled = inputRightStatus.stream()
        .filter(e -> entry.getId().equals(e.getPartyId()))
        .filter(e -> e.getMemberId() == null)
        .findFirst();
    
    if(disabled.isEmpty()) {
      final var status = ImmutableOrgActorStatus.builder()
          .id(OidUtils.gen())
          .partyId(entry.getId())
          .commitId(commitId)
          .rightId(current.getId())
          .value(OrgActorStatusType.DISABLED)
          .build();
      resultActorStatus.add(status);
      visitChangeTree(commitId, status, OrgOperationType.ADD);
      return;
    }

    // already removed
    if(disabled.get().getValue() == OrgActorStatusType.DISABLED) {
      return;
    }

    final var status = ImmutableOrgActorStatus.builder()
        .from(disabled.get())
        .commitId(commitId)
        .value(OrgActorStatusType.DISABLED)
        .build();
    identifiersForUpdates.add(status.getId());
    resultActorStatus.add(status);
    visitChangeTree(commitId, status, OrgOperationType.MOD);
  }
  
  private void visitAddRightsToParty(OrgParty entry, String commitId) {
    if(inputPartyRights.stream().filter(e -> e.getPartyId().equals(entry.getId())).count() == 0) {
      final var partyRight = ImmutableOrgPartyRight.builder()
          .id(OidUtils.gen())
          .partyId(entry.getId())
          .commitId(commitId)
          .rightId(current.getId())
          .build();
      resultPartyRights.add(null);
      visitChangeTree(commitId, partyRight, OrgOperationType.ADD);
    }
    
    final var disabled = inputRightStatus.stream()
        .filter(e -> entry.getId().equals(e.getPartyId()))
        .filter(e -> e.getValue() == OrgActorStatusType.DISABLED)
        .filter(e -> e.getMemberId() == null)
        .findFirst();
    if(disabled.isEmpty()) {
      return;
    }
    final var status = ImmutableOrgActorStatus.builder()
        .from(disabled.get())
        .commitId(commitId)
        .value(OrgActorStatusType.IN_FORCE)
        .build();
    resultActorStatus.add(status);
    identifiersForUpdates.add(status.getId());
    visitChangeTree(commitId, status, OrgOperationType.MOD);
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
