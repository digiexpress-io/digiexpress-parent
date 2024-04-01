package io.resys.thena.structures.grim.create;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.GrimLabel;
import io.resys.thena.api.entities.grim.ImmutableGrimMission;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionData;
import io.resys.thena.api.entities.grim.ThenaGrimChanges;
import io.resys.thena.api.entities.grim.ThenaGrimChanges.AssignmentChanges;
import io.resys.thena.api.entities.grim.ThenaGrimChanges.LabelChanges;
import io.resys.thena.api.entities.grim.ThenaGrimChanges.LinkChanges;
import io.resys.thena.api.entities.grim.ThenaGrimChanges.MissionChanges;
import io.resys.thena.api.entities.grim.ThenaGrimChanges.ObjectiveChanges;
import io.resys.thena.api.entities.grim.ThenaGrimChanges.RemarkChanges;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.ImmutableGrimBatchForOne;
import io.resys.thena.structures.grim.commitlog.GrimCommitLogger;
import io.resys.thena.structures.grim.commitlog.GrimCommitLogger.GrimOpType;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;



public class NewMissionBuilder implements ThenaGrimChanges.MissionChanges {
  private final GrimCommitLogger logger;
  private final ImmutableGrimMission.Builder mission;
  private final String missionId;
  private final String commitId;
  private final ImmutableGrimMissionData.Builder missionMeta;
  private final Map<String, GrimLabel> all_labels;

  
  private ImmutableGrimBatchForOne.Builder next;
  private boolean built;
  
  public NewMissionBuilder(Map<String, GrimLabel> all_labels, GrimCommitLogger logger) {
    super();
    this.logger = logger;
    this.next = ImmutableGrimBatchForOne.builder()
        .tenantId(logger.getTenantId())
        .status(BatchStatus.OK)
        .log("");
    this.commitId = logger.getCommitId();
    this.missionId = OidUtils.gen();
    this.mission = ImmutableGrimMission.builder()
        .id(missionId)
        .commitId(commitId);
    this.missionMeta = ImmutableGrimMissionData.builder()
      .id(OidUtils.gen())
      .commitId(commitId)
      .missionId(missionId)
      .title("")
      .description("");
    this.all_labels = new HashMap<>(all_labels);
  }

