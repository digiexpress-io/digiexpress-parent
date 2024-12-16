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

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.annotation.Nullable;

public interface ThenaGrimObject {
  interface IsGrimObject extends ThenaGrimObject { String getId(); GrimDocType getDocType(); }

  
  //transient object to resolve one of the connections to objective/goal/remark
  @Value.Immutable
  interface GrimOneOfRelations {
  
    @Nullable String getObjectiveId();
    @Nullable String getRemarkId();
    @Nullable String getObjectiveGoalId();
    GrimRelationType getRelationType();
    
    @JsonIgnore 
    default public String getTargetId() {
      switch (getRelationType()) {
      case GOAL: return getObjectiveGoalId();
      case OBJECTIVE: return getObjectiveId();
      case REMARK: return getRemarkId();
      default: throw new IllegalArgumentException("Unexpected value: " + getRelationType());
      }
    }
  }

  enum GrimRelationType {
    GOAL, OBJECTIVE, REMARK
  }
  
  enum GrimDocType {
    GRIM_MISSION,
    GRIM_MISSION_LINKS,
    GRIM_MISSION_LABEL,
    GRIM_OBJECTIVE,
    GRIM_OBJECTIVE_GOAL,
    GRIM_REMARK,
    GRIM_COMMANDS,

    GRIM_ASSIGNMENT,
    GRIM_MISSION_DATA,
    
    // infra object, tx log
    GRIM_COMMIT_VIEWER,
    GRIM_COMMIT,
  }
}
