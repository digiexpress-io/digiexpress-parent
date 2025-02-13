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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Objects;

import io.resys.thena.api.actions.OrgCommitActions.ModType;
import io.resys.thena.api.entities.org.ImmutableOrgCommit;
import io.resys.thena.api.entities.org.ImmutableOrgMemberRight;
import io.resys.thena.api.entities.org.ImmutableOrgMembership;
import io.resys.thena.api.entities.org.ImmutableOrgParty;
import io.resys.thena.api.entities.org.ImmutableOrgPartyRight;
import io.resys.thena.api.entities.org.OrgActorStatusType;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberRight;
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
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOnePartyModify {
  private final String repoId;
  private final String author;
  private final String message;
  private final List<ModMember> modifyMemberships = new ArrayList<>(); 
  private final List<ModRight> modifyPartyRights = new ArrayList<>();
  private final Map<OrgMember, List<OrgRight>> addMemberWithRights = new LinkedHashMap<>();

  private OrgParty current;
  private OrgCommitBuilder commitBuilder;
  private final ImmutableOrgBatchForOne.Builder batch = ImmutableOrgBatchForOne.builder();
  private final List<OrgPartyRight> currentPartyRights = new ArrayList<>();
  private final List<OrgMemberRight> currentMemberRights = new ArrayList<>();
  private final List<OrgMembership> currentMemberships = new ArrayList<>();

  private Optional<String> newPartyName;
  private Optional<String> newPartyDesc;
  private Optional<String> newExternalId;
  private Optional<OrgParty> newParentPartyId;
  private Optional<OrgDocSubType> newPartySubType;
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
  public BatchForOnePartyModify newPartyName(Optional<String> newPartyName) {
    this.newPartyName = newPartyName; 
    return this; 
  }
  public BatchForOnePartyModify newPartyDesc(Optional<String> newDesc) {
    this.newPartyDesc = newDesc; 
    return this; 
  }
  public BatchForOnePartyModify newPartySubType(Optional<OrgDocSubType> partySubType) {
    this.newPartySubType = partySubType; 
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
    RepoAssert.notNullIfDefined(newPartySubType, () -> "newPartySubType can't be null!");
    
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
    this.commitBuilder = new OrgCommitBuilder(author, ImmutableOrgCommit.builder()
        .commitId(commitId)
        .commitAuthor(author)
        .commitMessage(message)
        .createdAt(createdAt)
        .commitLog("")
        .build());
    
    // party
    visitPartyChanges(commitId);

    
    // parties
    final var memebrShipInParty = this.currentMemberships.stream().collect(Collectors.toMap(e -> e.getMemberId(), e -> e));
    
    for(final var entry : modifyMemberships) {
      final var membership = memebrShipInParty.get(entry.getMember().getId());
      if(entry.getType() == ModType.ADD) {
        this.visitAddMembership(entry.getMember(), membership, commitId);  
      } else if(entry.getType() == ModType.REMOVE) {
        this.visitRemoveMembership(entry.getMember(), membership, commitId);
      } else {
        RepoAssert.fail("Unknown modification type: " + entry.getType() + "!"); 
      }
    }
    
    // rights
    final var rightsInParty = this.currentPartyRights.stream().collect(Collectors.toMap(e -> e.getRightId(), e -> e));
    for(final var entry : modifyPartyRights) {

      final var rights = rightsInParty.get(entry.getRight().getId());
      
      if(entry.getType() == ModType.ADD) {
        this.visitAddRightForParty(entry.getRight(), rights, commitId);  
      } else if(entry.getType() == ModType.REMOVE) {
        this.visitRemoveRightForParty(entry.getRight(), rights, commitId);
      } else {
        RepoAssert.fail("Unknown modification type: " + entry.getType() + "!"); 
      }
    }
    
    final var rightsByRightIdId = this.currentMemberRights.stream().collect(Collectors.groupingBy(OrgMemberRight::getRightId));
    for(final Map.Entry<OrgMember, List<OrgRight>> entry : this.addMemberWithRights.entrySet()) {
      for(final OrgRight role : entry.getValue()) {
        final OrgMember member = entry.getKey();
        final List<OrgMemberRight> rights = Optional.ofNullable(rightsByRightIdId.get(role.getId())).orElse(Collections.emptyList());
        this.visitAddRightForMember(role, rights, member, commitId);
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
      throw new NoPartyChangesException();
    }
    return batch;
  }
  
  
  private Optional<OrgParty> visitPartyChanges(String commitId) {
    final var newState = ImmutableOrgParty.builder()
    .id(current.getId())
    .commitId(commitId)
    .createdWithCommitId(current.getCreatedWithCommitId())
    .externalId(newExternalId == null ? current.getExternalId() : newExternalId.get())
    .partyName(newPartyName == null ? current.getPartyName() : newPartyName.get())
    .partyDescription(newPartyDesc == null  ? current.getPartyDescription() : newPartyDesc.get())
    .parentId(newParentPartyId == null ? current.getParentId() : newParentPartyId.map(e -> e.getId()).orElse(null))
    .partySubType(newPartySubType == null  ? current.getPartySubType() : newPartySubType.get())
    .status(newStatus == null ? current.getStatus() : newStatus)
    .build();
    
    // no changes
    if( Objects.equal(newState.getPartyDescription(), current.getPartyDescription()) && 
        Objects.equal(newState.getExternalId(), current.getExternalId()) &&
        Objects.equal(newState.getParentId(), current.getParentId()) &&
        Objects.equal(newState.getPartySubType(), current.getPartySubType()) &&
        Objects.equal(newState.getStatus(), current.getStatus()) &&
        Objects.equal(newState.getPartyName(), current.getPartyName())
        ) {
      return Optional.empty();
    }
    this.commitBuilder.merge(current, newState);
    batch.addPartiesToUpdate(newState);
    
    return Optional.of(newState);
  }
  
  private void visitRemoveRightForParty(
      OrgRight entry, 
      OrgPartyRight partyRights, 
      String commitId) {
    
    if(partyRights != null) {
      this.batch.addPartyRightToDelete(partyRights);
      this.commitBuilder.rm(partyRights);
    }
    
  }
  
  private void visitAddRightForParty(
      OrgRight entry, 
      OrgPartyRight partyRights, 
      String commitId) {
    
    final var exists = partyRights != null;
    if(!exists) {
      final var membership = ImmutableOrgPartyRight.builder()
          .id(OidUtils.gen())
          .rightId(entry.getId())
          .commitId(commitId)
          .partyId(current.getId())
          .build();
      this.batch.addPartyRights(membership);
      this.commitBuilder.add(membership);
    }
    
  }
  

  
  private void visitRemoveMembership(OrgMember entry, OrgMembership currentMembership, String commitId) {
    
    if(currentMembership != null) {
      this.batch.addMembershipsToDelete(currentMembership);
      this.commitBuilder.rm(currentMembership);
    }
  }
  
  private void visitAddMembership(OrgMember member, OrgMembership currentMembership, String commitId) {
    if(currentMembership == null) {
      final var membership = ImmutableOrgMembership.builder()
          .id(OidUtils.gen())
          .partyId(current.getId())
          .memberId(member.getId())
          .commitId(commitId)
          .build();
      this.batch.addMemberships(membership);
      this.commitBuilder.add(membership);
    }
  }
  
  private void visitAddRightForMember(
      OrgRight entry, 
      List<OrgMemberRight> memberRights, 
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
      this.batch.addMemberRights(membership);
      this.commitBuilder.add(membership);
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
