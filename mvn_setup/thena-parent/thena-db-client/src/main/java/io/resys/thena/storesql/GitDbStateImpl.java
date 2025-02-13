package io.resys.thena.storesql;

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

import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.structures.git.GitInserts;
import io.resys.thena.structures.git.GitQueries;
import io.resys.thena.structures.git.GitState;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GitDbStateImpl implements GitState {
  private final ThenaSqlDataSource dataSource;
  
  @Override
  public <R> Uni<R> withTransaction(TransactionFunction<R> callback) {
    return dataSource.getPool().withTransaction(conn -> callback.apply(new GitDbStateImpl(dataSource.withTx(conn))));
  }
  @Override
  public GitInserts insert() {
    return new GitDbInsertsSqlPool(dataSource);
  }
  @Override
  public GitQueries query() {
    return new GitDbQueriesSqlImpl(dataSource);
  }
  @Override
  public ThenaSqlDataSource getDataSource() {
    return dataSource;
  }
}
