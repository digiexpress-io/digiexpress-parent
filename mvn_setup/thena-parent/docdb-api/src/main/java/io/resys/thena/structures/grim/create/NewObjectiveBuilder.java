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
import io.resys.thena.api.entities.grim.ImmutableGrimObjective;
import io.resys.thena.api.entities.grim.ImmutableGrimOneOfRelations;
import io.resys.thena.api.entities.grim.ThenaGrimChanges;
import io.resys.thena.api.entities.grim.ThenaGrimChanges.AssignmentChanges;
import io.resys.thena.api.entities.grim.ThenaGrimChanges.GoalChanges;
import io.resys.thena.api.entities.grim.ThenaGrimChanges.ObjectiveChanges;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimOneOfRelations;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimRelationType;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.ImmutableGrimBatchForOne;
import io.resys.thena.structures.grim.commitlog.GrimCommitLogger;
import io.resys.thena.structures.grim.commitlog.GrimCommitLogger.GrimOpType;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;

public class NewObjectiveBuilder implements ThenaGrimChanges.ObjectiveChanges {
  private final GrimCommitLogger logger;
  private final String missionId;
  private final Map<String, GrimLabel> all_labels;
  
  private final ImmutableGrimBatchForOne.Builder batch;
  private final GrimOneOfRelations childRel;
  private final String objectiveId;
  private final ImmutableGrimObjective.Builder objective;
  private final ImmutableGrimMissionData.Builder objectiveMeta;
  private boolean built;
  
  public NewObjectiveBuilder(GrimCommitLogger logger, String missionId,  Map<String, GrimLabel> all_labels) {
    super();
    this.logger = logger;
    this.missionId = missionId;
    this.all_labels = all_labels;
    this.objectiveId = OidUtils.gen();
    this.batch = ImmutableGrimBatchForOne.builder()
        .tenantId(logger.getTenantId())
        .status(BatchStatus.OK)
        .log("");
    this.objective = ImmutableGrimObjective.builder()
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
  public ObjectiveChanges title(String title) {
    this.objectiveMeta.title(title);
    return this;
  }
  @Override
  public ObjectiveChanges description(String description) {
    this.objectiveMeta.description(description);
    return this;
  }
  @Override
  public ObjectiveChanges status(String status) {
    this.objective.objectiveStatus(status);
    return this;
  }
  @Override
  public ObjectiveChanges startDate(LocalDate startDate) {
    this.objective.startDate(startDate);
    return this;
  }
  @Override
  public ObjectiveChanges dueDate(LocalDate dueDate) {
    this.objective.dueDate(dueDate);
    return this;
  }
  @Override
  public ObjectiveChanges addGoal(Consumer<GoalChanges> newGoal) {
    final var builder = new NewGoalBuilder(logger, missionId, objectiveId, all_labels);
    newGoal.accept(builder);
    this.batch.from(builder.close());
    return this;
  }
  @Override
  public ObjectiveChanges addAssignees(Consumer<AssignmentChanges> assignment) {
    final var all_assignments = this.batch.build().getAssignments().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewAssignmentBuilder(logger, missionId, childRel, all_assignments);
    assignment.accept(builder);
    final var built = builder.close();
    this.batch.addAssignments(built);
    return this;
  }
  @Override
  public <T> ObjectiveChanges setAllAssignees(List<T> replacments, Function<T, Consumer<AssignmentChanges>> callbacks) {
    // clear old
    this.batch.assignments(this.batch.build().getAssignments().stream()
        .filter(a -> a.getRelation().getObjectiveId().equals(objectiveId))
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
    RepoAssert.isTrue(built, () -> "you must call ObjectiveChanges.build() to finalize mission CREATE or UPDATE!");
    
    final var data = this.objectiveMeta.build();
    final var objective = this.objective.build();
    
    logger.visit(GrimOpType.ADD, objective);
    logger.visit(GrimOpType.ADD, data);
    
    this.batch.addObjectives(objective);
    this.batch.addData(data);
    
    return this.batch.build();
  }
}
