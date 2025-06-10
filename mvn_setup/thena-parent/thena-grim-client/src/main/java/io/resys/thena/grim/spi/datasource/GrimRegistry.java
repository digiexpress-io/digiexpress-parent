package io.resys.thena.grim.spi.datasource;

public interface GrimRegistry {
  GrimAssignmentRegistry assignments();
  GrimCommitRegistry commits();
  GrimCommitTreeRegistry commitTrees();
  GrimCommitViewerRegistry commitViewers();
  GrimMissionDataRegistry missionData();
  GrimMissionLabelRegistry missionLabels();
  GrimMissionLinkRegistry missionLinks();
  GrimMissionRegistry missions();
  GrimObjectiveGoalRegistry goals();
  GrimObjectiveRegistry objectives();
  GrimRemarkRegistry remarks();
  GrimCommandsRegistry commands();
}
