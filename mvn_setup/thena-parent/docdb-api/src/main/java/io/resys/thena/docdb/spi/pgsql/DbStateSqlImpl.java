package io.resys.thena.docdb.spi.pgsql;

import java.util.function.Function;

/*-
 * #%L
 * thena-docdb-pgsql
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.spi.DocDBDefault;
import io.resys.thena.docdb.spi.DocDbQueries;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.spi.GitDbQueries;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.resys.thena.docdb.sql.DbStateImpl;
import io.resys.thena.docdb.sql.SqlBuilder;
import io.resys.thena.docdb.sql.SqlMapper;
import io.resys.thena.docdb.sql.SqlSchema;
import io.resys.thena.docdb.sql.factories.DocDbQueriesSqlImpl;
import io.resys.thena.docdb.sql.factories.GitDbQueriesSqlImpl;
import io.resys.thena.docdb.sql.factories.GitDbQueriesSqlImpl.ClientQuerySqlContext;
import io.vertx.mutiny.sqlclient.Pool;


public class DbStateSqlImpl extends DbStateImpl implements DbState {


  public DbStateSqlImpl(
      DbCollections ctx, Pool client, ErrorHandler handler,
      Function<DbCollections, SqlSchema> sqlSchema, 
      Function<DbCollections, SqlMapper> sqlMapper,
      Function<DbCollections, SqlBuilder> sqlBuilder,
      Function<ClientQuerySqlContext, GitDbQueries> gitQuery,
      Function<ClientQuerySqlContext, DocDbQueries> docQuery) {
    super(ctx, client, handler, sqlSchema, sqlMapper, sqlBuilder, gitQuery, docQuery);
  }

  public static DbState state(
      final DbCollections ctx,
      final io.vertx.mutiny.sqlclient.Pool client, 
      final ErrorHandler handler) {
    
    return new DbStateSqlImpl(
        ctx, client, handler, 
        Builder::defaultSqlSchema, 
        Builder::defaultSqlMapper,
        Builder::defaultSqlBuilder,
        Builder::defaultGitQuery,
        Builder::defaultDocQuery);
  }
  
  public static DbStateImpl.Builder create() {
    return new Builder();
  }

  public static class Builder extends DbStateImpl.Builder {
    public Builder() {
      super.errorHandler = new PgErrors();
      super.sqlBuilder = Builder::defaultSqlBuilder;
      super.sqlMapper = Builder::defaultSqlMapper;
      super.sqlSchema = Builder::defaultSqlSchema;
      super.gitQuery = Builder::defaultGitQuery;
      super.docQuery = Builder::defaultDocQuery;
    }
    public static GitDbQueries defaultGitQuery(ClientQuerySqlContext ctx) {
      return new GitDbQueriesSqlImpl(ctx);
    }
    public static DocDbQueries defaultDocQuery(ClientQuerySqlContext ctx) {
      return new DocDbQueriesSqlImpl(ctx);
    }    
    public DocDB build() {
      RepoAssert.notNull(client, () -> "client must be defined!");
      RepoAssert.notNull(db, () -> "db must be defined!");
      RepoAssert.notNull(errorHandler, () -> "errorHandler must be defined!");

      final var ctx = DbCollections.defaults(db);
      final var state = new DbStateSqlImpl(ctx, client, errorHandler, sqlSchema, sqlMapper, sqlBuilder, gitQuery, docQuery);
      return new DocDBDefault(state);
    }
  }
}
