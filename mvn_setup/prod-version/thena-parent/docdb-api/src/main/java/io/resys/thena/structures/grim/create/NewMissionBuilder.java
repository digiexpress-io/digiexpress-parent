package io.resys.thena.structures.grim.create;

import java.text.SimpleDateFormat;

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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.ImmutableGrimCommands;
import io.resys.thena.api.entities.grim.ImmutableGrimMission;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionContainer;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionData;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionTransitives;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewAssignment;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewLabel;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewLink;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewMission;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewMissionCommitViewer;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewObjective;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewRemark;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.ImmutableGrimBatchMissions;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;



public class NewMissionBuilder implements ThenaGrimNewObject.NewMission {
  private final GrimCommitBuilder logger;
  private final ImmutableGrimMission.Builder mission;
  private final String missionId;
  private final String commitId;
  private final ImmutableGrimMissionData.Builder missionMeta;
  private final OffsetDateTime createdAt;
  private static final String DATE_NUMBER_SEPARATOR_DEFAULT = "-";
  private static final SimpleDateFormat dataFormat = new SimpleDateFormat("yyyyMM");
  
  private ImmutableGrimBatchMissions.Builder next;
  private Consumer<GrimMissionContainer> handleNewState;
  private boolean built;
  
  
  public NewMissionBuilder(GrimCommitBuilder logger, long nextVal) {
    super();
    this.next = ImmutableGrimBatchMissions.builder()
        .tenantId(logger.getTenantId())
        .status(BatchStatus.OK)
        .log("");

    this.createdAt = logger.getCreatedAt();
    this.commitId = logger.getCommitId();
    this.missionId = OidUtils.gen();
    this.mission = ImmutableGrimMission.builder()
        .id(missionId)
        .commitId(commitId)
        .updatedTreeWithCommitId(commitId)
        .createdWithCommitId(commitId)
        .title("")
        .description("")
        .refId(generateTaskRef(nextVal));
    this.missionMeta = ImmutableGrimMissionData.builder()
      .id(OidUtils.gen())
      .createdWithCommitId(logger.getCommitId())
      .commitId(commitId)
      .missionId(missionId);
        
    this.logger = logger;
  }
  
