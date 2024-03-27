package io.resys.thena.storesql;

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

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.spi.DbCollections;
import io.resys.thena.storesql.ImmutableClientQuerySqlContext;
import io.resys.thena.storesql.GitDbQueriesSqlImpl.ClientQuerySqlContext;
import io.resys.thena.storesql.builders.GitDbInsertsSqlPool;
import io.resys.thena.storesql.support.ImmutableSqlClientWrapper;
import io.resys.thena.structures.git.GitInserts;
import io.resys.thena.structures.git.GitQueries;
import io.resys.thena.structures.git.GitState.GitTenant;
import io.resys.thena.support.ErrorHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GitRepoImpl implements GitTenant {
  private final ImmutableSqlClientWrapper wrapper;
  private final ErrorHandler handler; 
  private final Function<DbCollections, SqlMapper> sqlMapper;
  private final Function<DbCollections, SqlBuilder> sqlBuilder;
  private final Function<ClientQuerySqlContext, GitQueries> clientQuery;
  
  @Override
  public String getTenantName() {
    return wrapper.getRepo().getName();
  }
  @Override
  public Tenant getRepo() {
    return wrapper.getRepo();
  }
  @Override
  public GitQueries query() {
    final var ctx = ImmutableClientQuerySqlContext.builder()
        .mapper(sqlMapper.apply(wrapper.getNames()))
        .builder(sqlBuilder.apply(wrapper.getNames()))
        .wrapper(wrapper)
        .errorHandler(handler)
        .build();
      
    return clientQuery.apply(ctx);
  }
  @Override
  public GitInserts insert() {
    return new GitDbInsertsSqlPool(wrapper, sqlMapper.apply(wrapper.getNames()), sqlBuilder.apply(wrapper.getNames()), handler);
  }
}
