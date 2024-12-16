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

import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface GrimAssignment extends IsGrimObject, ThenaTable {
  String getId();
  String getCommitId();
  String getMissionId();
  String getAssignee();
  String getAssignmentType();
  @Nullable GrimOneOfRelations getRelation(); // one of sub entities
  
  default boolean isMatch(String targetId) {
    if(targetId == null) {
      return false;
    }
    if(getMissionId().equals(targetId)) {
      return true;
    }
    if(getRelation() == null) {
      return false;
    }
    return getRelation().getTargetId().equals(targetId);
  }
  
  @JsonIgnore @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_ASSIGNMENT; };
}
