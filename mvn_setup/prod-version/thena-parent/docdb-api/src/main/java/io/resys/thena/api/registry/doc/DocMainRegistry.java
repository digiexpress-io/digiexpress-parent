package io.resys.thena.api.registry.doc;

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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.immutables.value.Value;

import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.Doc.DocFilter;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;
import io.vertx.core.json.JsonObject;


public interface DocMainRegistry extends ThenaRegistryService<Doc, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.SqlTuple findAll(DocFilter filter);
  
  ThenaSqlClient.SqlTuple getById(String id);  // matches by external_id or id
  ThenaSqlClient.SqlTuple deleteById(String id);
  ThenaSqlClient.Sql findAll();
  
  ThenaSqlClient.SqlTupleList insertMany(List<Doc> docs);
  ThenaSqlClient.SqlTupleList updateMany(List<Doc> docs);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, Doc> defaultMapper();
  

  @Value.Immutable
  interface DocFlatted {
    String getExternalId();
    
    String getDocId();
    String getDocType();
    String getDocCreatedWithCommitId();
    Optional<String> getDocParentId();
    Doc.DocStatus getDocStatus();
    Optional<JsonObject> getDocMeta();


    String getBranchId();
    String getBranchName();
    String getBranchCreatedWithCommitId();
    
    Doc.DocStatus getBranchStatus();
    JsonObject getBranchValue();
    
    String getCommitAuthor();
    LocalDateTime getCommitDateTime();
    String getCommitMessage();
    Optional<String> getCommitParent();
    String getCommitId();
    
    Optional<String> getDocLogId();
    Optional<JsonObject> getDocLogValue();
  }
  
}
