package io.resys.thena.api.actions;

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

import java.time.LocalDate;
import java.util.List;

import io.resys.thena.api.entities.grim.GrimCommitViewer;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.smallrye.mutiny.Uni;



public interface GrimQueryActions {
  MissionQuery missionQuery();
  CommitViewersQuery commitViewersQuery();
  
  interface CommitViewersQuery {
    Uni<QueryEnvelopeList<GrimCommitViewer>> findAll();
  }
  
  interface MissionQuery {
    MissionQuery addAssignment(String assignementType, String assignmentId);
    MissionQuery addLink(String linkType, String extId);
    MissionQuery viewer(String userBy, String usedFor);
    MissionQuery addMissionId(List<String> ids);
    MissionQuery archived(GrimArchiveQueryType includeArchived);
    
    MissionQuery reporterId(String reporterId);
    MissionQuery likeTitle(String likeTitle);
    MissionQuery likeDescription(String likeDescription);
    MissionQuery fromCreatedOrUpdated(LocalDate fromCreatedOrUpdated);
    
    Uni<QueryEnvelope<GrimMissionContainer>> get(String missionIdOrExtId);
    Uni<QueryEnvelopeList<GrimMissionContainer>> findAll();
  }
  
  enum GrimArchiveQueryType {
    ALL, ONLY_ARCHIVED, ONLY_IN_FORCE
  }
}
