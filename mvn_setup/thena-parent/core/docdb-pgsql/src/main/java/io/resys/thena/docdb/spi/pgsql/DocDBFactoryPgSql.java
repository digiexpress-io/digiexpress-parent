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
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.spi.ClientQuery;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.DocDBDefault;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.resys.thena.docdb.sql.DocDBFactorySql;
import io.resys.thena.docdb.sql.SqlBuilder;
import io.resys.thena.docdb.sql.SqlMapper;
import io.resys.thena.docdb.sql.SqlSchema;
import io.resys.thena.docdb.sql.factories.ClientQuerySqlPool;
import io.resys.thena.docdb.sql.factories.ClientQuerySqlPool.ClientQuerySqlContext;
import io.vertx.mutiny.sqlclient.Pool;


public class DocDBFactoryPgSql extends DocDBFactorySql implements ClientState {


  public DocDBFactoryPgSql(
      ClientCollections ctx, Pool client, ErrorHandler handler,
      Function<ClientCollections, SqlSchema> sqlSchema, 
      Function<ClientCollections, SqlMapper> sqlMapper,
      Function<ClientCollections, SqlBuilder> sqlBuilder,
      Function<ClientQuerySqlContext, ClientQuery> clientQuery) {
    super(ctx, client, handler, sqlSchema, sqlMapper, sqlBuilder, clientQuery);
  }

  public static ClientState state(
      final ClientCollections ctx,
      final io.vertx.mutiny.sqlclient.Pool client, 
      final ErrorHandler handler) {
    
    return new DocDBFactoryPgSql(
        ctx, client, handler, 
        Builder::defaultSqlSchema, 
        Builder::defaultSqlMapper,
        Builder::defaultSqlBuilder,
        Builder::defaultSqlQuery);
  }
  
  public static DocDBFactorySql.Builder create() {
    return new Builder();
  }

  public static class Builder extends DocDBFactorySql.Builder {
    public Builder() {
      super.errorHandler = new PgErrors();
      super.sqlBuilder = Builder::defaultSqlBuilder;
      super.sqlMapper = Builder::defaultSqlMapper;
      super.sqlSchema = Builder::defaultSqlSchema;
      super.sqlQuery = Builder::defaultSqlQuery;
    }

    public static SqlBuilder defaultSqlBuilder(ClientCollections ctx) {
      return new SqlBuilderPg(ctx);
    }
    public static SqlMapper defaultSqlMapper(ClientCollections ctx) {
      return new SqlMapperPg(ctx);
    }
    public static SqlSchema defaultSqlSchema(ClientCollections ctx) {
      return new SqlSchemaPg(ctx);
    }
    public static ClientQuery defaultSqlQuery(ClientQuerySqlContext ctx) {
      return new ClientQuerySqlPool(ctx);
    }
    
    public DocDB build() {
      RepoAssert.notNull(client, () -> "client must be defined!");
      RepoAssert.notNull(db, () -> "db must be defined!");
      RepoAssert.notNull(errorHandler, () -> "errorHandler must be defined!");

      final var ctx = ClientCollections.defaults(db);
      final var state = new DocDBFactoryPgSql(ctx, client, errorHandler, sqlSchema, sqlMapper, sqlBuilder, sqlQuery);
      return new DocDBDefault(state);
    }
  }
}
