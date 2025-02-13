package io.resys.thena.datasource.vertx;

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

import java.util.function.Function;

import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.ThenaSqlPool;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ThenaSqlPoolVertx implements ThenaSqlPool {
  
  private final io.vertx.mutiny.sqlclient.Pool realPool;
  
  @Override
  public ThenaPreparedQuery<RowSet<Row>> preparedQuery(String sql) {
    return new ThenaPreparedQueryVertx<RowSet<Row>>(realPool, sql);
  }
  @Override
  public ThenaQuery<RowSet<Row>> query(String sql) {
    return new ThenaQueryVertx<RowSet<Row>>(realPool, sql);
  }
  @Override
  public <T> Uni<T> withTransaction(Function<ThenaSqlClient, Uni<T>> function) {
    return realPool.withTransaction(realTxClient -> doInTx(realTxClient, function));
  }
  public <T> Uni<T> doInTx(io.vertx.mutiny.sqlclient.SqlConnection realTxClient, Function<ThenaSqlClient, Uni<T>> function) {
    final var delegate = new ThenaSqlClientVertx(realTxClient);
    final Uni<T> result = function.apply(delegate);
    return result;
  }
  @Override
  public Uni<Void> rollback() {
    return Uni.createFrom().voidItem();
  }
}
