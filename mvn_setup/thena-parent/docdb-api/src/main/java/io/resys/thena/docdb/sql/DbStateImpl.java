package io.resys.thena.docdb.sql;

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
import io.resys.thena.docdb.spi.DocDbState;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.spi.GitDbQueries;
import io.resys.thena.docdb.spi.GitDbState;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.resys.thena.docdb.sql.factories.ClientQuerySqlPool;
import io.resys.thena.docdb.sql.factories.ClientQuerySqlPool.ClientQuerySqlContext;
import io.resys.thena.docdb.sql.factories.SqlBuilderImpl;
import io.resys.thena.docdb.sql.factories.SqlMapperImpl;
import io.resys.thena.docdb.sql.factories.SqlSchemaImpl;
import io.resys.thena.docdb.sql.queries.RepoBuilderSqlPool;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DbStateImpl implements DbState {
  final DbCollections ctx;
  final io.vertx.mutiny.sqlclient.Pool pool; 
  final ErrorHandler handler;
  final Function<DbCollections, SqlSchema> sqlSchema; 
  final Function<DbCollections, SqlMapper> sqlMapper;
  final Function<DbCollections, SqlBuilder> sqlBuilder;
  final Function<ClientQuerySqlContext, GitDbQueries> clientQuery;
  
  @Override public ErrorHandler getErrorHandler() { return handler; }
  @Override public DbCollections getCollections() { return ctx; }
  
  @Override
  public RepoBuilder project() {
    return new RepoBuilderSqlPool(pool, null, ctx, sqlSchema.apply(ctx), sqlMapper.apply(ctx), sqlBuilder.apply(ctx), handler);
  }
  @Override
  public GitDbState toGitState() {
    return new GitDbStateImpl(ctx, pool, handler, sqlSchema, sqlMapper, sqlBuilder, clientQuery);
  }
  
  @Override
  public DocDbState toDocState() {
    // TODO Auto-generated method stub
    throw new RuntimeException("not implemented");
  }
  public static DbState state(
      final DbCollections ctx,
      final io.vertx.mutiny.sqlclient.Pool client, 
      final ErrorHandler handler) {
    
    return new DbStateImpl(
        ctx, client, handler, 
        Builder::defaultSqlSchema, 
        Builder::defaultSqlMapper,
        Builder::defaultSqlBuilder,
        Builder::defaultSqlQuery);
  }
  
  public static Builder create() {
    return new Builder();
  }

  public static class Builder {
    protected io.vertx.mutiny.sqlclient.Pool client;
    protected String db = "docdb";
    protected ErrorHandler errorHandler;
    protected Function<DbCollections, SqlSchema> sqlSchema; 
    protected Function<DbCollections, SqlMapper> sqlMapper;
    protected Function<DbCollections, SqlBuilder> sqlBuilder;
    protected Function<ClientQuerySqlContext, GitDbQueries> sqlQuery;

    
    public Builder sqlMapper(Function<DbCollections, SqlMapper> sqlMapper) {this.sqlMapper = sqlMapper; return this; }
    public Builder sqlBuilder(Function<DbCollections, SqlBuilder> sqlBuilder) {this.sqlBuilder = sqlBuilder; return this; }
    public Builder sqlSchema(Function<DbCollections, SqlSchema> sqlSchema) {this.sqlSchema = sqlSchema; return this; }
    public Builder sqlQuery(Function<ClientQuerySqlContext, GitDbQueries> sqlQuery) {this.sqlQuery = sqlQuery; return this; }
    
    public Builder errorHandler(ErrorHandler errorHandler) {this.errorHandler = errorHandler; return this; }
    public Builder db(String db) { this.db = db; return this; }
    public Builder client(io.vertx.mutiny.sqlclient.Pool client) { this.client = client; return this; }

    public static SqlBuilder defaultSqlBuilder(DbCollections ctx) { return new SqlBuilderImpl(ctx); }
    public static SqlMapper defaultSqlMapper(DbCollections ctx) { return new SqlMapperImpl(ctx); }
    public static SqlSchema defaultSqlSchema(DbCollections ctx) { return new SqlSchemaImpl(ctx); }
    public static GitDbQueries defaultSqlQuery(ClientQuerySqlContext ctx) { return new ClientQuerySqlPool(ctx); }
    
    public DocDB build() {
      RepoAssert.notNull(client, () -> "client must be defined!");
      RepoAssert.notNull(db, () -> "db must be defined!");
      RepoAssert.notNull(errorHandler, () -> "errorHandler must be defined!");

      final var ctx = DbCollections.defaults(db);
      final Function<DbCollections, SqlSchema> sqlSchema = this.sqlSchema == null ? Builder::defaultSqlSchema : this.sqlSchema;
      final Function<DbCollections, SqlMapper> sqlMapper = this.sqlMapper == null ? Builder::defaultSqlMapper : this.sqlMapper;
      final Function<DbCollections, SqlBuilder> sqlBuilder = this.sqlBuilder == null ? Builder::defaultSqlBuilder : this.sqlBuilder;
      final Function<ClientQuerySqlContext, GitDbQueries> sqlQuery = this.sqlQuery == null ? Builder::defaultSqlQuery : this.sqlQuery;
      final var state = new DbStateImpl(ctx, client, errorHandler, sqlSchema, sqlMapper, sqlBuilder, sqlQuery);
      
      return new DocDBDefault(state);
    }
  }
}
