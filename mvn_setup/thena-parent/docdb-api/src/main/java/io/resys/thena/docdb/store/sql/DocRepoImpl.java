package io.resys.thena.docdb.store.sql;

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
import io.resys.thena.docdb.models.doc.DocInserts;
import io.resys.thena.docdb.models.doc.DocQueries;
import io.resys.thena.docdb.models.doc.DocState.DocRepo;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.store.sql.factories.GitDbQueriesSqlImpl.ClientQuerySqlContext;
import io.resys.thena.docdb.store.sql.queries.DocDbInsertsSqlPool;
import io.resys.thena.docdb.store.sql.factories.ImmutableClientQuerySqlContext;
import io.resys.thena.docdb.store.sql.support.ImmutableSqlClientWrapper;
import io.resys.thena.docdb.support.ErrorHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocRepoImpl implements DocRepo {
  private final ImmutableSqlClientWrapper wrapper;
  private final ErrorHandler handler; 
  private final Function<DbCollections, SqlMapper> sqlMapper;
  private final Function<DbCollections, SqlBuilder> sqlBuilder;
  private final Function<ClientQuerySqlContext, DocQueries> clientQuery;
  
  @Override
  public String getRepoName() {
    return wrapper.getRepo().getName();
  }
  @Override
  public Repo getRepo() {
    return wrapper.getRepo();
  }
  @Override
  public DocQueries query() {
    final var ctx = ImmutableClientQuerySqlContext.builder()
        .mapper(sqlMapper.apply(wrapper.getNames()))
        .builder(sqlBuilder.apply(wrapper.getNames()))
        .wrapper(wrapper)
        .errorHandler(handler)
        .build();
      
    return clientQuery.apply(ctx);
  }
  @Override
  public DocInserts insert() {
    return new DocDbInsertsSqlPool(wrapper, sqlMapper.apply(wrapper.getNames()), sqlBuilder.apply(wrapper.getNames()), handler);
  }
}
