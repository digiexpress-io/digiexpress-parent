package io.resys.thena.datasource;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import org.immutables.value.Value;

import io.resys.thena.api.entities.Tenant;

@Value.Immutable
public abstract class GrimTableNames {
  private static final GrimTableNames DEFAULTS = defaults();
  
  public abstract String getPrefix();
  public abstract String getGrimAssignment();
  public abstract String getGrimCommit();
  public abstract String getGrimCommands();
  public abstract String getGrimCommitTree();
  public abstract String getGrimCommitViewer();
  public abstract String getGrimMission();
  public abstract String getGrimMissionRef();
  public abstract String getGrimMissionData();
  public abstract String getGrimMissionLabel();
  public abstract String getGrimMissionLink();
  public abstract String getGrimObjective();
  public abstract String getGrimObjectiveGoal();
  public abstract String getGrimRemark();
  
  public GrimTableNames toRepo(Tenant repo) {
    final String prefix = repo.getPrefix();
    return toRepo(prefix);
  }
  
  public GrimTableNames toRepo(String prefix) {
    return ImmutableGrimTableNames.builder()
        .prefix(prefix)
        
        
        .grimAssignment(    prefix + DEFAULTS.getGrimAssignment())
        .grimCommit(        prefix + DEFAULTS.getGrimCommit())
        .grimCommands(      prefix + DEFAULTS.getGrimCommands())
        .grimCommitTree(    prefix + DEFAULTS.getGrimCommitTree())
        .grimCommitViewer(  prefix + DEFAULTS.getGrimCommitViewer())
        .grimMission(       prefix + DEFAULTS.getGrimMission())
        .grimMissionRef(    prefix + DEFAULTS.getGrimMissionRef())
        .grimMissionData(   prefix + DEFAULTS.getGrimMissionData())
        .grimMissionLabel(  prefix + DEFAULTS.getGrimMissionLabel())
        .grimMissionLink(   prefix + DEFAULTS.getGrimMissionLink())
        .grimObjective(     prefix + DEFAULTS.getGrimObjective())
        .grimObjectiveGoal( prefix + DEFAULTS.getGrimObjectiveGoal())
        .grimRemark(        prefix + DEFAULTS.getGrimRemark())
        

        
        .build();
  }
  
  public static GrimTableNames defaults() {
    return ImmutableGrimTableNames.builder()
        .prefix("")
        .grimAssignment("grim_assignment")
        .grimCommit("grim_commit")
        .grimCommitTree("grim_commit_tree")
        .grimCommitViewer("grim_commit_viewer")
        .grimMission("grim_mission")
        .grimCommands("grim_commands")
        .grimMissionData("grim_mission_data")
        .grimMissionLabel("grim_mission_label")
        .grimMissionLink("grim_mission_link")
        .grimObjective("grim_objective")
        .grimObjectiveGoal("grim_objective_goal")
        .grimRemark("grim_remark")
        .grimMissionRef("grim_mission_ref")
        
        .build();
  }
}
