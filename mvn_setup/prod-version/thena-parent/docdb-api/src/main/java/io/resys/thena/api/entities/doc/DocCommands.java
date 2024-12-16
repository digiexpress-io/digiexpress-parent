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
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.doc.DocEntity.IsDocObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.vertx.core.json.JsonObject;

@Value.Immutable
public interface DocCommands extends DocEntity, IsDocObject, ThenaTable {
  String getId();
  String getCommitId();
  
  String getDocId();
  Optional<String> getBranchId();
  OffsetDateTime getCreatedAt(); // transitive from commit
  String getCreatedBy(); // transitive from commit
  
  List<JsonObject> getCommands();
  
  @JsonIgnore @Override default public DocType getDocType() { return DocType.DOC_COMMANDS; };
}
