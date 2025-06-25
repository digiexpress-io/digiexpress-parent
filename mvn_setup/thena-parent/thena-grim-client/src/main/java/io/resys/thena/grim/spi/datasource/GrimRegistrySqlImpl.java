package io.resys.thena.grim.spi.datasource;

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

import io.resys.thena.api.entities.grim.ImmutableGrimOneOfRelations;
import io.resys.thena.api.entities.grim.ThenaGrimObject;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.grim.spi.sql.GrimAssignmentRegistrySqlImpl;
import io.resys.thena.grim.spi.sql.GrimCommandsRegistrySqlImpl;
import io.resys.thena.grim.spi.sql.GrimCommitRegistrySqlImpl;
import io.resys.thena.grim.spi.sql.GrimCommitTreeRegistrySqlImpl;
import io.resys.thena.grim.spi.sql.GrimCommitViewerRegistrySqlImpl;
import io.resys.thena.grim.spi.sql.GrimMissionDataRegistrySqlImpl;
import io.resys.thena.grim.spi.sql.GrimMissionLabelRegistrySqlImpl;
import io.resys.thena.grim.spi.sql.GrimMissionLinkRegistrySqlImpl;
import io.resys.thena.grim.spi.sql.GrimMissionRegistrySqlImpl;
import io.resys.thena.grim.spi.sql.GrimObjectiveGoalRegistrySqlImpl;
import io.resys.thena.grim.spi.sql.GrimObjectiveRegistrySqlImpl;
import io.resys.thena.grim.spi.sql.GrimProcessRegistrySqlImpl;
import io.resys.thena.grim.spi.sql.GrimRemarkRegistrySqlImpl;
import io.resys.thena.grim.spi.sql.GrimTableNames;

public class GrimRegistrySqlImpl implements GrimRegistry {
  private final GrimTableNames options;
  private final GrimAssignmentRegistry assignments;
  private final GrimCommitRegistry commits;
  private final GrimCommitTreeRegistry commitTrees;
  private final GrimCommitViewerRegistry commitViewers;
  private final GrimMissionDataRegistry missionData;
  private final GrimMissionLabelRegistry missionLabels;
  private final GrimMissionLinkRegistry missionsLinks;
  private final GrimMissionRegistry mission;
  private final GrimObjectiveGoalRegistry goals;
  private final GrimObjectiveRegistry objectives;
  private final GrimRemarkRegistry remarks;
  private final GrimCommandsRegistry commands;
  private final GrimProcessRegistry processes;

  public GrimRegistrySqlImpl(TenantContext tenant) {
    this.options = GrimTableNames.defaults().toRepo(tenant.getPrefix());
    assignments = new GrimAssignmentRegistrySqlImpl(options);
    commits = new GrimCommitRegistrySqlImpl(options);
    commitTrees = new GrimCommitTreeRegistrySqlImpl(options);
    commitViewers = new GrimCommitViewerRegistrySqlImpl(options);
    missionData = new GrimMissionDataRegistrySqlImpl(options);
    missionLabels = new GrimMissionLabelRegistrySqlImpl(options);
    missionsLinks = new GrimMissionLinkRegistrySqlImpl(options);
    mission = new GrimMissionRegistrySqlImpl(options);
    goals = new GrimObjectiveGoalRegistrySqlImpl(options);
    objectives = new GrimObjectiveRegistrySqlImpl(options);
    remarks = new GrimRemarkRegistrySqlImpl(options);
    commands = new GrimCommandsRegistrySqlImpl(options);
    processes = new GrimProcessRegistrySqlImpl(options);
  }
  @Override
  public GrimProcessRegistry processes() {
    return processes;
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
  public GrimMissionDataRegistry missionData() {
    return missionData;
  }
  @Override
  public GrimMissionLabelRegistry missionLabels() {
    return missionLabels;
  }
  @Override
  public GrimMissionLinkRegistry missionLinks() {
    return missionsLinks;
  }
  @Override
  public GrimMissionRegistry missions() {
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
  @Override
  public GrimCommandsRegistry commands() {
    return commands;
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
