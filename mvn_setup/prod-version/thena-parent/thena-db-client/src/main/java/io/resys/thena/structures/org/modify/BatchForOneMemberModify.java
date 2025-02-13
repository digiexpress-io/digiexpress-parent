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
import io.resys.thena.api.entities.org.ImmutableOrgMember;
import io.resys.thena.api.entities.org.ImmutableOrgMemberRight;
import io.resys.thena.api.entities.org.ImmutableOrgMembership;
import io.resys.thena.api.entities.org.OrgActorStatusType;
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
    
    
    // parties
    final var memebrShipInParty = this.currentMemberships.stream().filter(g -> g.getPartyId() != null).collect(Collectors.toMap(e -> e.getPartyId(), e -> e));
    for(final var entry : modifyMemberships) {
      final var membership = memebrShipInParty.get(entry.getGroup().getId());
      if(entry.getType() == ModType.ADD) {
        this.visitAddMembership(entry.getGroup(), membership, commitId);  
      } else if(entry.getType() == ModType.REMOVE) {
        this.visitRemoveMembership(entry.getGroup(), membership, commitId);
      } else {
        RepoAssert.fail("Unknown modification type: " + entry.getType() + "!"); 
      }
    }
    
    // rights
    
    final var rightsByRightIdId = this.currentMemberRights.stream().collect(Collectors.groupingBy(OrgMemberRight::getRightId));
    for(final var entry : modifyMemberRights) {
      final var rights = Optional.ofNullable(rightsByRightIdId.get(entry.getRole().getId())).orElse(Collections.emptyList());
      if(entry.getType() == ModType.ADD) {
        this.visitAddRightForMember(entry.getRole(), rights, Optional.empty(), commitId);  
      } else if(entry.getType() == ModType.REMOVE) {
        this.visitRemoveRightForMember(entry.getRole(), rights, Optional.empty(), commitId);
      } else {
        RepoAssert.fail("Unknown modification type: " + entry.getType() + "!"); 
      }
    }
    
    for(final var entry : this.addToPartyWithRights.entrySet()) {
      for(final OrgRight role : entry.getValue()) {
        final var party = Optional.of(entry.getKey());
        final var rights = Optional.ofNullable(rightsByRightIdId.get(role.getId())).orElse(Collections.emptyList());

        this.visitAddRightForMember(role, rights, party, commitId);
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
    .status(newStatus == null ? current.getStatus() : newStatus)
    .build();
    
    // no changes
    if( Objects.equal(newState.getEmail(), current.getEmail()) && 
        Objects.equal(newState.getExternalId(), current.getExternalId()) &&
        Objects.equal(newState.getStatus(), current.getStatus()) &&
        Objects.equal(newState.getUserName(), current.getUserName())
        ) {
      return;
    }
    this.commitBuilder.merge(current, newState);
    this.batch.addMembersToUpdate(newState);
  }

  private void visitRemoveRightForMember(
      OrgRight entry, 
      List<OrgMemberRight> memberRights,  
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
  }
  
  private void visitAddRightForMember(
      OrgRight entry, 
      List<OrgMemberRight> memberRights, 
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
    
  }

  
  private void visitRemoveMembership(OrgParty entry, OrgMembership currentMembership, String commitId) {
    if(currentMembership != null) {
      this.batch.addMembershipsToDelete(currentMembership);
      this.commitBuilder.rm(currentMembership);
    }
  }
  
  private void visitAddMembership(OrgParty entry, OrgMembership currentMembership, String commitId) {
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
