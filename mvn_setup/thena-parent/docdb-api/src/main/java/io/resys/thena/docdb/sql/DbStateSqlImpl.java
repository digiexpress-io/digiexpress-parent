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
import io.resys.thena.docdb.spi.DocDbQueries;
import io.resys.thena.docdb.spi.DocDbState;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.spi.GitDbQueries;
import io.resys.thena.docdb.spi.GitDbState;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.resys.thena.docdb.sql.factories.DocDbQueriesSqlImpl;
import io.resys.thena.docdb.sql.factories.GitDbQueriesSqlImpl;
import io.resys.thena.docdb.sql.factories.GitDbQueriesSqlImpl.ClientQuerySqlContext;
import io.resys.thena.docdb.sql.factories.SqlBuilderImpl;
import io.resys.thena.docdb.sql.factories.SqlMapperImpl;
import io.resys.thena.docdb.sql.factories.SqlSchemaImpl;
import io.resys.thena.docdb.sql.queries.RepoBuilderSqlPool;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DbStateSqlImpl implements DbState {
  final DbCollections ctx;
  final io.vertx.mutiny.sqlclient.Pool pool; 
  final ErrorHandler handler;
  final Function<DbCollections, SqlSchema> sqlSchema; 
  final Function<DbCollections, SqlMapper> sqlMapper;
  final Function<DbCollections, SqlBuilder> sqlBuilder;
  final Function<ClientQuerySqlContext, GitDbQueries> gitQuery;
  final Function<ClientQuerySqlContext, DocDbQueries> docQuery;
  
  @Override public ErrorHandler getErrorHandler() { return handler; }
  @Override public DbCollections getCollections() { return ctx; }
  
  @Override
  public RepoBuilder project() {
    return new RepoBuilderSqlPool(pool, null, ctx, sqlSchema.apply(ctx), sqlMapper.apply(ctx), sqlBuilder.apply(ctx), handler);
  }
  @Override
  public GitDbState toGitState() {
    return new GitDbStateImpl(ctx, pool, handler, sqlSchema, sqlMapper, sqlBuilder, gitQuery);
  }
  
  @Override
  public DocDbState toDocState() {
    return new DocDbStateImpl(ctx, pool, handler, sqlSchema, sqlMapper, sqlBuilder, docQuery);
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
  
  public static Builder create() {
    return new Builder();
  }

  public static class Builder {
    private io.vertx.mutiny.sqlclient.Pool client;
    private String db = "docdb";
    private ErrorHandler errorHandler;
    private Function<DbCollections, SqlSchema> sqlSchema; 
    private Function<DbCollections, SqlMapper> sqlMapper;
    private Function<DbCollections, SqlBuilder> sqlBuilder;
    private Function<ClientQuerySqlContext, GitDbQueries> gitQuery;
    private Function<ClientQuerySqlContext, DocDbQueries> docQuery;
    
    public Builder sqlMapper(Function<DbCollections, SqlMapper> sqlMapper) {this.sqlMapper = sqlMapper; return this; }
    public Builder sqlBuilder(Function<DbCollections, SqlBuilder> sqlBuilder) {this.sqlBuilder = sqlBuilder; return this; }
    public Builder sqlSchema(Function<DbCollections, SqlSchema> sqlSchema) {this.sqlSchema = sqlSchema; return this; }
    public Builder gitQuery(Function<ClientQuerySqlContext, GitDbQueries> sqlQuery) {this.gitQuery = sqlQuery; return this; }
    public Builder docQuery(Function<ClientQuerySqlContext, DocDbQueries> docQuery) {this.docQuery = docQuery; return this; }
    
    public Builder errorHandler(ErrorHandler errorHandler) {this.errorHandler = errorHandler; return this; }
    public Builder db(String db) { this.db = db; return this; }
    public Builder client(io.vertx.mutiny.sqlclient.Pool client) { this.client = client; return this; }

    public static SqlBuilder defaultSqlBuilder(DbCollections ctx) { return new SqlBuilderImpl(ctx); }
    public static SqlMapper defaultSqlMapper(DbCollections ctx) { return new SqlMapperImpl(ctx); }
    public static SqlSchema defaultSqlSchema(DbCollections ctx) { return new SqlSchemaImpl(ctx); }
    public static GitDbQueries defaultGitQuery(ClientQuerySqlContext ctx) { return new GitDbQueriesSqlImpl(ctx); }
    public static DocDbQueries defaultDocQuery(ClientQuerySqlContext ctx) { return new DocDbQueriesSqlImpl(ctx); }
    
    public DocDB build() {
      RepoAssert.notNull(client, () -> "client must be defined!");
      RepoAssert.notNull(db, () -> "db must be defined!");
      
      this.errorHandler = new PgErrors();

      final var ctx = DbCollections.defaults(db);
      final Function<DbCollections, SqlSchema> sqlSchema = this.sqlSchema == null ? Builder::defaultSqlSchema : this.sqlSchema;
      final Function<DbCollections, SqlMapper> sqlMapper = this.sqlMapper == null ? Builder::defaultSqlMapper : this.sqlMapper;
      final Function<DbCollections, SqlBuilder> sqlBuilder = this.sqlBuilder == null ? Builder::defaultSqlBuilder : this.sqlBuilder;
      final Function<ClientQuerySqlContext, GitDbQueries> gitQuery = this.gitQuery == null ? Builder::defaultGitQuery : this.gitQuery;
      final Function<ClientQuerySqlContext, DocDbQueries> docQuery = this.gitQuery == null ? Builder::defaultDocQuery : this.docQuery;
      
      
      final var state = new DbStateSqlImpl(ctx, client, errorHandler, sqlSchema, sqlMapper, sqlBuilder, gitQuery, docQuery);
      
      return new DocDBDefault(state);
    }
  }
}
