package io.resys.thena.api.entities.doc;

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
import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.doc.DocEntity.IsDocObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.vertx.core.json.JsonObject;

@Value.Immutable
public interface Doc extends DocEntity, IsDocObject, ThenaTable {
  enum DocStatus { IN_FORCE, ARCHIVED }
  
  String getId();
  String getType();
  DocStatus getStatus();
  String getExternalId();

  String getCommitId();
  String getCreatedWithCommitId();

  OffsetDateTime getCreatedAt(); // transitive from commit
  OffsetDateTime getUpdatedAt(); // transitive from commit
  
  @Nullable String getOwnerId();
  @Nullable String getParentId();
  @Nullable JsonObject getMeta();
  
  @JsonIgnore @Override default public DocType getDocType() { return DocType.DOC; };
 
  @Value.Immutable
  interface DocFilter {
    @Nullable List<String> getDocIds();
    @Nullable String getParentId();
    @Nullable String getOwnerId();
    @Nullable String getDocType();
    
    @Nullable String getBranch();
  }

}
