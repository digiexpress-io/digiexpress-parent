package io.resys.thena.structures.grim.modify;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.api.entities.grim.GrimMissionLabel;
import io.resys.thena.api.entities.grim.GrimMissionLink;
import io.resys.thena.api.entities.grim.GrimObjectiveGoal;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionData;
import io.resys.thena.api.entities.grim.ImmutableGrimObjectiveGoal;
import io.resys.thena.api.entities.grim.ImmutableGrimOneOfRelations;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeGoal;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewAssignment;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewLabel;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewLink;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewRemark;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimOneOfRelations;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimRelationType;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.ImmutableGrimBatchMissions;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.structures.grim.create.NewAssignmentBuilder;
import io.resys.thena.structures.grim.create.NewMissionLabelBuilder;
import io.resys.thena.structures.grim.create.NewMissionLinkBuilder;
import io.resys.thena.structures.grim.create.NewRemarkBuilder;
import io.resys.thena.support.RepoAssert;

public class MergeGoalBuilder implements MergeGoal {
  
  private final GrimMissionContainer container;
  private final GrimObjectiveGoal previous;
  private final GrimCommitBuilder logger;
  private final ImmutableGrimOneOfRelations childRel;
  private final ImmutableGrimBatchMissions.Builder batch;
  private final ImmutableGrimObjectiveGoal.Builder nextGoal;
  private final ImmutableGrimMissionData.Builder nextGoalMeta;
  private final String missionId;
  private boolean built;
  
  public MergeGoalBuilder(GrimMissionContainer container, GrimCommitBuilder logger, GrimObjectiveGoal current) {
    super();
    this.container = container;
    this.logger = logger;
    this.previous = current;
    this.childRel = ImmutableGrimOneOfRelations.builder().relationType(GrimRelationType.GOAL).objectiveGoalId(current.getId()).build();
    this.batch = ImmutableGrimBatchMissions.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.nextGoal = ImmutableGrimObjectiveGoal.builder().from(current);
    this.missionId = container.getMissions().values().iterator().next().getId();
    this.nextGoalMeta = ImmutableGrimMissionData.builder()
        .from(container
            .getData().values().stream()
            .filter(d -> d.getRelation() != null)
            .filter(d -> d.getRelation().getRelationType() == GrimRelationType.GOAL)
            .filter(d -> d.getRelation().getObjectiveGoalId().equals(current.getId()))
            .findFirst().get());
  }
  @Override
  public MergeGoal title(String title) {
    this.nextGoalMeta.title(title);
    return this;
  }
  @Override
  public MergeGoal description(String description) {
    this.nextGoalMeta.description(description);
    return this;
  }
  @Override
  public MergeGoal status(String status) {
    this.nextGoal.goalStatus(status);
    return this;
  }
  @Override
  public MergeGoal startDate(LocalDate startDate) {
    this.nextGoal.startDate(startDate);
    return this;
  }
  @Override
  public MergeGoal dueDate(LocalDate dueDate) {
    this.nextGoal.dueDate(dueDate);
    return this;
  }
  
  private boolean isGoalRelation(GrimOneOfRelations rel) {
    if(rel == null) {
      return false;
    }
    return rel.getRelationType() == GrimRelationType.GOAL && rel.getObjectiveGoalId().equals(this.previous.getId());
  }
  
  @Override
  public <T> MergeGoal setAllAssignees(String assigneeType, List<T> replacments, Function<T, Consumer<NewAssignment>> callbacks) {
    RepoAssert.notEmpty(assigneeType, () -> "assigneeType can't be empty!");
    RepoAssert.notNull(replacments, () -> "replacments can't be empty!");
    RepoAssert.notNull(callbacks, () -> "callbacks can't be empty!");

    // clear old
    final var saved = this.batch.build().getAssignments().stream()
      .filter(e -> !e.getAssignmentType().equals(assigneeType))
      .toList();
    this.batch.assignments(saved);
    final var all_assignments = new HashMap<String, GrimAssignment>(saved.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    
    // delete old
    this.batch.addAllDeleteAssignments(container.getAssignments().values().stream()
        .filter(a -> isGoalRelation(a.getRelation()))
        .filter(e -> e.getAssignmentType().equals(assigneeType))
        .toList());
    
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
  public <T> MergeGoal setAllLabels(String labelType, List<T> replacments, Function<T, Consumer<NewLabel>> callbacks) {
    // clear old
    final var saved = this.batch.build().getMissionLabels().stream()
        .filter(e -> !e.getLabelType().equals(labelType))
        .toList();
    this.batch.missionLabels(Collections.emptyList());
    final var all_mission_label = new HashMap<String, GrimMissionLabel>(saved.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    
    // delete old
    this.batch.addAllDeleteMissionLabels(container.getMissionLabels().values().stream()
        .filter(a -> isGoalRelation(a.getRelation()))
        .filter(e -> e.getLabelType().equals(labelType))
        .toList());
    
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
  public <T> MergeGoal setAllLinks(String linkType, List<T> replacments, Function<T, Consumer<NewLink>> callbacks) {
    // clear old
    final var saved = this.batch.build().getLinks().stream()
        .filter(e -> !e.getLinkType().equals(linkType))
        .toList();
    this.batch.links(saved);
    final var all_links = new HashMap<String, GrimMissionLink>(saved.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    
    // delete old
    this.batch.addAllDeleteLinks(container.getLinks().values().stream()
        .filter(a -> isGoalRelation(a.getRelation()))
        .filter(a -> a.getLinkType().equals(linkType))
        .toList());
    
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
  public MergeGoal addAssignees(Consumer<NewAssignment> assignment) {
    final var all_assignments = this.batch.build().getAssignments().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewAssignmentBuilder(logger, missionId, childRel, all_assignments);
    assignment.accept(builder);
    final var built = builder.close();
    this.batch.addAssignments(built);
    return this;
  }

  @Override
  public MergeGoal addLabels(Consumer<NewLabel> label) {
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
  public MergeGoal addLink(Consumer<NewLink> link) {
    final var all_links = this.batch.build().getLinks().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewMissionLinkBuilder(logger, missionId, childRel, all_links);
    link.accept(builder);
    final var built = builder.close();
    this.batch.addLinks(built);
    return this;
  }

  @Override
  public MergeGoal addRemark(Consumer<NewRemark> remark) {
    final var all_remarks = this.batch.build().getRemarks().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewRemarkBuilder(logger, missionId, childRel, Collections.unmodifiableMap(all_remarks));
    remark.accept(builder);
    final var built = builder.close();
    this.batch.from(built);
    return this;
  }


  @Override
  public void build() {
    this.built = true;
  }

  public ImmutableGrimBatchMissions close() {
    RepoAssert.isTrue(built, () -> "you must call MergeGoal.build() to finalize mission CREATE or UPDATE!");

    
    // mission meta merge
    {
      var data = this.nextGoalMeta.build();
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
      var goal = this.nextGoal.build();
      final var isModified = !goal.equals(this.previous);
      
      if(isModified) {
        goal = ImmutableGrimObjectiveGoal.builder()
            .from(goal)
            .commitId(this.logger.getCommitId())
            .build();
        logger.merge(previous, goal);
        batch.addUpdateGoals(goal);
      }
    }

    return batch.build();
  }
}
