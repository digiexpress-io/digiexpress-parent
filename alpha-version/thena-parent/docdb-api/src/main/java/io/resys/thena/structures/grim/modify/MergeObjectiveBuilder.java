package io.resys.thena.structures.grim.modify;

import java.time.LocalDate;
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
import io.resys.thena.api.entities.grim.GrimObjective;
import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionData;
import io.resys.thena.api.entities.grim.ImmutableGrimObjective;
import io.resys.thena.api.entities.grim.ImmutableGrimOneOfRelations;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeObjective;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewAssignment;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewGoal;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewLabel;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewLink;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewRemark;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimOneOfRelations;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimRelationType;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.ImmutableGrimBatchMissions;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.structures.grim.create.NewAssignmentBuilder;
import io.resys.thena.structures.grim.create.NewGoalBuilder;
import io.resys.thena.structures.grim.create.NewMissionLabelBuilder;
import io.resys.thena.structures.grim.create.NewMissionLinkBuilder;
import io.resys.thena.structures.grim.create.NewRemarkBuilder;
import io.resys.thena.support.RepoAssert;

public class MergeObjectiveBuilder implements MergeObjective {
  
  private final GrimMissionContainer container;
  private final GrimObjective previous;
  private final GrimCommitBuilder logger;
  private final ImmutableGrimOneOfRelations childRel;
  private final ImmutableGrimBatchMissions.Builder batch;
  private final ImmutableGrimObjective.Builder nextObjective;
  private final ImmutableGrimMissionData.Builder nextObjectiveMeta;
  private final String missionId;
  private boolean built;
  
  public MergeObjectiveBuilder(GrimMissionContainer container, GrimCommitBuilder logger, GrimObjective current) {
    super();
    this.container = container;
    this.logger = logger;
    this.previous = current;
    this.childRel = ImmutableGrimOneOfRelations.builder().relationType(GrimRelationType.OBJECTIVE).objectiveGoalId(current.getId()).build();
    this.batch = ImmutableGrimBatchMissions.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.nextObjective = ImmutableGrimObjective.builder().from(current);
    this.missionId = container.getMissions().values().iterator().next().getId();
    this.nextObjectiveMeta = ImmutableGrimMissionData.builder()
        .from(container
            .getData().values().stream()
            .filter(d -> d.getRelation() != null)
            .filter(d -> d.getRelation().getRelationType() == GrimRelationType.OBJECTIVE)
            .filter(d -> d.getRelation().getObjectiveId().equals(current.getId()))
            .findFirst().get());
  }
  @Override
  public MergeObjective title(String title) {
    this.nextObjectiveMeta.title(title);
    return this;
  }
  @Override
  public MergeObjective description(String description) {
    this.nextObjectiveMeta.description(description);
    return this;
  }
  @Override
  public MergeObjective status(String status) {
    this.nextObjective.objectiveStatus(status);
    return this;
  }
  @Override
  public MergeObjective startDate(LocalDate startDate) {
    this.nextObjective.startDate(startDate);
    return this;
  }
  @Override
  public MergeObjective dueDate(LocalDate dueDate) {
    this.nextObjective.dueDate(dueDate);
    return this;
  }
  
  private boolean isGoalRelation(GrimOneOfRelations rel) {
    if(rel == null) {
      return false;
    }
    return rel.getRelationType() == GrimRelationType.OBJECTIVE && rel.getObjectiveId().equals(this.previous.getId());
  }
  
  @Override
  public <T> MergeObjective setAllAssignees(List<T> replacments, Function<T, Consumer<NewAssignment>> callbacks) {
    // clear old
    this.batch.assignments(Collections.emptyList());
    final var all_assignments = new HashMap<String, GrimAssignment>();
    
    // delete old
    this.batch.addAllDeleteAssignments(container.getAssignments().values().stream().filter(a -> isGoalRelation(a.getRelation())).toList());
    
    // add new
    for(final var replacement : replacments) {
      final var assignment = callbacks.apply(replacement);
      
      final var builder = new NewAssignmentBuilder(logger, missionId, childRel, Collections.unmodifiableMap(all_assignments));
      assignment.accept(builder);

      final var built = builder.close();
      all_assignments.put(built.getId(), built);
      this.batch.addAssignments(built);

    }
    return this;
  }

