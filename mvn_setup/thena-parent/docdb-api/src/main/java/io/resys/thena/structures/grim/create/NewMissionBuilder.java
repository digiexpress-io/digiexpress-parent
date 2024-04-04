package io.resys.thena.structures.grim.create;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.ImmutableGrimCommands;
import io.resys.thena.api.entities.grim.ImmutableGrimMission;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionData;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewAssignment;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewLabel;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewLink;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewMission;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewObjective;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewRemark;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.ImmutableGrimBatchForOne;
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
  
  private ImmutableGrimBatchForOne.Builder next;
  private boolean built;
  
  public NewMissionBuilder(GrimCommitBuilder logger) {
    super();
    this.next = ImmutableGrimBatchForOne.builder()
        .tenantId(logger.getTenantId())
        .status(BatchStatus.OK)
        .log("");
    this.commitId = logger.getCommitId();
    this.missionId = OidUtils.gen();
    this.mission = ImmutableGrimMission.builder()
        .id(missionId)
        .commitId(commitId)
        .updatedTreeWithCommitId(commitId)
        .createdWithCommitId(commitId);
    this.missionMeta = ImmutableGrimMissionData.builder()
      .id(OidUtils.gen())
      .commitId(commitId)
      .missionId(missionId)
      .title("")
      .description("");
    this.logger = logger;
  }

  @Override
  public NewMission title(String title) {
    this.missionMeta.title(title);
    return this;
  }
  @Override
  public NewMission description(String description) {
    this.missionMeta.title(description);
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
    this.next.addRemarks(built);
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
        .createdAt(OffsetDateTime.now())
        .id(OidUtils.gen())
        .build());
    return this;
  }
  public ImmutableGrimBatchForOne close() {
    RepoAssert.isTrue(built, () -> "you must call MissionChanges.build() to finalize mission CREATE or UPDATE!");

    final var data = this.missionMeta.build();
    final var mission = this.mission.build();
    
    logger.add(mission);
    logger.add(data);
    
    next.addMissions(mission);
    next.addData(data);
    return next.build();
  }
}
