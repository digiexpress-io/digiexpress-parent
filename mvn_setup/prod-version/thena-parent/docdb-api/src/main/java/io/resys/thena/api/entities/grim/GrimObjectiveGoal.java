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


/*
 *  miniature task in the in the task hierarchy that can be assigned
 *  mission/level-1 (main task)
 *        |
 *        * objective/level-2 (sub task)
 *                  |
 *                  * n - goals/level-3 (sub sub task)
 */
@Value.Immutable
public interface GrimObjectiveGoal extends IsGrimObject, ThenaTable {
  String getId();
  String getCommitId();
  String getCreatedWithCommitId();  
  String getObjectiveId();

  @Transient @JsonIgnore
  @Nullable GrimObjectiveGoalTransitives getTransitives();
  
  @Nullable String getGoalStatus();
  @Nullable LocalDate getStartDate();
  @Nullable LocalDate getDueDate();
  @Nullable String getDescription(); // free form description
  String getTitle(); // task title
  
  
  @JsonIgnore @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_OBJECTIVE_GOAL; };
  
  @Value.Immutable
  interface GrimObjectiveGoalTransitives {
    @Nullable String getMissionId(); // transitive resolved using objective
    @Nullable OffsetDateTime getCreatedAt(); // Transitive from commit table
    @Nullable OffsetDateTime getUpdatedAt(); // Transitive from commit table
    @Nullable JsonObject getDataExtension(); // Transitive from data table
  }
}