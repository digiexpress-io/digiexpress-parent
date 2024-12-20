package io.resys.thena.api.entities.grim;

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

import java.beans.Transient;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;



@Value.Immutable
public interface GrimMission extends IsGrimObject, ThenaTable {
  String getId();
  String getCommitId();
  String getCreatedWithCommitId();
  @Nullable String getUpdatedTreeWithCommitId();
  
  @Transient @JsonIgnore
  @Nullable GrimMissionTransitives getTransitives();
  
  @Nullable String getParentMissionId(); // parent-child connection
  @Nullable String getExternalId(); // optional id if linked with external system
  @Nullable String getQuestionnaireId(); // dialob form
  String getRefId(); // user friendly id
  
  @Nullable String getMissionStatus(); // smth like completed, open, new rejected etc...
  @Nullable String getMissionPriority(); // super important, low etc...
  
  @Nullable LocalDate getStartDate(); // when the task is supposed to start
  @Nullable LocalDate getDueDate(); // when the task is supposed to be completed
  
  @Nullable String getReporterId(); // who reported id, probably should be SSN(not the greatest) in our case
  @Nullable String getDescription(); // free form description
  String getTitle(); // task title
  
  @Nullable OffsetDateTime getCompletedAt(); // when we completed it
  @Nullable OffsetDateTime getArchivedAt(); // when we archived it
  @Nullable String getArchivedStatus(); // on the way to be archived or is already archived
  
  @JsonIgnore @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_MISSION; };
  
  @Value.Immutable
  interface GrimMissionTransitives {
    OffsetDateTime getCreatedAt(); // Transitive from commit table
    OffsetDateTime getTreeUpdatedAt(); // Transitive from commit table
    String getTreeUpdatedBy(); // Transitive from commit table
    @Nullable OffsetDateTime getUpdatedAt(); // Transitive from commit table
    @Nullable JsonObject getDataExtension();
  }
}
