package io.resys.thena.docdb.sql;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.spi.ClientInsertBuilder;
import io.resys.thena.docdb.spi.ClientQuery;
import io.resys.thena.docdb.spi.ClientState.ClientRepoState;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.sql.factories.ClientQuerySqlPool.ClientQuerySqlContext;
import io.resys.thena.docdb.sql.factories.ImmutableClientQuerySqlContext;
import io.resys.thena.docdb.sql.queries.ClientInsertBuilderSqlPool;
import io.resys.thena.docdb.sql.support.ImmutableSqlClientWrapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SqlClientRepoState implements ClientRepoState {
  private final ImmutableSqlClientWrapper wrapper;
  private final ErrorHandler handler; 
  private final Function<ClientCollections, SqlMapper> sqlMapper;
  private final Function<ClientCollections, SqlBuilder> sqlBuilder;
  private final Function<ClientQuerySqlContext, ClientQuery> clientQuery;
  
  @Override
  public String getRepoName() {
    return wrapper.getRepo().getName();
  }
  @Override
  public Repo getRepo() {
    return wrapper.getRepo();
  }
  @Override
  public ClientQuery query() {
    final var ctx = ImmutableClientQuerySqlContext.builder()
        .mapper(sqlMapper.apply(wrapper.getNames()))
        .builder(sqlBuilder.apply(wrapper.getNames()))
        .wrapper(wrapper)
        .errorHandler(handler)
        .build();
      
    return clientQuery.apply(ctx);
  }
  @Override
  public ClientInsertBuilder insert() {
    return new ClientInsertBuilderSqlPool(wrapper, sqlMapper.apply(wrapper.getNames()), sqlBuilder.apply(wrapper.getNames()), handler);
  }
}
