package io.resys.thena.structures.grim.create;

import java.time.LocalDate;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.ImmutableGrimMissionData;
import io.resys.thena.api.entities.grim.ImmutableGrimObjective;
import io.resys.thena.api.entities.grim.ImmutableGrimOneOfRelations;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewAssignment;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewGoal;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewObjective;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimOneOfRelations;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimRelationType;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.ImmutableGrimBatchForOne;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;

public class NewObjectiveBuilder implements ThenaGrimNewObject.NewObjective {
  private final GrimCommitBuilder logger;
  private final String missionId;
  
  private final ImmutableGrimBatchForOne.Builder batch;
  private final GrimOneOfRelations childRel;
  private final String objectiveId;
  private final ImmutableGrimObjective.Builder objective;
  private final ImmutableGrimMissionData.Builder objectiveMeta;
  private boolean built;
  
  public NewObjectiveBuilder(GrimCommitBuilder logger, String missionId) {
    super();
    this.logger = logger;
    this.missionId = missionId;
    this.objectiveId = OidUtils.gen();
    this.batch = ImmutableGrimBatchForOne.builder()
        .tenantId(logger.getTenantId())
        .status(BatchStatus.OK)
        .log("");
    this.objective = ImmutableGrimObjective.builder()
        .createdWithCommitId(logger.getCommitId())
        .commitId(logger.getCommitId())
        .missionId(missionId)
        .id(objectiveId)
        ;
    this.childRel = ImmutableGrimOneOfRelations.builder()
        .objectiveId(objectiveId)
        .relationType(GrimRelationType.OBJECTIVE)
        .build();
    this.objectiveMeta = ImmutableGrimMissionData.builder()
        .id(OidUtils.gen())
        .createdWithCommitId(logger.getCommitId())
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
  public NewObjective title(String title) {
    this.objectiveMeta.title(title);
    return this;
  }
  @Override
  public NewObjective description(String description) {
    this.objectiveMeta.description(description);
    return this;
  }
  @Override
  public NewObjective status(String status) {
    this.objective.objectiveStatus(status);
    return this;
  }
  @Override
  public NewObjective startDate(LocalDate startDate) {
    this.objective.startDate(startDate);
    return this;
  }
  @Override
  public NewObjective dueDate(LocalDate dueDate) {
    this.objective.dueDate(dueDate);
    return this;
  }
  @Override
  public NewObjective addGoal(Consumer<NewGoal> newGoal) {
    final var builder = new NewGoalBuilder(logger, missionId, objectiveId);
    newGoal.accept(builder);
    this.batch.from(builder.close());
    return this;
  }
  @Override
  public NewObjective addAssignees(Consumer<NewAssignment> assignment) {
    final var all_assignments = this.batch.build().getAssignments().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewAssignmentBuilder(logger, missionId, childRel, all_assignments);
    assignment.accept(builder);
    final var built = builder.close();
    this.batch.addAssignments(built);
    return this;
  }
  public ImmutableGrimBatchForOne close() {
    RepoAssert.isTrue(built, () -> "you must call ObjectiveChanges.build() to finalize mission CREATE or UPDATE!");
    
    final var data = this.objectiveMeta.build();
    final var objective = this.objective.build();
    
    logger.add(objective);
    logger.add(data);
    
    this.batch.addObjectives(objective);
    this.batch.addData(data);
    
    return this.batch.build();
  }
}
