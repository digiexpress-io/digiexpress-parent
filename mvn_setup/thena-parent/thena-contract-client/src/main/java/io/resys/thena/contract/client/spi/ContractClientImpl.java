package io.resys.thena.contract.client.spi;

/*-
 * #%L
 * thena-grim-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.contract.client.api.ContractClient;
import io.resys.thena.contract.client.tables.ContractDb;
import io.resys.thena.contract.client.tables.spi.ContractDbImpl;
import io.resys.thena.datasource.TenantCacheImpl;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.datasource.ThenaSqlDataSource.TenantCache;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceImpl;
import io.resys.thena.datasource.vertx.ThenaSqlPoolVertx;
import io.resys.thena.grim.api.GrimCommitActions;
import io.resys.thena.grim.api.GrimQueryActions;
import io.resys.thena.grim.spi.actions.GrimCommitActionsImpl;
import io.resys.thena.grim.spi.actions.GrimQueryActionsImpl;
import io.resys.thena.spi.TenantActionsImpl;
import io.resys.thena.storesql.PgErrors;
import io.resys.thena.support.RepoAssert;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ContractClientImpl implements ContractClient {
  private final ContractDb startingState;
  
  @Override
  public GrimStructuredTenant grim(String repoId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    return new GrimStructuredTenant() {
      @Override public GrimQueryActions find() { return new GrimQueryActionsImpl(startingState, repoId); }
      @Override public GrimCommitActions commit() { return new GrimCommitActionsImpl(startingState, repoId); }
      @Override public GrimProjectQuery tenants() { return null; }
      @Override public String getTenantId() { return repoId; }
    };
  }
  @Override
  public GrimStructuredTenant grim(TenantCommitResult repo) {
    return grim(repo.getRepo().getId());
  }
  @Override
  public GrimStructuredTenant grim(Tenant repo) {
    return this.grim(repo.getId());
  }
  @Override
  public TenantActions tenants() {
    return new TenantActionsImpl(startingState, StructureType.contract);
  }
  
  
  public static ContractDb create(TenantContext names, io.vertx.mutiny.sqlclient.Pool client, TenantCache tenantCache) {
    final var pool = new ThenaSqlPoolVertx(client);
    final var errorHandler = new PgErrors();
    final var dataSource = new ThenaSqlDataSourceImpl(
        "", names, pool, errorHandler, 
        Optional.empty(),
        tenantCache
    );
    return new ContractDbImpl(dataSource);
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

    
    public ContractClientImpl build() {
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
      
      final var state = new ContractDbImpl(dataSource);
      return new ContractClientImpl(state);
    }
  }
}
