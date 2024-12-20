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

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.Doc.DocFilter;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface DocCommitRegistry extends ThenaRegistryService<DocCommit, io.vertx.mutiny.sqlclient.Row> {
  
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTupleList insertAll(Collection<DocCommit> commits);
  ThenaSqlClient.SqlTuple findAll(DocFilter filter);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, DocCommit> defaultMapper();
  
}