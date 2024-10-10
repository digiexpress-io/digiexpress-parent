package io.resys.thena.api.registry;

import io.resys.thena.api.registry.grim.GrimAssignmentRegistry;
import io.resys.thena.api.registry.grim.GrimCommandsRegistry;
import io.resys.thena.api.registry.grim.GrimCommitRegistry;
import io.resys.thena.api.registry.grim.GrimCommitTreeRegistry;
import io.resys.thena.api.registry.grim.GrimCommitViewerRegistry;
import io.resys.thena.api.registry.grim.GrimMissionDataRegistry;
import io.resys.thena.api.registry.grim.GrimMissionLabelRegistry;
import io.resys.thena.api.registry.grim.GrimMissionLinkRegistry;
import io.resys.thena.api.registry.grim.GrimMissionRegistry;
import io.resys.thena.api.registry.grim.GrimObjectiveGoalRegistry;
import io.resys.thena.api.registry.grim.GrimObjectiveRegistry;
import io.resys.thena.api.registry.grim.GrimRemarkRegistry;

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