  public String generateTaskRef(long nextVal) {
    final Date now = new Date();
    return dataFormat.format(now) + DATE_NUMBER_SEPARATOR_DEFAULT + nextVal;
  }
  @Override
  public NewMission questionnaireId(String questionnaireId) {
    this.mission.questionnaireId(questionnaireId);
    return this;
  }
  @Override
  public NewMission title(String title) {
    this.mission.title(title);
    return this;
  }
  @Override
  public NewMission description(String description) {
    this.mission.description(description);
    return this;
  }
  @Override
  public NewMission parentId(String parentId) {
    this.mission.parentMissionId(parentId);
    return this;
  }
  @Override
  public NewMission reporterId(String reporterId) {
    this.mission.reporterId(reporterId);
    return this;
  }
  @Override
  public NewMission status(String status) {
    this.mission.missionStatus(status);
    return this;
  }
  @Override
  public NewMission startDate(LocalDate startDate) {
    this.mission.startDate(startDate);
    return this;
  }
  @Override
  public NewMission dueDate(LocalDate dueDate) {
    this.mission.dueDate(dueDate);
    return this;
  }
  @Override
  public NewMission priority(String priority) {
    this.mission.missionPriority(priority);
    return this;
  }
  @Override
  public NewMission addViewer(Consumer<NewMissionCommitViewer> viewer) {
    final var delegate = new NewMissionCommitViewerBuilder(createdAt, missionId, commitId);
    viewer.accept(delegate);
    final var viewed = delegate.close();
    this.next.addCommitViewers(viewed);
    return this;
  }
  @Override
  public NewMission addAssignees(Consumer<NewAssignment> assignment) {
    final var all_assignments = this.next.build().getAssignments().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewAssignmentBuilder(logger, missionId, null, all_assignments);
    assignment.accept(builder);
    final var built = builder.close();
    this.next.addAssignments(built);
    return this;
  }
  @Override
  public NewMission addLabels(Consumer<NewLabel> label) {
    final var all_mission_label = this.next.build().getMissionLabels().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewMissionLabelBuilder(
        logger, missionId, null, 
        all_mission_label
    );
    
    label.accept(builder);
    final var built = builder.close();
    this.next.addMissionLabels(built);
    
    return this;
  }
  @Override
  public NewMission addLink(Consumer<NewLink> link) {
    final var all_links = this.next.build().getLinks().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewMissionLinkBuilder(logger, missionId, null, all_links);
    link.accept(builder);
    final var built = builder.close();
    this.next.addLinks(built);
    return this;
  }
  @Override
  public NewMission addRemark(Consumer<NewRemark> remark) {
    final var all_remarks = this.next.build().getRemarks().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewRemarkBuilder(logger, missionId, null, Collections.unmodifiableMap(all_remarks));
    remark.accept(builder);
    final var built = builder.close();
    this.next.from(built);
    return this;
  }
  @Override
  public NewMission addObjective(Consumer<NewObjective> objective) {
    final var builder = new NewObjectiveBuilder(logger, missionId);
    
    objective.accept(builder);
    final var built = builder.close();
    this.next.from(built);
    return this;
  }
  @Override
  public void build() {
    this.built = true;
  }
  @Override
  public NewMission addCommands(List<JsonObject> commandToAppend) {
    next.addCommands(ImmutableGrimCommands.builder()
        .commands(commandToAppend)
        .commitId(logger.getCommitId())
        .missionId(missionId)
        .createdAt(createdAt)
        .id(OidUtils.gen())
        .build());
    return this;
  }
  @Override
  public NewMission onNewState(Consumer<GrimMissionContainer> handleNewState) {
    this.handleNewState = handleNewState;
    return this;
  }
  public ImmutableGrimBatchMissions close() {
    RepoAssert.isTrue(built, () -> "you must call MissionChanges.build() to finalize mission CREATE or UPDATE!");

    final var data = this.missionMeta.build();
    final var mission = this.mission
        .transitives(ImmutableGrimMissionTransitives.builder()
            .createdAt(createdAt)
            .updatedAt(createdAt)
            .treeUpdatedAt(createdAt)
            .treeUpdatedBy(logger.getAuthor())
            .dataExtension(Optional.ofNullable(data.getDataExtension()).orElse(null))
            .build())
        .build();
    
    logger.add(mission);
    
    next.addMissions(mission);
    if(data.getDataExtension() != null) {
      logger.add(data);
      next.addData(data);
    }
    final var batch = next.build();
    
    onNewState(batch);
    
    return batch;
  }
  
  private void onNewState(ImmutableGrimBatchMissions batch) {
    if(handleNewState == null) {
      return;
    }
    final var mission = batch.getMissions().iterator().next();
    final var builders = ImmutableGrimMissionContainer.builder().putMissions(mission.getId(), mission);

    batch.getMissionLabels().forEach(label -> builders.putMissionLabels(label.getId(), label));
    batch.getLinks().forEach(link -> builders.putLinks(link.getId(), link));
    batch.getRemarks().forEach(remark -> builders.putRemarks(remark.getId(), remark));
    batch.getObjectives().forEach(objective -> builders.putObjectives(objective.getId(), objective));
    batch.getGoals().forEach(goals -> builders.putGoals(goals.getId(), goals));
    batch.getData().forEach(data -> builders.putData(data.getId(), data));
    batch.getAssignments().forEach(assignment -> builders.putAssignments(assignment.getId(), assignment));
    batch.getCommands().forEach(commands -> builders.putCommands(commands.getId(), commands));
    batch.getCommits().forEach(commit -> builders.putCommits(commit.getCommitId(), commit));
    final var container = builders.build();
    handleNewState.accept(container);
  }
}
