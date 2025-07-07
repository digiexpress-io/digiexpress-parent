package io.resys.thena.grim.spi;

/*-
 * #%L
 * thena-grim-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.api.entities.PageQuery.PageSortingOrder;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.grim.GrimAnyObject;
import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.api.entities.grim.GrimCommands;
import io.resys.thena.api.entities.grim.GrimCommit;
import io.resys.thena.api.entities.grim.GrimCommitTree;
import io.resys.thena.api.entities.grim.GrimCommitViewer;
import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.GrimMissionData;
import io.resys.thena.api.entities.grim.GrimMissionLabel;
import io.resys.thena.api.entities.grim.GrimMissionLink;
import io.resys.thena.api.entities.grim.GrimMissionStats.GrimMissionAttributeEvent;
import io.resys.thena.api.entities.grim.GrimObjective;
import io.resys.thena.api.entities.grim.GrimObjectiveGoal;
import io.resys.thena.api.entities.grim.GrimProcess;
import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.entities.grim.GrimUniqueMissionLabel;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.datasource.ThenaDataSource;
import io.resys.thena.grim.api.GrimQueryActions.GrimArchiveQueryType;
import io.resys.thena.grim.api.GrimQueryActions.MissionOrderByType;
import io.resys.thena.grim.spi.datasource.GrimCommitViewerRegistry.AnyObjectCriteria;
import io.resys.thena.spi.TenantDataSource;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface GrimDataSource extends TenantDataSource {
  Uni<GrimState> toGrimState(String tenantId);
  GrimState toGrimState(Tenant repo);
  <R> Uni<R> withGrimTransaction(TxScope tenantId, TransactionFunction<R> callback);
  
  
  interface GrimState {
    String getTenantId();
    ThenaDataSource getDataSource();
    <R> Uni<R> withTransaction(TransactionFunction<R> callback);
    
    
    Uni<GrimBatchMissions> batchMany(GrimBatchMissions output);
    Uni<GrimBatchForViewers> batchMany(GrimBatchForViewers output);

    InternalMissionQuery missions();
    InternalMissionLabelQuery missionLabels();
    InternalMissionStatsQuery missionStats();
    
    InternalCommitViewerQuery commitViewer();
    InternalCommitTreeQuery commitTree();
    InternalCommitQuery commit();
    InternalProcQuery missionProcs();
    InternalMissionSequence missionSequences();
    InternalMissionRemarkQuery missionRemarks();
  }
  
  @FunctionalInterface
  interface TransactionFunction<R> {
    Uni<R> apply(GrimState repoState);
  }
  
  interface InternalCommitTreeQuery {
    Uni<List<GrimCommitTree>> findAll();
    Uni<List<GrimCommitTree>> findAllByMissionId(String missionId);
  }
  
  interface InternalProcQuery {
    Uni<GrimProcess> getOneById(String id);
    Multi<GrimProcess> findOnOrAfter(OffsetDateTime onOrAfter);
    Multi<GrimProcess> findOnOrBeforeWithoutMission(OffsetDateTime onOrBefore);
  }
  
  interface InternalCommitQuery {
    Uni<List<GrimCommit>> findAll();
    Uni<List<GrimCommit>> findAllByMissionId(String missionId);
  }
  
  interface InternalCommitViewerQuery {
    Multi<GrimAnyObject> findAnyObjects(Collection<AnyObjectCriteria> commits);
    Multi<GrimCommitViewer> findAllViewersByUsed(String userId, String usedBy, Collection<String> commits);
    Multi<GrimCommitViewer> findAllViewersInDuration(
      @Nullable String usedBy, 
      @Nullable String usedFor, 
      @Nullable Duration duration,
      @Nullable String missionId
    );
  }
  
  interface InternalMissionRemarkQuery {
    Uni<GrimMissionContainer> getOneByRemarkId(String remarkId);    
    Uni<GrimMissionContainer> findAllByMissionId(String missionId);
    Uni<GrimMissionContainer> findAllByReporterId(String reporterId);
  }
  interface InternalMissionSequence {
    Uni<Long> nextVal();
    Uni<List<Long>> nextVal(long howMany);
  }

  interface InternalMissionLabelQuery {
    Uni<List<GrimUniqueMissionLabel>> findAllUnique();
  }    
  
  interface InternalMissionStatsQuery {
    Uni<List<GrimMissionAttributeEvent>> findAllByMissionAttributes(List<String> assigneeGroups);    
    
  }    
  
  interface InternalMissionQuery {
    
    InternalMissionQuery notViewed(@Nullable String userId, String usedFor);
    InternalMissionQuery includeViewer(String usedBy, String usedFor);
    InternalMissionQuery lockForUpdate();
    
    InternalMissionQuery onlyDocs(GrimDocType ...docs);
    
    InternalMissionQuery excludeDocs(GrimDocType ...docs); // multiple will be OR
    InternalMissionQuery archived(GrimArchiveQueryType includeArchived); // true to exclude any tasks with archiveAt date present
    InternalMissionQuery missionId(String ...missionId); // multiple will be OR
    InternalMissionQuery addAssignment(String assignmentType, boolean isExact, List<String> assignmentValue); // multiple will be OR
    InternalMissionQuery addLink(String linkType, String linkValue); // multiple will be OR
    
    InternalMissionQuery status(String ...status);
    InternalMissionQuery priority(String ...priority);
    InternalMissionQuery overdue(Boolean overdue);// false = mission.mission_due_date < CURRENT_DATE
    InternalMissionQuery atLeastOneRemarkWithType(String remarkType);
    InternalMissionQuery atLeastOneRemarkWithAnyType(Boolean includeAny);
    
    InternalMissionQuery likeReporterId(String reporterId);
    InternalMissionQuery likeTitle(String likeTitle);
    InternalMissionQuery likeDescription(String likeDescription);
    InternalMissionQuery fromCreatedOrUpdated(LocalDate fromCreatedOrUpdated);
    
    
    Uni<List<String>> findAllIdentifiers(List<PageSortingOrder<MissionOrderByType>> orderBy, long offset, long limit);
    Multi<GrimMissionContainer> findAll();
    Uni<GrimMissionContainer> getById(String missionId);
    Uni<Long> count();
  }

  
  @Value.Immutable
  interface GrimBatchForViewers {
    List<GrimCommitViewer> getViewers();
    List<GrimCommitViewer> getUpdateViewers();
    String getLog();
    List<Message> getMessages();
    BatchStatus getStatus();
    String getTenantId();
  }
  
  
  @Value.Immutable
  interface GrimBatchMissions {
    List<GrimMission> getMissions();
    List<GrimProcess> getProcs();
    List<GrimMissionLabel> getMissionLabels();
    List<GrimMissionLink> getLinks();
    List<GrimRemark> getRemarks();
    List<GrimObjective> getObjectives();
    List<GrimObjectiveGoal> getGoals();
    List<GrimMissionData> getData();
    List<GrimAssignment> getAssignments();
    List<GrimCommands> getCommands();
    
    // Commit related
    List<GrimCommit> getCommits();
    List<GrimCommitTree> getCommitTrees();
    List<GrimCommitViewer> getCommitViewers();
    
    // Objects to update
    List<GrimProcess> getUpdateProcs();
    List<GrimMissionData> getUpdateData();
    List<GrimRemark> getUpdateRemarks();
    List<GrimObjectiveGoal> getUpdateGoals();
    List<GrimObjective> getUpdateObjectives();
    List<GrimMission> getUpdateMissions();
    List<GrimMissionLink> getUpdateLinks();

    // Objects to delete
    List<GrimAssignment> getDeleteAssignments();
    List<GrimMissionLink> getDeleteLinks();
    List<GrimMissionLabel> getDeleteMissionLabels();
    List<GrimRemark> getDeleteRemarks();
    List<GrimObjective> getDeleteObjectives();
    List<GrimMissionData> getDeleteData();
    List<GrimObjectiveGoal> getDeleteGoals();
    
    BatchStatus getStatus();
    String getTenantId();

    String getLog();
    List<Message> getMessages();
    
    @JsonIgnore
    default boolean isEmpty() {
      return 
        this.getMissions().isEmpty() &&
        this.getProcs().isEmpty() &&
        this.getMissionLabels().isEmpty() &&
        this.getLinks().isEmpty() &&
        this.getRemarks().isEmpty() &&
        this.getObjectives().isEmpty() &&
        this.getGoals().isEmpty() &&
        this.getData().isEmpty() &&
        this.getAssignments().isEmpty() &&
        this.getCommands().isEmpty() &&
        
        // Objects to update
        this.getUpdateData().isEmpty() &&
        this.getUpdateRemarks().isEmpty() &&
        this.getUpdateGoals().isEmpty() &&
        this.getUpdateObjectives().isEmpty() &&
        this.getUpdateMissions().isEmpty() &&
  
        // Objects to delete
        this.getDeleteAssignments().isEmpty() &&
        this.getDeleteLinks().isEmpty() &&
        this.getDeleteMissionLabels().isEmpty() &&
        this.getDeleteRemarks().isEmpty() &&
        this.getDeleteObjectives().isEmpty() &&
        this.getDeleteGoals().isEmpty();
    } 
  }
}
