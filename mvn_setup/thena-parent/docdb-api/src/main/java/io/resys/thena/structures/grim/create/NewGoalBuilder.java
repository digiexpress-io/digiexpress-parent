package io.resys.thena.structures.grim.create;

import java.time.LocalDate;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.ImmutableGrimMissionData;
import io.resys.thena.api.entities.grim.ImmutableGrimObjectiveGoal;
import io.resys.thena.api.entities.grim.ImmutableGrimOneOfRelations;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewAssignment;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewGoal;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimOneOfRelations;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimRelationType;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.ImmutableGrimBatchForOne;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;

public class NewGoalBuilder implements ThenaGrimNewObject.NewGoal {
  private final GrimCommitBuilder logger;
  private final String missionId;
  
  private final ImmutableGrimBatchForOne.Builder batch;
  private final GrimOneOfRelations childRel;
  private final String goalId;
  private final ImmutableGrimObjectiveGoal.Builder goal;
  private final ImmutableGrimMissionData.Builder objectiveMeta;
  private boolean built;
  
  public NewGoalBuilder(GrimCommitBuilder logger, String missionId, String objectiveId) {
    super();
    this.logger = logger;
    this.missionId = missionId;
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
  public NewGoal title(String title) {
    this.objectiveMeta.title(title);
    return this;
  }
  @Override
  public NewGoal description(String description) {
    this.objectiveMeta.description(description);
    return this;
  }
  @Override
  public NewGoal status(String status) {
    this.goal.goalStatus(status);
    return this;
  }
  @Override
  public NewGoal startDate(LocalDate startDate) {
    this.goal.startDate(startDate);
    return this;
  }
  @Override
  public NewGoal dueDate(LocalDate dueDate) {
    this.goal.dueDate(dueDate);
    return this;
  }
  @Override
  public NewGoal addAssignees(Consumer<NewAssignment> assignment) {
    final var all_assignments = this.batch.build().getAssignments().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewAssignmentBuilder(logger, missionId, childRel, all_assignments);
    assignment.accept(builder);
    final var built = builder.close();
    this.batch.addAssignments(built);
    return this;
  }
  public ImmutableGrimBatchForOne close() {
    RepoAssert.isTrue(built, () -> "you must call GoalChanges.build() to finalize mission CREATE or UPDATE!");
    
    final var data = this.objectiveMeta.build();
    final var goal = this.goal.build();
    
    logger.add(goal);
    logger.add(data);
    
    this.batch.addGoals(goal);
    this.batch.addData(data);
    
    return this.batch.build();
  }
}
