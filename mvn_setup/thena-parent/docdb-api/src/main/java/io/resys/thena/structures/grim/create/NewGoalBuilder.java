package io.resys.thena.structures.grim.create;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;

import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.api.entities.grim.GrimLabel;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionData;
import io.resys.thena.api.entities.grim.ImmutableGrimObjectiveGoal;
import io.resys.thena.api.entities.grim.ImmutableGrimOneOfRelations;
import io.resys.thena.api.entities.grim.ThenaGrimChanges;
import io.resys.thena.api.entities.grim.ThenaGrimChanges.AssignmentChanges;
import io.resys.thena.api.entities.grim.ThenaGrimChanges.GoalChanges;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimOneOfRelations;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimRelationType;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.ImmutableGrimBatchForOne;
import io.resys.thena.structures.grim.commitlog.GrimCommitLogger;
import io.resys.thena.structures.grim.commitlog.GrimCommitLogger.GrimOpType;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;

public class NewGoalBuilder implements ThenaGrimChanges.GoalChanges {
  private final GrimCommitLogger logger;
  private final String missionId;
  private final Map<String, GrimLabel> all_labels;
  
  private final ImmutableGrimBatchForOne.Builder batch;
  private final GrimOneOfRelations childRel;
  private final String goalId;
  private final ImmutableGrimObjectiveGoal.Builder goal;
  private final ImmutableGrimMissionData.Builder objectiveMeta;
  private boolean built;
  
  public NewGoalBuilder(GrimCommitLogger logger, String missionId, String objectiveId, Map<String, GrimLabel> all_labels) {
    super();
    this.logger = logger;
    this.missionId = missionId;
    this.all_labels = all_labels;
    this.goalId = OidUtils.gen();
    this.batch = ImmutableGrimBatchForOne.builder()
        .tenantId(logger.getTenantId())
        .status(BatchStatus.OK)
        .log("");
    this.goal = ImmutableGrimObjectiveGoal.builder()
        .id(OidUtils.gen())
        .commitId(logger.getCommitId())
        .missionId(missionId)
        .objectiveId(objectiveId)
        .id(goalId)
        ;
    this.childRel = ImmutableGrimOneOfRelations.builder()
        .objectiveGoalId(goalId)
        .relationType(GrimRelationType.GOAL)
        .build();
    this.objectiveMeta = ImmutableGrimMissionData.builder()
        .id(OidUtils.gen())
        .commitId(logger.getCommitId())
        .missionId(missionId)
        .relation(childRel)
        .title("")
        .description("");
  }
  @Override
  public void build() {
    this.built = true;
  }

  @Override
  public GoalChanges title(String title) {
    this.objectiveMeta.title(title);
    return this;
  }
  @Override
  public GoalChanges description(String description) {
    this.objectiveMeta.description(description);
    return this;
  }
  @Override
  public GoalChanges status(String status) {
    this.goal.goalStatus(status);
    return this;
  }
  @Override
  public GoalChanges startDate(LocalDate startDate) {
    this.goal.startDate(startDate);
    return this;
  }
  @Override
  public GoalChanges dueDate(LocalDate dueDate) {
    this.goal.dueDate(dueDate);
    return this;
  }
  @Override
  public GoalChanges addAssignees(Consumer<AssignmentChanges> assignment) {
    final var all_assignments = this.batch.build().getAssignments().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewAssignmentBuilder(logger, missionId, childRel, all_assignments);
    assignment.accept(builder);
    final var built = builder.close();
    this.batch.addAssignments(built);
    return this;
  }
  @Override
  public <T> GoalChanges setAllAssignees(List<T> replacments, Function<T, Consumer<AssignmentChanges>> callbacks) {
    // clear old
    this.batch.assignments(this.batch.build().getAssignments().stream()
        .filter(a -> a.getRelation().getObjectiveId().equals(goalId))
        .toList());
    
    // add new
    for(final var replacement : replacments) {
      final var assignment = callbacks.apply(replacement);
      
      final var builder = new NewAssignmentBuilder(logger, missionId, childRel, ImmutableMap.<String, GrimAssignment>builder()
          .putAll(this.batch.build().getAssignments().stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build());
      assignment.accept(builder);

      final var built = builder.close();
      this.batch.addAssignments(built);
    }
    return this;
  }
  public ImmutableGrimBatchForOne close() {
    RepoAssert.isTrue(built, () -> "you must call GoalChanges.build() to finalize mission CREATE or UPDATE!");
    
    final var data = this.objectiveMeta.build();
    final var goal = this.goal.build();
    
    logger.visit(GrimOpType.ADD, goal);
    logger.visit(GrimOpType.ADD, data);
    
    this.batch.addGoals(goal);
    this.batch.addData(data);
    
    return this.batch.build();
  }
}
