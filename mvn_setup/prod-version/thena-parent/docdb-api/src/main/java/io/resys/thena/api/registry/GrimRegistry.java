package io.resys.thena.api.registry;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
