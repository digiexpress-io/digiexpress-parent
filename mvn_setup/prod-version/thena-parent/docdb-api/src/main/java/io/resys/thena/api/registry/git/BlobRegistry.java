package io.resys.thena.api.registry.git;

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
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.resys.thena.api.actions.GitPullActions.MatchCriteria;
import io.resys.thena.api.entities.git.Blob;
import io.resys.thena.api.entities.git.BlobHistory;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface BlobRegistry extends ThenaRegistryService<Blob, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple getById(String blobId);
  
  ThenaSqlClient.SqlTuple insertOne(Blob blob);
  ThenaSqlClient.SqlTupleList insertAll(Collection<Blob> blobs);
  
  ThenaSqlClient.SqlTuple find(@Nullable String name, boolean latestOnly, List<MatchCriteria> criteria);
  ThenaSqlClient.SqlTuple findByTree(String treeId, List<MatchCriteria> criteria);
  ThenaSqlClient.SqlTuple findByTree(String treeId, List<String> blobNames, List<MatchCriteria> criteria);
  ThenaSqlClient.SqlTuple findByIds(Collection<String> blobId);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, Blob> defaultMapper();
  
  Function<io.vertx.mutiny.sqlclient.Row, BlobHistory> historyMapper();
}
