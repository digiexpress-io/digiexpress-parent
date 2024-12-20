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
import io.resys.thena.api.entities.grim.GrimUniqueMissionLabel;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.smallrye.mutiny.Uni;



public interface GrimQueryActions {

  MissionQuery missionQuery();
  MissionLabelQuery missionLabelQuery();
  MissionRemarkQuery missionRemarkQuery();
  CommitViewersQuery commitViewersQuery();
  
  interface CommitViewersQuery {
    Uni<QueryEnvelopeList<GrimCommitViewer>> findAll();
  }
  
  interface MissionLabelQuery {
    Uni<List<GrimUniqueMissionLabel>> findAllUnique();
  }
  
  // find all remark related data
  interface MissionRemarkQuery {
    Uni<QueryEnvelope<GrimMissionContainer>> getOneByRemarkId(String remarkId);    
    Uni<QueryEnvelope<GrimMissionContainer>> findAllByMissionId(String missionId);
  }
  
  interface MissionQuery {
    // filter missions based on to what is the assignment, 
    // assignmentId parameter is used with OR filter
    // multiple calls addAssignment are treated as AND filter
    MissionQuery addAssignment(String assignementType, boolean exact, List<String> assignmentId);
    MissionQuery addAssignment(String assignementType, boolean exact, String... assignmentId);
    
    // optimization, exclude explicitly doc-s that we don't need 
    MissionQuery excludeDocs(GrimDocType... docs);
    
    MissionQuery addLink(String linkType, String extId);
    MissionQuery archived(GrimArchiveQueryType includeArchived);
    
    MissionQuery addMissionId(List<String> ids); // include only data for given missions
    MissionQuery status(List<String> status); // any of the following statuses, empty list = filter not used
    MissionQuery priority(List<String> priority); // any of the following priority, empty list = filter not used
    MissionQuery overdue(Boolean overdue); // include the tasks where dueDate > current_date
    
    MissionQuery likeReporterId(String reporterId);
    MissionQuery likeTitle(String likeTitle);
    MissionQuery likeDescription(String likeDescription);
    
    
    MissionQuery atLeastOneRemarkWithType(String remarkType); // mission must contain at least 1 remark with type
    MissionQuery atLeastOneRemarkWithAnyType(); // mission must contain at least 1 remark with any type
    
    MissionQuery notViewed(String userBy, String usedFor); // include only missions that have not been viewed
    MissionQuery notViewed(String usedFor); // include only missions that have no views for given purpose
    
    MissionQuery includeViewer(String userBy, String usedFor); //include viewer information
    
    
    MissionQuery fromCreatedOrUpdated(LocalDate fromCreatedOrUpdated);
    
    Uni<QueryEnvelope<GrimMissionContainer>> get(String missionIdOrExtId);
    Uni<QueryEnvelopeList<GrimMissionContainer>> findAll();

  }
  
  enum GrimArchiveQueryType {
    ALL, ONLY_ARCHIVED, ONLY_IN_FORCE
  }
}
