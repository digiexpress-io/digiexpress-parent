package io.resys.thena.structures.org.modify;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Objects;

import io.resys.thena.api.actions.OrgCommitActions.ModType;
import io.resys.thena.api.entities.org.ImmutableOrgCommit;
import io.resys.thena.api.entities.org.ImmutableOrgMemberRight;
import io.resys.thena.api.entities.org.ImmutableOrgPartyRight;
import io.resys.thena.api.entities.org.ImmutableOrgRight;
import io.resys.thena.api.entities.org.OrgActorStatusType;
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
    
    
    // parties
    for(final var entry : parties) {
      if(entry.getType() == ModType.ADD) {
        this.visitAddRightsToParty(entry.getParty(), commitId);  
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
  
  
  private void visitRightsChanges(String commitId) {
    final var newState = ImmutableOrgRight.builder()
    .id(current.getId())
    .commitId(commitId)
    .createdWithCommitId(current.getCreatedWithCommitId())
    .externalId(newExternalId == null ? current.getExternalId() : newExternalId.get())
    .rightName(newRightName == null ? current.getRightName() : newRightName.get())
    .rightDescription(newRightDescription == null  ? current.getRightDescription() : newRightDescription.get())
    .rightSubType(newRightSubType == null  ? current.getRightSubType() : newRightSubType.get())
    .status(newStatus == null  ? current.getStatus() : newStatus)
    .build();
    
    // no changes
    if( Objects.equal(newState.getRightDescription(), current.getRightDescription()) && 
        Objects.equal(newState.getExternalId(), current.getExternalId()) &&
        Objects.equal(newState.getRightSubType(), current.getRightSubType()) &&
        Objects.equal(newState.getRightName(), current.getRightName()) &&
        Objects.equal(newState.getStatus(), current.getStatus())
        ) {
      return ;
    }
    this.batch.addRightsToUpdate(newState);
    this.commitBuilder.merge(current, newState);
  }
  private void visitRemoveRightsToMember(OrgMember member, String commitId) {
    final var currentMemberRight = currentMemberRights.stream()
        .filter(e -> member.getId().equals(e.getMemberId()))
        .findFirst();
    
    if(currentMemberRight.isPresent()) {
      this.batch.addMemberRightsToDelete(currentMemberRight.get());
      this.commitBuilder.rm(currentMemberRight.get());
    }
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
  }
  
  private void visitRemoveRightFromParty(OrgParty entry, String commitId) {
    final var currentPartyRight = currentPartyRights.stream()
        .filter(e -> entry.getId().equals(e.getPartyId()))
        .findFirst();
    
    if(currentPartyRight.isPresent()) {
      this.batch.addPartyRightToDelete(currentPartyRight.get());
      this.commitBuilder.rm(currentPartyRight.get());
    }
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
