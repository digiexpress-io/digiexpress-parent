package io.resys.thena.api.registry;

import io.resys.thena.api.registry.grim.GrimAssignmentRegistry;
import io.resys.thena.api.registry.grim.GrimCommitRegistry;
import io.resys.thena.api.registry.grim.GrimCommitTreeRegistry;
import io.resys.thena.api.registry.grim.GrimCommitViewerRegistry;
import io.resys.thena.api.registry.grim.GrimLabelRegistry;
import io.resys.thena.api.registry.grim.GrimMissionDataRegistry;
import io.resys.thena.api.registry.grim.GrimMissionLabelRegistry;
import io.resys.thena.api.registry.grim.GrimMissionLinksRegistry;
import io.resys.thena.api.registry.grim.GrimMissionRegistry;
import io.resys.thena.api.registry.grim.GrimObjectiveGoalRegistry;
import io.resys.thena.api.registry.grim.GrimObjectiveRegistry;
import io.resys.thena.api.registry.grim.GrimRemarkRegistry;

public interface GrimRegistry {
  GrimAssignmentRegistry assignments();
  GrimCommitRegistry commits();
  GrimCommitTreeRegistry commitTrees();
  GrimCommitViewerRegistry commitViewers();
  GrimLabelRegistry labels();
  GrimMissionDataRegistry missionData();
  GrimMissionLabelRegistry missionLabels();
  GrimMissionLinksRegistry missionsLinks();
  GrimMissionRegistry mission();
  GrimObjectiveGoalRegistry goals();
  GrimObjectiveRegistry objectives();
  GrimRemarkRegistry remarks();
}
