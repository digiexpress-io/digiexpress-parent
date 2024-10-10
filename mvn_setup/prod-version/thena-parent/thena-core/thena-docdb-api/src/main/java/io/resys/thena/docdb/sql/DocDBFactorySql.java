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
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.spi.ClientInsertBuilder;
import io.resys.thena.docdb.spi.ClientQuery;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.DocDBDefault;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.resys.thena.docdb.sql.builders.ClientInsertBuilderSqlPool;
import io.resys.thena.docdb.sql.builders.RepoBuilderSqlPool;
import io.resys.thena.docdb.sql.defaults.DefaultSqlBuilder;
import io.resys.thena.docdb.sql.defaults.DefaultSqlMapper;
import io.resys.thena.docdb.sql.defaults.DefaultSqlSchema;
import io.resys.thena.docdb.sql.support.ImmutableSqlClientWrapper;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocDBFactorySql implements ClientState {
  final ClientCollections ctx;
  final io.vertx.mutiny.sqlclient.Pool client; 
  final ErrorHandler handler;
  final Function<ClientCollections, SqlSchema> sqlSchema; 
  final Function<ClientCollections, SqlMapper> sqlMapper;
  final Function<ClientCollections, SqlBuilder> sqlBuilder;
  
  
  @Override public ErrorHandler getErrorHandler() { return handler; }
  @Override public ClientCollections getCollections() { return ctx; }
  
  @Override
  public Uni<ClientQuery> query(String repoNameOrId) {
    return repos().getByNameOrId(repoNameOrId).onItem().transform(repo -> query(repo));
  }
  @Override
  public Uni<ClientInsertBuilder> insert(String repoNameOrId) {
    return repos().getByNameOrId(repoNameOrId).onItem().transform(repo -> insert(repo));
  }
  @Override
  public Uni<ClientRepoState> withRepo(String repoNameOrId) {
    return repos().getByNameOrId(repoNameOrId).onItem().transform(repo -> withRepo(repo));
  }
  @Override
  public RepoBuilder repos() {
    return new RepoBuilderSqlPool(client, ctx, sqlSchema.apply(ctx), sqlMapper.apply(ctx), sqlBuilder.apply(ctx), handler);
  }
  @Override
  public ClientInsertBuilder insert(Repo repo) {
    final var wrapper = ImmutableSqlClientWrapper.builder()
        .repo(repo)
        .client(client)
        .names(ctx.toRepo(repo))
        .build();
    return new ClientInsertBuilderSqlPool(wrapper.getClient(), sqlMapper.apply(wrapper.getNames()), sqlBuilder.apply(wrapper.getNames()), handler);
  }
  @Override
  public ClientQuery query(Repo repo) {
    final var wrapper = ImmutableSqlClientWrapper.builder()
        .repo(repo)
        .client(client)
        .names(ctx.toRepo(repo))
        .build();
    return new ClientQuerySqlPool(wrapper, sqlMapper.apply(wrapper.getNames()), sqlBuilder.apply(wrapper.getNames()), handler);
  }
  @Override
  public ClientRepoState withRepo(Repo repo) {
    final var wrapper = ImmutableSqlClientWrapper.builder()
        .repo(repo)
        .client(client)
        .names(ctx.toRepo(repo))
        .build();
    return new ClientRepoState() {
      @Override
      public ClientQuery query() {
        return new ClientQuerySqlPool(wrapper, sqlMapper.apply(wrapper.getNames()), sqlBuilder.apply(wrapper.getNames()), handler);
      }
      @Override
      public ClientInsertBuilder insert() {
        return new ClientInsertBuilderSqlPool(wrapper.getClient(), sqlMapper.apply(wrapper.getNames()), sqlBuilder.apply(wrapper.getNames()), handler);
      }
    };
  }
  

  public static ClientState state(
      final ClientCollections ctx,
      final io.vertx.mutiny.sqlclient.Pool client, 
      final ErrorHandler handler) {
    
    return new DocDBFactorySql(
        ctx, client, handler, 
        Builder::defaultSqlSchema, 
        Builder::defaultSqlMapper,
        Builder::defaultSqlBuilder);
  }
  
  public static Builder create() {
    return new Builder();
  }

  public static class Builder {
    private io.vertx.mutiny.sqlclient.Pool client;
    private String db = "docdb";
    private ErrorHandler errorHandler;
    private Function<ClientCollections, SqlSchema> sqlSchema; 
    private Function<ClientCollections, SqlMapper> sqlMapper;
    private Function<ClientCollections, SqlBuilder> sqlBuilder;

    public Builder sqlMapper(Function<ClientCollections, SqlMapper> sqlMapper) {this.sqlMapper = sqlMapper; return this; }
    public Builder sqlBuilder(Function<ClientCollections, SqlBuilder> sqlBuilder) {this.sqlBuilder = sqlBuilder; return this; }
    public Builder sqlSchema(Function<ClientCollections, SqlSchema> sqlSchema) {this.sqlSchema = sqlSchema; return this; }
    public Builder errorHandler(ErrorHandler errorHandler) {this.errorHandler = errorHandler; return this; }
    public Builder db(String db) { this.db = db; return this; }
    public Builder client(io.vertx.mutiny.sqlclient.Pool client) { this.client = client; return this; }
    

    public static SqlBuilder defaultSqlBuilder(ClientCollections ctx) {
      return new DefaultSqlBuilder(ctx);
    }
    public static SqlMapper defaultSqlMapper(ClientCollections ctx) {
      return new DefaultSqlMapper(ctx);
    }
    public static SqlSchema defaultSqlSchema(ClientCollections ctx) {
      return new DefaultSqlSchema(ctx);
    }
    
    public DocDB build() {
      RepoAssert.notNull(client, () -> "client must be defined!");
      RepoAssert.notNull(db, () -> "db must be defined!");
      RepoAssert.notNull(errorHandler, () -> "errorHandler must be defined!");

      final var ctx = ClientCollections.defaults(db);
      final Function<ClientCollections, SqlSchema> sqlSchema = this.sqlSchema == null ? Builder::defaultSqlSchema : this.sqlSchema;
      final Function<ClientCollections, SqlMapper> sqlMapper = this.sqlMapper == null ? Builder::defaultSqlMapper : this.sqlMapper;
      final Function<ClientCollections, SqlBuilder> sqlBuilder = this.sqlBuilder == null ? Builder::defaultSqlBuilder : this.sqlBuilder;
      final var state = new DocDBFactorySql(ctx, client, errorHandler, sqlSchema, sqlMapper, sqlBuilder);
      
      return new DocDBDefault(state);
    }
  }
}
