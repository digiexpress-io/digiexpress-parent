package io.resys.thena.api.entities.grim;

/*-
 * #%L
 * thena-grim-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.api.entities.AnyTenantEntity;
import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;


/**
 * Audit trail for deleted mission, represents last known object state.
 * Commit-s and commit views are not backed up.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableGrimDeletedMission.class)
@JsonDeserialize(as = ImmutableGrimDeletedMission.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface GrimDeletedMission extends IsGrimObject, AnyTenantEntity {
  GrimMission getMission();    
  
  List<GrimMissionLabel> getMissionLabels();
  List<GrimMissionLink> getLinks();
  List<GrimRemark> getRemarks();
  List<GrimObjective> getObjectives();
  List<GrimObjectiveGoal> getGoals();
  List<GrimMissionData> getData();
  List<GrimAssignment> getAssignments(); 
  List<GrimCommands> getCommands();
  List<GrimCommit> getCommits();
  
  @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_MISSION; };
}