  @Override
  public MissionChanges title(String title) {
    this.missionMeta.title(title);
    return this;
  }
  @Override
  public MissionChanges description(String description) {
    this.missionMeta.title(description);
    return this;
  }
  @Override
  public MissionChanges parentId(String parentId) {
    this.mission.parentMissionId(parentId);
    return this;
  }
  @Override
  public MissionChanges reporterId(String reporterId) {
    this.mission.reporterId(reporterId);
    return this;
  }
  @Override
  public MissionChanges status(String status) {
    this.mission.missionStatus(status);
    return this;
  }
  @Override
  public MissionChanges startDate(LocalDate startDate) {
    this.mission.startDate(startDate);
    return this;
  }
  @Override
  public MissionChanges dueDate(LocalDate dueDate) {
    this.mission.dueDate(dueDate);
    return this;
  }
  @Override
  public MissionChanges priority(String priority) {
    this.mission.missionPriority(priority);
    return this;
  }
  @Override
  public MissionChanges addAssignees(Consumer<AssignmentChanges> assignment) {
    final var all_assignments = this.next.build().getAssignments().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewAssignmentBuilder(logger, missionId, null, all_assignments);
    assignment.accept(builder);
    final var built = builder.close();
    this.next.addAssignments(built);
    return this;
  }
  @Override
  public MissionChanges addLabels(Consumer<LabelChanges> label) {
    final var all_mission_label = this.next.build().getMissionLabels().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewMissionLabelBuilder(
        logger, missionId, null, 
        all_mission_label,
        Collections.unmodifiableMap(all_labels)
    );
    
    label.accept(builder);
    final var built = builder.close();
    this.next.addMissionLabels(built.getItem1());
    
    if(built.getItem2().isPresent()) {
      this.next.addLabels(built.getItem2().get());
      this.all_labels.put(built.getItem2().get().getId(), built.getItem2().get());
    }
    return this;
  }
  @Override
  public MissionChanges addLink(Consumer<LinkChanges> link) {
    final var all_links = this.next.build().getLinks().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewMissionLinkBuilder(logger, missionId, null, all_links);
    link.accept(builder);
    final var built = builder.close();
    this.next.addLinks(built);
    return this;
  }
  @Override
  public MissionChanges addRemark(Consumer<RemarkChanges> remark) {
    final var all_remarks = this.next.build().getRemarks().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewRemarkBuilder(logger, missionId, null, Collections.unmodifiableMap(all_remarks));
    remark.accept(builder);
    final var built = builder.close();
    this.next.addRemarks(built);
    return this;
  }
  @Override
  public <T> MissionChanges setAllAssignees(List<T> replacments, Function<T, Consumer<AssignmentChanges>> callbacks) {
    // clear old
    this.next.assignments(this.next.build().getAssignments().stream().filter(a -> a.getRelation() == null).toList());
    final var all_assignments = this.next.build().getAssignments().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    
    // add new
    for(final var replacement : replacments) {
      final var assignment = callbacks.apply(replacement);
      
      final var builder = new NewAssignmentBuilder(logger, missionId, null, all_assignments);
      assignment.accept(builder);

      final var built = builder.close();
      this.next.addAssignments(built);

    }
    
    return this;
  }
  @Override
  public <T> MissionChanges setAllLabels(List<T> replacments, Function<T, Consumer<LabelChanges>> callbacks) {
    // clear old
    this.next.missionLabels(this.next.build().getMissionLabels().stream().filter(a -> a.getRelation() == null).toList());
    final var all_missionLabels = this.next.build().getMissionLabels().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    
    // add new
    for(final var replacement : replacments) {
      final var builder = new NewMissionLabelBuilder(
          logger, missionId, null, 
          Collections.unmodifiableMap(all_missionLabels),
          Collections.unmodifiableMap(all_labels)
      );
      final var label = callbacks.apply(replacement);
      label.accept(builder);
      final var built = builder.close();
      this.next.addMissionLabels(built.getItem1());
      
      if(built.getItem2().isPresent()) {
        this.next.addLabels(built.getItem2().get());
      }
    }
    return this;
  }
  @Override
  public <T> MissionChanges setAllLinks(List<T> replacments, Function<T, Consumer<LinkChanges>> callback) {
    // clear old
    this.next.links(this.next.build().getLinks().stream().filter(a -> a.getRelation() == null).toList());
    final var all_links = this.next.build().getLinks().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    
    // add new
    for(final var replacement : replacments) {
      final var builder = new NewMissionLinkBuilder(logger, missionId, null, Collections.unmodifiableMap(all_links));
      final var link = callback.apply(replacement);
      link.accept(builder);
      
      final var built = builder.close();
      this.next.addLinks(built);
    }
    return this;
  }
  @Override
  public MissionChanges addObjective(Consumer<ObjectiveChanges> objective) {
    final var builder = new NewObjectiveBuilder(logger, missionId, Collections.unmodifiableMap(all_labels));
    
    objective.accept(builder);
    final var built = builder.close();
    this.next.from(built);
    return this;
  }
  @Override
  public void build() {
    this.built = true;
  }

  public ImmutableGrimBatchForOne close() {
    RepoAssert.isTrue(built, () -> "you must call MissionChanges.build() to finalize mission CREATE or UPDATE!");

    final var data = this.missionMeta.build();
    final var mission = this.mission.build();
    
    logger.visit(GrimOpType.ADD, mission);
    logger.visit(GrimOpType.ADD, data);
    
    next.addMissions(mission);
    next.addData(data);
    return next.build();
  }
}
