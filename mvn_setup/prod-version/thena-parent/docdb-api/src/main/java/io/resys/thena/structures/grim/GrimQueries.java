package io.resys.thena.structures.grim;

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
import java.util.Collection;

import io.resys.thena.api.actions.GrimQueryActions.GrimArchiveQueryType;
import io.resys.thena.api.entities.grim.GrimAnyObject;
import io.resys.thena.api.entities.grim.GrimCommitViewer;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.registry.grim.GrimCommitViewerRegistry.AnyObjectCriteria;
import io.resys.thena.datasource.ThenaDataSource;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface GrimQueries {
  ThenaDataSource getDataSource();
  InternalMissionQuery missions();
  CommitViewerQuery commitViewer();
  
  interface CommitViewerQuery {
    Multi<GrimAnyObject> findAnyObjects(Collection<AnyObjectCriteria> commits);
    Multi<GrimCommitViewer> findAllViewersByUsed(String userId, String usedBy, Collection<String> commits);
  }

  
  interface InternalMissionQuery {
    
    InternalMissionQuery viewer(String userId, String usedBy);
    InternalMissionQuery excludeDocs(GrimDocType ...docs); // multiple will be OR
    InternalMissionQuery archived(GrimArchiveQueryType includeArchived); // true to exclude any tasks with archiveAt date present
    InternalMissionQuery missionId(String ...missionId); // multiple will be OR
    InternalMissionQuery addAssignment(String assignmentType, String assignmentValue); // multiple will be OR
    InternalMissionQuery addLink(String linkType, String linkValue); // multiple will be OR
    
    InternalMissionQuery reporterId(String reporterId);
    InternalMissionQuery likeTitle(String likeTitle);
    InternalMissionQuery likeDescription(String likeDescription);
    InternalMissionQuery fromCreatedOrUpdated(LocalDate fromCreatedOrUpdated);

    Multi<GrimMissionContainer> findAll();
    Uni<GrimMissionContainer> getById(String missionId);
  }
}
