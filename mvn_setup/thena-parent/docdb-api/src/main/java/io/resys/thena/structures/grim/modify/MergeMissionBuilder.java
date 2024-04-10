package io.resys.thena.structures.grim.modify;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;

import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.api.entities.grim.GrimMissionLabel;
import io.resys.thena.api.entities.grim.GrimMissionLink;
import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.entities.grim.ImmutableGrimCommands;
import io.resys.thena.api.entities.grim.ImmutableGrimMission;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionData;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeGoal;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeMission;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeObjective;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeRemark;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.MergeLink;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewAssignment;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewLabel;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewLink;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewObjective;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewRemark;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.ImmutableGrimBatchMissions;
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
  private final ImmutableGrimBatchMissions.Builder batch;
  private final ImmutableGrimMission.Builder nextMission;
  private final ImmutableGrimMissionData.Builder nextMissionMeta;
  private final String missionId;
  private boolean built;
  
  public MergeMissionBuilder(GrimMissionContainer container, GrimCommitBuilder logger) {
    super();
    this.container = container;
    this.logger = logger;
    this.batch = ImmutableGrimBatchMissions.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
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
    RepoAssert.isTrue(parentId == null || !parentId.equals(missionId), () -> "parent mission id can't be itself!");
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
  public MergeMission archivedAt(OffsetDateTime archivedAt) {
    this.nextMission.archivedAt(archivedAt);
    return this;
  }
  @Override
  public <T> MergeMission setAllAssignees(String assigneeType, List<T> replacments, Function<T, Consumer<NewAssignment>> callbacks) {
    // clear old
    final var intermed = this.batch.build()
        .getAssignments().stream()
        .filter(a -> a.getRelation() != null)
        .filter(e -> !e.getAssignmentType().equals(assigneeType))
        .toList();
    this.batch.assignments(intermed);
    final var all_assignments = new HashMap<String, GrimAssignment>();
    
    // delete old
    this.batch.addAllDeleteAssignments(container.getAssignments().values().stream()
        .filter(a -> a.getRelation() == null)
        .filter(e -> e.getAssignmentType().equals(assigneeType))
        .toList());
    
    // add new
    for(final var replacement : replacments) {
      final var assignment = callbacks.apply(replacement);
      
      final var builder = new NewAssignmentBuilder(logger, missionId, null, Collections.unmodifiableMap(all_assignments));
      assignment.accept(builder);

      final var built = builder.close();
      all_assignments.put(built.getId(), built);
      this.batch.addAssignments(built);

    }
    return this;
  }

  @Override
  public <T> MergeMission setAllLabels(String labelType, List<T> replacments, Function<T, Consumer<NewLabel>> callbacks) {
    // clear old
    final var intermed = this.batch.build()
        .getMissionLabels().stream()
        .filter(a -> a.getRelation() != null)
        .filter(e -> !e.getLabelType().equals(labelType))
        .toList();
    this.batch.missionLabels(intermed);
    final var all_mission_label = new HashMap<String, GrimMissionLabel>();
    
    // delete old
    this.batch.addAllDeleteMissionLabels(container.getMissionLabels().values().stream()
        .filter(a -> a.getRelation() == null)
        .filter(e -> e.getLabelType().equals(labelType))
        .toList());
    
    // add new
    for(final var replacement : replacments) {
      final var label = callbacks.apply(replacement);
      
      final var builder = new NewMissionLabelBuilder(logger, missionId, null,  all_mission_label);
      label.accept(builder);

      final var built = builder.close();
      all_mission_label.put(built.getId(), built);
      this.batch.addMissionLabels(built);

    }
    return this;
  }

  @Override
  public <T> MergeMission setAllLinks(String linkType, List<T> replacments, Function<T, Consumer<NewLink>> callbacks) {
    // clear old
    final var intermed = this.batch.build()
        .getLinks().stream()
        .filter(a -> a.getRelation() != null)
        .filter(a -> !a.getLinkType().equals(linkType))
        .toList();
    this.batch.links(intermed);
    final var all_links = new HashMap<String, GrimMissionLink>();
    
    // delete old
    this.batch.addAllDeleteLinks(container.getLinks().values().stream()
        .filter(a -> a.getRelation() == null)
        .filter(a -> a.getLinkType().equals(linkType))
        .toList());
    
    // add new
    for(final var replacement : replacments) {
      final var link = callbacks.apply(replacement);
      
      final var builder = new NewMissionLinkBuilder(logger, missionId, null, Collections.unmodifiableMap(all_links));
      link.accept(builder);

      final var built = builder.close();
      all_links.put(built.getId(), built);
      this.batch.addLinks(built);
    }
    return this;
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
    final var current_remarks = this.batch.build().getRemarks().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var all_remarks = ImmutableMap.<String, GrimRemark>builder()
        .putAll(this.container.getRemarks().values().stream()
            .filter(e -> !current_remarks.containsKey(e.getId()))
            .collect(Collectors.toMap(e -> e.getId(), e -> e)))
        .putAll(current_remarks)
        .build();
    
    final var builder = new NewRemarkBuilder(logger, missionId, null, Collections.unmodifiableMap(all_remarks));
    remark.accept(builder);
    final var built = builder.close();
    this.batch.from(built);
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
  public MergeMission modifyGoal(String goalId, Consumer<MergeGoal> mergeGoal) {
    final var currentGoal = container.getGoals().get(goalId);
    RepoAssert.notNull(currentGoal, () -> "Can't find goal with id: '" + goalId + "' for mission: '" + missionId + "'!");
    final var builder = new MergeGoalBuilder(container, logger, currentGoal);
    mergeGoal.accept(builder);
    final var built = builder.close();
    this.batch.from(built);
    return this;
  }

  @Override
  public MergeMission modifyObjective(String objectiveId, Consumer<MergeObjective> mergeObjective) {
    final var currentObjective = container.getObjectives().get(objectiveId);
    RepoAssert.notNull(currentObjective, () -> "Can't find objective with id: '" + objectiveId + "' for mission: '" + missionId + "'!");
    final var builder = new MergeObjectiveBuilder(container, logger, currentObjective);
    mergeObjective.accept(builder);
    final var built = builder.close();
    this.batch.from(built);
    return this;
  }

  @Override
  public MergeMission modifyRemark(String remarkId, Consumer<MergeRemark> mergeRemark) {
    final var builder = new MergeRemarkBuilder(container, logger, missionId, remarkId, container.getRemarks());
    mergeRemark.accept(builder);
    final var built = builder.close();
    this.batch.from(built);
    return this;
  }
  @Override
  public MergeMission modifyLink(String linkId, Consumer<MergeLink> mergeLink) {
    final var builder = new MergeLinkBuilder(container, logger, missionId, linkId);
    mergeLink.accept(builder);
    final var built = builder.close();
    this.batch.from(built);
    return this;
  }
  @Override
  public MergeMission removeGoal(String goalId) {
    final var currentGoal = container.getGoals().get(goalId);
    RepoAssert.notNull(currentGoal, () -> "Can't find goal with id: '" + goalId + "' for mission: '" + missionId + "'!");
    this.batch.addDeleteGoals(currentGoal);
    this.logger.rm(currentGoal);
    
    this.container.getLinks().values().stream()
    .filter(link -> link.getRelation() != null && goalId.equals(link.getRelation().getObjectiveGoalId())
    ).forEach(link -> {
      this.logger.rm(link);
      this.batch.addDeleteLinks(link);
    });
    this.container.getMissionLabels().values().stream()
    .filter(link -> link.getRelation() != null && goalId.equals(link.getRelation().getObjectiveGoalId())  
    ).forEach(label -> {
      this.logger.rm(label);
      this.batch.addDeleteMissionLabels(label);
    });
    this.container.getData().values().stream()
    .filter(link -> link.getRelation() != null && goalId.equals(link.getRelation().getObjectiveGoalId()) 
    ).forEach(data -> {
      this.logger.rm(data);
      this.batch.addDeleteData(data);
    });  
    this.container.getAssignments().values().stream()
    .filter(link -> link.getRelation() != null && goalId.equals(link.getRelation().getObjectiveGoalId()) 
    ).forEach(data -> {
      this.logger.rm(data);
      this.batch.addDeleteAssignments(data);
    });      
    
    return this;
  }

  @Override
  public MergeMission removeObjective(String objectiveId) {
    final var currentObjective = container.getObjectives().get(objectiveId);
    RepoAssert.notNull(currentObjective, () -> "Can't find objective with id: '" + objectiveId + "' for mission: '" + missionId + "'!");
    this.batch.addDeleteObjectives(currentObjective);
    this.logger.rm(currentObjective);
    
    this.container.getLinks().values().stream()
    .filter(link -> link.getRelation() != null && objectiveId.equals(link.getRelation().getObjectiveId())
    ).forEach(link -> {
      this.logger.rm(link);
      this.batch.addDeleteLinks(link);
    });
    this.container.getMissionLabels().values().stream()
    .filter(link -> link.getRelation() != null && objectiveId.equals(link.getRelation().getObjectiveId())  
    ).forEach(label -> {
      this.logger.rm(label);
      this.batch.addDeleteMissionLabels(label);
    });
    this.container.getData().values().stream()
    .filter(link -> link.getRelation() != null && objectiveId.equals(link.getRelation().getObjectiveId()) 
    ).forEach(data -> {
      this.logger.rm(data);
      this.batch.addDeleteData(data);
    });  
    this.container.getAssignments().values().stream()
    .filter(link -> link.getRelation() != null && objectiveId.equals(link.getRelation().getObjectiveId()) 
    ).forEach(data -> {
      this.logger.rm(data);
      this.batch.addDeleteAssignments(data);
    });
    return this;
  }

  @Override
  public MergeMission removeRemark(String remarkId) {
    final var currentRemark = container.getRemarks().get(remarkId);
    RepoAssert.notNull(currentRemark, () -> "Can't find remark with id: '" + remarkId + "' for mission: '" + missionId + "'!");
    
    this.logger.rm(currentRemark);
    this.batch.addDeleteRemarks(currentRemark);
    this.container.getLinks().values().stream()
      .filter(link -> link.getRelation() != null && remarkId.equals(link.getRelation().getRemarkId()) 
      ).forEach(link -> {
        this.logger.rm(link);
        this.batch.addDeleteLinks(link);
      });
    this.container.getMissionLabels().values().stream()
    .filter(link -> link.getRelation() != null && remarkId.equals(link.getRelation().getRemarkId()) 
    ).forEach(label -> {
      this.logger.rm(label);
      this.batch.addDeleteMissionLabels(label);
    });
    this.container.getData().values().stream()
    .filter(link -> link.getRelation() != null && remarkId.equals(link.getRelation().getRemarkId())
    ).forEach(data -> {
      this.logger.rm(data);
      this.batch.addDeleteData(data);
    });
    this.container.getAssignments().values().stream()
    .filter(link -> link.getRelation() != null && remarkId.equals(link.getRelation().getRemarkId()) 
    ).forEach(data -> {
      this.logger.rm(data);
      this.batch.addDeleteAssignments(data);
    });
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutableGrimBatchMissions close() {
    RepoAssert.isTrue(built, () -> "you must call MergeMission.build() to finalize mission CREATE or UPDATE!");

    
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
            .updatedTreeWithCommitId(this.logger.getCommitId())
            .build();
        logger.merge(previous, mission);
        batch.addUpdateMissions(mission);
      } else if(!batch.build().isEmpty()) {
        mission = ImmutableGrimMission.builder()
            .from(mission)
            .updatedTreeWithCommitId(this.logger.getCommitId())
            .build();
        batch.addUpdateMissions(mission);
      }
    }
    

    
    
    return batch.build();
  }
}
