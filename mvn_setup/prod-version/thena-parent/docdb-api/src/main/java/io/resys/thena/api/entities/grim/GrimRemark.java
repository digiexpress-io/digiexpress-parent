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

import java.time.OffsetDateTime;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import jakarta.annotation.Nullable;


// user inputed "comment" text that can be connected to the most of entities
@Value.Immutable
public interface GrimRemark extends IsGrimObject, ThenaTable {
  String getId();
  String getCommitId();
  String getCreatedWithCommitId();
  String getMissionId();
  @Nullable String getParentId();
  @Nullable GrimRemarkTransitives getTransitives();
  
  String getRemarkText(); // user inputed free text
  String getReporterId(); // user who inputed it
  @Nullable String getRemarkStatus(); // optional status ie... open/approved, not mandatory, can be empty for most implementation
  @Nullable String getRemarkType();   // optional type ie. internal / external 
  @Nullable String getRemarkSource(); // comment origin ie. frontoffice/backoffice
  
  @Nullable GrimOneOfRelations getRelation(); // one of sub entities
  
  @JsonIgnore @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_REMARK; };
  
  @Value.Immutable
  interface GrimRemarkTransitives {
    OffsetDateTime getCreatedAt(); // Transitive from commit table
    OffsetDateTime getUpdatedAt(); // Transitive from commit table
  }
}
