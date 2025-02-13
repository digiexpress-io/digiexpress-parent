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
import java.util.List;
import java.util.function.Function;

import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.Doc.DocFilter;
import io.resys.thena.api.entities.doc.DocLock.DocBranchLock;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.structures.doc.DocQueries.DocBranchLockCriteria;
import io.resys.thena.structures.doc.DocQueries.DocLockCriteria;


public interface DocBranchRegistry extends ThenaRegistryService<DocBranch, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.SqlTuple getById(String branchId);

  ThenaSqlClient.SqlTupleList insertAll(Collection<DocBranch> docs);
  ThenaSqlClient.SqlTupleList updateAll(List<DocBranch> doc);
  ThenaSqlClient.SqlTuple getBranchLock(DocBranchLockCriteria crit);
  ThenaSqlClient.SqlTuple getBranchLocks(List<DocBranchLockCriteria> crit);
  ThenaSqlClient.SqlTuple getDocLock(DocLockCriteria crit);
  ThenaSqlClient.SqlTuple getDocLocks(List<DocLockCriteria> crit);
  ThenaSqlClient.Sql findAll();
  
  ThenaSqlClient.SqlTuple findAll(DocFilter filter);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, DocBranch> defaultMapper();
  Function<io.vertx.mutiny.sqlclient.Row, DocBranchLock> docBranchLockMapper();
  
}
