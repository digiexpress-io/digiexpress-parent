package io.resys.thena.registry.grim;

import io.resys.thena.api.entities.grim.ImmutableGrimOneOfRelations;
import io.resys.thena.api.entities.grim.ThenaGrimObject;
import io.resys.thena.api.registry.GrimRegistry;
import io.resys.thena.api.registry.grim.GrimAssignmentRegistry;
import io.resys.thena.api.registry.grim.GrimCommitRegistry;
import io.resys.thena.api.registry.grim.GrimCommitTreeRegistry;
import io.resys.thena.api.registry.grim.GrimCommitViewerRegistry;
import io.resys.thena.api.registry.grim.GrimLabelRegistry;
import io.resys.thena.api.registry.grim.GrimMissionDataRegistry;
import io.resys.thena.api.registry.grim.GrimMissionLabelRegistry;
import io.resys.thena.api.registry.grim.GrimMissionLinkRegistry;
import io.resys.thena.api.registry.grim.GrimMissionRegistry;
import io.resys.thena.api.registry.grim.GrimObjectiveGoalRegistry;
import io.resys.thena.api.registry.grim.GrimObjectiveRegistry;
import io.resys.thena.api.registry.grim.GrimRemarkRegistry;
import io.resys.thena.datasource.TenantTableNames;

public class GrimRegistrySqlImpl implements GrimRegistry {
  @SuppressWarnings("unused")
  private final TenantTableNames options;
  private final GrimAssignmentRegistry assignments;
  private final GrimCommitRegistry commits;
  private final GrimCommitTreeRegistry commitTrees;
  private final GrimCommitViewerRegistry commitViewers;
  private final GrimLabelRegistry labels;
  private final GrimMissionDataRegistry missionData;
  private final GrimMissionLabelRegistry missionLabels;
  private final GrimMissionLinkRegistry missionsLinks;
  private final GrimMissionRegistry mission;
  private final GrimObjectiveGoalRegistry goals;
  private final GrimObjectiveRegistry objectives;
  private final GrimRemarkRegistry remarks;
  
  public GrimRegistrySqlImpl(TenantTableNames options) {
    this.options = options;
    assignments = new GrimAssignmentRegistrySqlImpl(options);
    commits = new GrimCommitRegistrySqlImpl(options);
    commitTrees = new GrimCommitTreeRegistrySqlImpl(options);
    commitViewers = new GrimCommitViewerRegistrySqlImpl(options);
    labels = new GrimLabelRegistrySqlImpl(options);
    missionData = new GrimMissionDataRegistrySqlImpl(options);
    missionLabels = new GrimMissionLabelRegistrySqlImpl(options);
    missionsLinks = new GrimMissionLinkRegistrySqlImpl(options);
    mission = new GrimMissionRegistrySqlImpl(options);
    goals = new GrimObjectiveGoalRegistrySqlImpl(options);
    objectives = new GrimObjectiveRegistrySqlImpl(options);
    remarks = new GrimRemarkRegistrySqlImpl(options);
  }

  @Override
  public GrimAssignmentRegistry assignments() {
    return assignments;
  }
  @Override
  public GrimCommitRegistry commits() {
    return commits;
  }
  @Override
  public GrimCommitTreeRegistry commitTrees() {
    return commitTrees;
  }
  @Override
  public GrimCommitViewerRegistry commitViewers() {
    return commitViewers;
  }
  @Override
  public GrimLabelRegistry labels() {
    return labels;
  }
  @Override
  public GrimMissionDataRegistry missionData() {
    return missionData;
  }
  @Override
  public GrimMissionLabelRegistry missionLabels() {
    return missionLabels;
  }
  @Override
  public GrimMissionLinkRegistry missionsLinks() {
    return missionsLinks;
  }
  @Override
  public GrimMissionRegistry mission() {
    return mission;
  }
  @Override
  public GrimObjectiveGoalRegistry goals() {
    return goals;
  }
  @Override
  public GrimObjectiveRegistry objectives() {
    return objectives;
  }
  @Override
  public GrimRemarkRegistry remarks() {
    return remarks;
  }
  
  public static ImmutableGrimOneOfRelations toRelations(String objectiveId, String goalId, String remarkId) {
    ThenaGrimObject.GrimRelationType relationType = null;
    if(objectiveId != null) {
      relationType = ThenaGrimObject.GrimRelationType.OBJECTIVE; 
    } else if(goalId != null) {
      relationType = ThenaGrimObject.GrimRelationType.GOAL;      
    } else if(remarkId != null) {
      relationType = ThenaGrimObject.GrimRelationType.REMARK;
    }
    if(relationType == null) {
      return null;
    }
    return ImmutableGrimOneOfRelations.builder()
        .objectiveGoalId(goalId)
        .objectiveId(objectiveId)
        .remarkId(remarkId)
        .relationType(relationType)
        .build();
  }
}
