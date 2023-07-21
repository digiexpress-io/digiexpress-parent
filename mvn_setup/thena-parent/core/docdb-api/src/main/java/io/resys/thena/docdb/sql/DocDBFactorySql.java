package io.resys.thena.docdb.sql;

import java.util.Optional;
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
import io.resys.thena.docdb.api.exceptions.RepoException;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.spi.ClientInsertBuilder;
import io.resys.thena.docdb.spi.ClientQuery;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.DocDBDefault;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.resys.thena.docdb.sql.factories.ClientQuerySqlPool;
import io.resys.thena.docdb.sql.factories.ClientQuerySqlPool.ClientQuerySqlContext;
import io.resys.thena.docdb.sql.factories.ImmutableClientQuerySqlContext;
import io.resys.thena.docdb.sql.factories.SqlBuilderImpl;
import io.resys.thena.docdb.sql.factories.SqlMapperImpl;
import io.resys.thena.docdb.sql.factories.SqlSchemaImpl;
import io.resys.thena.docdb.sql.queries.ClientInsertBuilderSqlPool;
import io.resys.thena.docdb.sql.queries.RepoBuilderSqlPool;
import io.resys.thena.docdb.sql.support.ImmutableSqlClientWrapper;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class DocDBFactorySql implements ClientState {
  final ClientCollections ctx;
  final io.vertx.mutiny.sqlclient.Pool pool; 
  final ErrorHandler handler;
  final Function<ClientCollections, SqlSchema> sqlSchema; 
  final Function<ClientCollections, SqlMapper> sqlMapper;
  final Function<ClientCollections, SqlBuilder> sqlBuilder;
  final Function<ClientQuerySqlContext, ClientQuery> clientQuery;
  
  @Override public ErrorHandler getErrorHandler() { return handler; }
  @Override public ClientCollections getCollections() { return ctx; }
  
  @Override
  public <R> Uni<R> withTransaction(String repoId, String headName, TransactionFunction<R> callback) {
    return pool.withTransaction(conn -> {
      final var repoPool = new RepoBuilderSqlPool(pool, conn, ctx, sqlSchema.apply(ctx), sqlMapper.apply(ctx), sqlBuilder.apply(ctx), handler);
      return repoPool.getByNameOrId(repoId)
        .onItem().transformToUni((repo -> {
          if(repo == null) {
            return repoPool.findAll().collect().asList().onItem().transform(repos -> {
              final var ex = RepoException.builder().notRepoWithName(repoId, repos);
              log.error(ex.getText());
              throw new RepoException(ex.getText());
            });
          }
          
          return Uni.createFrom().item(repo);
        }))
        .onItem().transformToUni((Repo existing) -> {
          if(existing == null) {
            final var ex = RepoException.builder().notRepoWithName(repoId);
            log.error(ex.getText());
            throw new RepoException(ex.getText());
          }
          final var wrapper = ImmutableSqlClientWrapper.builder()
              .repo(existing)
              .pool(pool)
              .tx(conn)
              .names(ctx.toRepo(existing))
              .build();
          return callback.apply(new SqlClientRepoState(wrapper, handler, sqlMapper, sqlBuilder, clientQuery));
        });
    });
  }
  @Override
  public Uni<ClientQuery> query(String repoNameOrId) {
    return project().getByNameOrId(repoNameOrId).onItem().transform(repo -> query(repo));
  }
  @Override
  public Uni<ClientInsertBuilder> insert(String repoNameOrId) {
    return project().getByNameOrId(repoNameOrId).onItem().transform(repo -> insert(repo));
  }
  @Override
  public Uni<ClientRepoState> withRepo(String repoNameOrId) {
    return project().getByNameOrId(repoNameOrId).onItem().transform(repo -> withRepo(repo));
  }
  @Override
  public RepoBuilder project() {
    return new RepoBuilderSqlPool(pool, null, ctx, sqlSchema.apply(ctx), sqlMapper.apply(ctx), sqlBuilder.apply(ctx), handler);
  }
  @Override
  public ClientInsertBuilder insert(Repo repo) {
    final var wrapper = ImmutableSqlClientWrapper.builder()
        .repo(repo)
        .pool(pool)
        .tx(Optional.empty())
        .names(ctx.toRepo(repo))
        .build();
    return new ClientInsertBuilderSqlPool(wrapper, sqlMapper.apply(wrapper.getNames()), sqlBuilder.apply(wrapper.getNames()), handler);
  }
  @Override
  public ClientQuery query(Repo repo) {
    final var wrapper = ImmutableSqlClientWrapper.builder()
        .repo(repo)
        .pool(pool)
        .tx(Optional.empty())
        .names(ctx.toRepo(repo))
        .build();
    final var ctx = ImmutableClientQuerySqlContext.builder()
      .mapper(sqlMapper.apply(wrapper.getNames()))
      .builder(sqlBuilder.apply(wrapper.getNames()))
      .wrapper(wrapper)
      .errorHandler(handler)
      .build();
    
    return clientQuery.apply(ctx);
  }
  @Override
  public ClientRepoState withRepo(Repo repo) {
    final var wrapper = ImmutableSqlClientWrapper.builder()
        .repo(repo)
        .pool(pool)
        .tx(Optional.empty())
        .names(ctx.toRepo(repo))
        .build();
    return new SqlClientRepoState(wrapper, handler, sqlMapper, sqlBuilder, clientQuery);
  }
  
  public static ClientState state(
      final ClientCollections ctx,
      final io.vertx.mutiny.sqlclient.Pool client, 
      final ErrorHandler handler) {
    
    return new DocDBFactorySql(
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
    protected Function<ClientCollections, SqlSchema> sqlSchema; 
    protected Function<ClientCollections, SqlMapper> sqlMapper;
    protected Function<ClientCollections, SqlBuilder> sqlBuilder;
    protected Function<ClientQuerySqlContext, ClientQuery> sqlQuery;

    
    public Builder sqlMapper(Function<ClientCollections, SqlMapper> sqlMapper) {this.sqlMapper = sqlMapper; return this; }
    public Builder sqlBuilder(Function<ClientCollections, SqlBuilder> sqlBuilder) {this.sqlBuilder = sqlBuilder; return this; }
    public Builder sqlSchema(Function<ClientCollections, SqlSchema> sqlSchema) {this.sqlSchema = sqlSchema; return this; }
    public Builder sqlQuery(Function<ClientQuerySqlContext, ClientQuery> sqlQuery) {this.sqlQuery = sqlQuery; return this; }
    
    public Builder errorHandler(ErrorHandler errorHandler) {this.errorHandler = errorHandler; return this; }
    public Builder db(String db) { this.db = db; return this; }
    public Builder client(io.vertx.mutiny.sqlclient.Pool client) { this.client = client; return this; }
    

    public static SqlBuilder defaultSqlBuilder(ClientCollections ctx) {
      return new SqlBuilderImpl(ctx);
    }
    public static SqlMapper defaultSqlMapper(ClientCollections ctx) {
      return new SqlMapperImpl(ctx);
    }
    public static SqlSchema defaultSqlSchema(ClientCollections ctx) {
      return new SqlSchemaImpl(ctx);
    }
    public static ClientQuery defaultSqlQuery(ClientQuerySqlContext ctx) {
      return new ClientQuerySqlPool(ctx);
    }
    
    public DocDB build() {
      RepoAssert.notNull(client, () -> "client must be defined!");
      RepoAssert.notNull(db, () -> "db must be defined!");
      RepoAssert.notNull(errorHandler, () -> "errorHandler must be defined!");

      final var ctx = ClientCollections.defaults(db);
      final Function<ClientCollections, SqlSchema> sqlSchema = this.sqlSchema == null ? Builder::defaultSqlSchema : this.sqlSchema;
      final Function<ClientCollections, SqlMapper> sqlMapper = this.sqlMapper == null ? Builder::defaultSqlMapper : this.sqlMapper;
      final Function<ClientCollections, SqlBuilder> sqlBuilder = this.sqlBuilder == null ? Builder::defaultSqlBuilder : this.sqlBuilder;
      final Function<ClientQuerySqlContext, ClientQuery> sqlQuery = this.sqlQuery == null ? Builder::defaultSqlQuery : this.sqlQuery;
      final var state = new DocDBFactorySql(ctx, client, errorHandler, sqlSchema, sqlMapper, sqlBuilder, sqlQuery);
      
      return new DocDBDefault(state);
    }
  }
}
