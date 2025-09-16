package io.resys.thena.storesql;

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

import java.util.Optional;

import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.exceptions.RepoException;
import io.resys.thena.datasource.TenantCacheImpl;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.datasource.ThenaDataSource;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSource.TenantCache;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceImpl;
import io.resys.thena.datasource.vertx.ThenaSqlPoolVertx;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ThenaClientPgSql;
import io.resys.thena.structures.fs.FsState;
import io.resys.thena.structures.git.GitState;
import io.resys.thena.structures.git.GitState.TransactionFunction;
import io.resys.thena.structures.org.OrgState;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class DbStateSqlImpl implements DbState {
  private final ThenaSqlDataSource dataSource;

  @Override
  public ThenaDataSource getDataSource() {
    return dataSource;
  }
  @Override
  public InternalTenantQuery tenant() {
    return new DbStateTenantQuery(dataSource);
  }
  
  
  
  @Override
  public Uni<FsState> toFsState(String tenantId) {
    return tenant().getByNameOrId(tenantId).onItem().transformToUni(tenant -> {
      if(tenant == null) {
        return tenantNotFound(tenantId);
      }
      return Uni.createFrom().item(toFsState(tenant));
    });
  }
  @Override
  public FsState toFsState(Tenant repo) {
    return new FsDbStateImpl(dataSource.withTenant(repo));
  }
  @Override
  public <R> Uni<R> withFsTransaction(TxScope scope, io.resys.thena.structures.fs.FsState.TransactionFunction<R> callback) {
    return toFsState(scope.getTenantId()).onItem().transformToUni(state -> {
      return state.withTransaction(callback);
    });
  }
  
  // git state
  @Override
  public Uni<GitState> toGitState(String tenantId) {
    return tenant().getByNameOrId(tenantId).onItem().transformToUni(tenant -> {
      if(tenant == null) {
        return tenantNotFound(tenantId);
      }
      return Uni.createFrom().item(toGitState(tenant));
    });
  }
  @Override
  public GitState toGitState(Tenant repo) {
    return new GitDbStateImpl(dataSource.withTenant(repo));
  }
  @Override
  public <R> Uni<R> withGitTransaction(TxScope scope, TransactionFunction<R> callback) {
    return toGitState(scope.getTenantId()).onItem().transformToUni(state -> state.withTransaction(callback));
  }
  
  // org state
  @Override
  public Uni<OrgState> toOrgState(String tenantId) {
    return tenant().getByNameOrId(tenantId).onItem().transformToUni(tenant -> {
      if(tenant == null) {
        return tenantNotFound(tenantId);
      }
      return Uni.createFrom().item(toOrgState(tenant));
    });
  }
  @Override
  public OrgState toOrgState(Tenant repo) {
    return new OrgDbStateImpl(dataSource.withTenant(repo));
  }
  @Override
  public <R> Uni<R> withOrgTransaction(TxScope scope, io.resys.thena.structures.org.OrgState.TransactionFunction<R> callback) {
    return toOrgState(scope.getTenantId()).onItem().transformToUni(state -> state.withTransaction(callback));
  }
  private <T> Uni<T> tenantNotFound(String tenantId) {
    return tenant().findAll().collect().asList().onItem().transform(repos -> {
      final var ex = RepoException.builder().notRepoWithName(tenantId, repos);
      log.error(ex.getText());
      throw new RepoException(ex.getText());
    }); 
  }

  public static DbStateSqlImpl create(TenantContext names, io.vertx.mutiny.sqlclient.Pool client, TenantCache tenantCache) {
    final var pool = new ThenaSqlPoolVertx(client);
    final var errorHandler = new PgErrors();
    final var dataSource = new ThenaSqlDataSourceImpl(
        "", names, pool, errorHandler, 
        Optional.empty(),
        tenantCache
    );
    return new DbStateSqlImpl(dataSource);
  }
  
  public static Builder create() {
    return new Builder();
  }

  public static class Builder {
    private io.vertx.mutiny.sqlclient.Pool client;
    private String db = "docdb";
    private ThenaSqlDataSourceErrorHandler errorHandler;

    private TenantCache tenantCache;    
    public Builder errorHandler(ThenaSqlDataSourceErrorHandler errorHandler) {this.errorHandler = errorHandler; return this; }
    public Builder db(String db) { this.db = db; return this; }
    public Builder tenantCache(TenantCache tenantCache) { this.tenantCache = tenantCache; return this; }
    public Builder client(io.vertx.mutiny.sqlclient.Pool client) { this.client = client; return this; }

    
    public ThenaClient build() {
      RepoAssert.notNull(client, () -> "client must be defined!");
      RepoAssert.notNull(db, () -> "db must be defined!");
      
      final var tenantCache = this.tenantCache == null ? new TenantCacheImpl() : this.tenantCache;
      
      final var ctx = TenantContext.defaults(db);
      this.errorHandler = new PgErrors();
      
      
      final var pool = new ThenaSqlPoolVertx(client);
      
      final var dataSource = new ThenaSqlDataSourceImpl(
          db, ctx, pool, errorHandler, 
          Optional.empty(),
          tenantCache
      );
      
      final var state = new DbStateSqlImpl(dataSource);
      return new ThenaClientPgSql(state);
    }
  }
}
