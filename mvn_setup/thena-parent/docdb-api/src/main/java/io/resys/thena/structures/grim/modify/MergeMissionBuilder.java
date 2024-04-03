package io.resys.thena.structures.grim.modify;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.ImmutableGrimCommands;
import io.resys.thena.api.entities.grim.ImmutableGrimMission;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionData;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeGoal;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeMission;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeObjective;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeRemark;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewAssignment;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewLabel;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewLink;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewObjective;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewRemark;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.ImmutableGrimBatchForOne;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.structures.grim.create.NewAssignmentBuilder;
import io.resys.thena.structures.grim.create.NewMissionLabelBuilder;
import io.resys.thena.structures.grim.create.NewMissionLinkBuilder;
import io.resys.thena.structures.grim.create.NewObjectiveBuilder;
import io.resys.thena.structures.grim.create.NewRemarkBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;

public class MergeMissionBuilder implements MergeMission {
  
  private final GrimMissionContainer container;
  private final GrimCommitBuilder logger;
  private final ImmutableGrimBatchForOne.Builder batch;
  private final ImmutableGrimMission.Builder nextMission;
  private final ImmutableGrimMissionData.Builder nextMissionMeta;
  private final String missionId;
  private boolean built;
  
  public MergeMissionBuilder(GrimMissionContainer container, GrimCommitBuilder logger) {
    super();
    this.container = container;
    this.logger = logger;
    this.batch = ImmutableGrimBatchForOne.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.nextMission = ImmutableGrimMission.builder()
        .from(container.getMissions().values().iterator().next());
    this.missionId = container.getMissions().values().iterator().next().getId();
    this.nextMissionMeta = ImmutableGrimMissionData.builder()
        .from(container
            .getData().values().stream()
            .filter(d -> d.getRelation() == null)
            .findFirst().get());
  }
  @Override
  public MergeMission title(String title) {
    this.nextMissionMeta.title(title);
    return this;
  }
  @Override
  public MergeMission description(String description) {
    this.nextMissionMeta.description(description);
    return this;
  }
  @Override
  public MergeMission parentId(String parentId) {
    this.nextMission.parentMissionId(parentId);
    return this;
  }
  @Override
  public MergeMission reporterId(String reporterId) {
    this.nextMission.reporterId(reporterId);
    return this;
  }
  @Override
  public MergeMission status(String status) {
    this.nextMission.missionStatus(status);
    return this;
  }
  @Override
  public MergeMission startDate(LocalDate startDate) {
    this.nextMission.startDate(startDate);
    return this;
  }
  @Override
  public MergeMission dueDate(LocalDate dueDate) {
    this.nextMission.dueDate(dueDate);
    return this;
  }
  @Override
  public MergeMission priority(String priority) {
    this.nextMission.missionPriority(priority);
    return this;
  }

  @Override
  public <T> MergeMission setAllAssignees(List<T> replacments, Function<T, Consumer<NewAssignment>> assignment) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> MergeMission setAllLabels(List<T> replacments, Function<T, Consumer<NewLabel>> label) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> MergeMission setAllLinks(List<T> replacments, Function<T, Consumer<NewLink>> link) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public MergeMission addAssignees(Consumer<NewAssignment> assignment) {
    final var all_assignments = this.batch.build().getAssignments().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewAssignmentBuilder(logger, missionId, null, all_assignments);
    assignment.accept(builder);
    final var built = builder.close();
    this.batch.addAssignments(built);
    return this;
  }

  @Override
  public MergeMission addLabels(Consumer<NewLabel> label) {
    final var all_mission_label = this.batch.build().getMissionLabels().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewMissionLabelBuilder(
        logger, missionId, null, 
        all_mission_label
    );
    
    label.accept(builder);
    final var built = builder.close();
    this.batch.addMissionLabels(built);
    return this;
  }

  @Override
  public MergeMission addLink(Consumer<NewLink> link) {
    final var all_links = this.batch.build().getLinks().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewMissionLinkBuilder(logger, missionId, null, all_links);
    link.accept(builder);
    final var built = builder.close();
    this.batch.addLinks(built);
    return this;
  }

  @Override
  public MergeMission addRemark(Consumer<NewRemark> remark) {
    final var all_remarks = this.batch.build().getRemarks().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewRemarkBuilder(logger, missionId, null, Collections.unmodifiableMap(all_remarks));
    remark.accept(builder);
    final var built = builder.close();
    this.batch.addRemarks(built);
    return this;
  }
  @Override
  public MergeMission addCommands(List<JsonObject> commandToAppend) {
    batch.addCommands(ImmutableGrimCommands.builder()
        .commands(commandToAppend)
        .commitId(logger.getCommitId())
        .missionId(missionId)
        .createdAt(OffsetDateTime.now())
        .id(OidUtils.gen())
        .build());
    return this;
  }
  @Override
  public MergeMission addObjective(Consumer<NewObjective> objective) {
    final var builder = new NewObjectiveBuilder(logger, missionId);
    objective.accept(builder);
    final var built = builder.close();
    this.batch.from(built);
    return this;
  }

  @Override
  public MergeMission modifyGoal(String goalId, Consumer<MergeGoal> goal) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public MergeMission modifyObjective(String objectiveId, Consumer<MergeObjective> objective) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public MergeMission modifyRemark(String remarkId, Consumer<MergeRemark> objective) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public MergeMission removeGoal(String goalId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public MergeMission removeObjective(String objectiveId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public MergeMission removeRemark(String remarkId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutableGrimBatchForOne close() {
    RepoAssert.isTrue(built, () -> "you must call MissionChanges.build() to finalize mission CREATE or UPDATE!");

    // mission meta merge
    {
      var data = this.nextMissionMeta.build();
      final var previous = this.container.getData().get(data.getId());
      final var isModified = !data.equals(previous);
      if(isModified) {
        data = ImmutableGrimMissionData.builder()
            .from(data)
            .commitId(this.logger.getCommitId())
            .build();
        logger.merge(previous, data);
        batch.addUpdateData(data);
      }
    }
    
    // mission merge
    {
      var mission = this.nextMission.build();
      final var previous = this.container.getMissions().get(mission.getId());
      final var isModified = !mission.equals(previous);
      
      if(isModified) {
        mission = ImmutableGrimMission.builder()
            .from(mission)
            .commitId(this.logger.getCommitId())
            .build();
        logger.merge(previous, mission);
        batch.addUpdateMissions(mission);
      }
    }
    
    return batch.build();
  }
}