  @Override
  public <T> MergeObjective setAllLabels(List<T> replacments, Function<T, Consumer<NewLabel>> callbacks) {
    // clear old
    this.batch.missionLabels(Collections.emptyList());
    final var all_mission_label = new HashMap<String, GrimMissionLabel>();
    
    // delete old
    this.batch.addAllDeleteMissionLabels(container.getMissionLabels().values().stream().filter(a -> isGoalRelation(a.getRelation())).toList());
    
    // add new
    for(final var replacement : replacments) {
      final var label = callbacks.apply(replacement);
      
      final var builder = new NewMissionLabelBuilder(logger, missionId, childRel,  all_mission_label);
      label.accept(builder);

      final var built = builder.close();
      all_mission_label.put(built.getId(), built);
      this.batch.addMissionLabels(built);

    }
    return this;
  }

  @Override
  public <T> MergeObjective setAllLinks(List<T> replacments, Function<T, Consumer<NewLink>> callbacks) {
    // clear old
    this.batch.links(Collections.emptyList());
    final var all_links = new HashMap<String, GrimMissionLink>();
    
    // delete old
    this.batch.addAllDeleteLinks(container.getLinks().values().stream().filter(a -> isGoalRelation(a.getRelation())).toList());
    
    // add new
    for(final var replacement : replacments) {
      final var link = callbacks.apply(replacement);
      
      final var builder = new NewMissionLinkBuilder(logger, missionId, childRel, Collections.unmodifiableMap(all_links));
      link.accept(builder);

      final var built = builder.close();
      all_links.put(built.getId(), built);
      this.batch.addLinks(built);
    }
    return this;
  }

  @Override
  public MergeObjective addAssignees(Consumer<NewAssignment> assignment) {
    final var all_assignments = this.batch.build().getAssignments().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewAssignmentBuilder(logger, missionId, childRel, all_assignments);
    assignment.accept(builder);
    final var built = builder.close();
    this.batch.addAssignments(built);
    return this;
  }

  @Override
  public MergeObjective addLabels(Consumer<NewLabel> label) {
    final var all_mission_label = this.batch.build().getMissionLabels().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewMissionLabelBuilder(
        logger, missionId, childRel, 
        all_mission_label
    );
    
    label.accept(builder);
    final var built = builder.close();
    this.batch.addMissionLabels(built);
    return this;
  }

  @Override
  public MergeObjective addLink(Consumer<NewLink> link) {
    final var all_links = this.batch.build().getLinks().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewMissionLinkBuilder(logger, missionId, childRel, all_links);
    link.accept(builder);
    final var built = builder.close();
    this.batch.addLinks(built);
    return this;
  }

  @Override
  public MergeObjective addRemark(Consumer<NewRemark> remark) {
    final var current_remarks = this.batch.build().getRemarks().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var all_remarks = ImmutableMap.<String, GrimRemark>builder()
        .putAll(this.container.getRemarks().values().stream()
            .filter(e -> !current_remarks.containsKey(e.getId()))
            .collect(Collectors.toMap(e -> e.getId(), e -> e)))
        .putAll(current_remarks)
        .build();
    
    final var builder = new NewRemarkBuilder(logger, missionId, childRel, Collections.unmodifiableMap(all_remarks));
    remark.accept(builder);
    final var built = builder.close();
    this.batch.from(built);
    return this;
  }
  @Override
  public MergeObjective addGoal(Consumer<NewGoal> newGoal) {
    final var builder = new NewGoalBuilder(logger, missionId, this.previous.getId());
    newGoal.accept(builder);
    this.batch.from(builder.close());
    return this;
  }
  
  @Override
  public void build() {
    this.built = true;
  }

  public ImmutableGrimBatchMissions close() {
    RepoAssert.isTrue(built, () -> "you must call MergeObjective.build() to finalize mission CREATE or UPDATE!");

    
    // mission meta merge
    {
      var data = this.nextObjectiveMeta.build();
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
      var objective = this.nextObjective.build();
      final var isModified = !objective.equals(this.previous);
      
      if(isModified) {
        objective = ImmutableGrimObjective.builder()
            .from(objective)
            .commitId(this.logger.getCommitId())
            .build();
        logger.merge(previous, objective);
        batch.addUpdateObjectives(objective);
      }
    }

    return batch.build();
  }
}
