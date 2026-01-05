package io.digiexpress.thena.cockpit.client.spi;

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

import io.digiexpress.thena.cockpit.client.api.CockpitAware;
import io.digiexpress.thena.cockpit.client.api.CockpitClient;
import io.digiexpress.thena.cockpit.client.api.CockpitCommitActions;
import io.digiexpress.thena.cockpit.client.api.CockpitQueryActions;
import io.digiexpress.thena.cockpit.client.spi.actions.CockpitCommitActionsImpl;
import io.digiexpress.thena.cockpit.client.spi.actions.CockpitQueryActionsImpl;
import io.digiexpress.thena.cockpit.client.tables.CockpitDb;
import io.digiexpress.thena.cockpit.client.tables.spi.CockpitDbImpl;
import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.TenantCacheImpl;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.datasource.ThenaSqlDataSource.TenantCache;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceImpl;
import io.resys.thena.datasource.vertx.ThenaSqlPoolVertx;
import io.resys.thena.spi.TenantActionsImpl;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CockpitClientImpl implements CockpitClient {
  private final CockpitDb startingState;
  

  @Override
  public CockpitQueryActions queries() {
    final var tenantId = startingState.getDataSource().getTenant().getName();
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    return new CockpitQueryActionsImpl(startingState, tenantId); 
  }
  @Override 
  public CockpitCommitActions commits() {
    final var tenantId = startingState.getDataSource().getTenant().getName();
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    return new CockpitCommitActionsImpl(startingState, tenantId); 
  }
  public TenantActions tenants() {
    return new TenantActionsImpl(startingState, StructureType.cockpit);
  }
  
  
  public static CockpitDb create(TenantContext names, io.vertx.mutiny.sqlclient.Pool client, TenantCache tenantCache, ThenaSqlDataSourceErrorHandler errorHAndler) {
    final var pool = new ThenaSqlPoolVertx(client);

    final var dataSource = new ThenaSqlDataSourceImpl(
        "", names, pool, errorHAndler, 
        Optional.empty(),
        tenantCache
    );
    return new CockpitDbImpl(dataSource);
  }
  
  public String getTenantName() {
    return startingState.getDataSource().getTenant().getName();
  }
  
  public static Builder create() {
    return new Builder();
  }

  public static class Builder {
    private io.vertx.mutiny.sqlclient.Pool client;
    private String tenantName = "cockpit";
    private ThenaSqlDataSourceErrorHandler errorHandler;

    private TenantCache tenantCache;    
    public Builder errorHandler(ThenaSqlDataSourceErrorHandler errorHandler) {this.errorHandler = errorHandler; return this; }
    public Builder tenantName(String tenantName) { this.tenantName = tenantName; return this; }
    public Builder tenantCache(TenantCache tenantCache) { this.tenantCache = tenantCache; return this; }
    public Builder client(io.vertx.mutiny.sqlclient.Pool client) { this.client = client; return this; }

    
    public CockpitClientImpl build() {
      RepoAssert.notNull(client, () -> "client must be defined!");
      RepoAssert.notNull(tenantName, () -> "tenantName must be defined!");
      RepoAssert.notNull(errorHandler, () -> "errorHandler must be defined!");
      
      final var tenantCache = this.tenantCache == null ? new TenantCacheImpl() : this.tenantCache;
      final var ctx = TenantContext.defaults(tenantName);
      
      
      final var pool = new ThenaSqlPoolVertx(client);
      
      final var dataSource = new ThenaSqlDataSourceImpl(
          tenantName, ctx, pool, errorHandler, 
          Optional.empty(),
          tenantCache
      );
      
      final var state = new CockpitDbImpl(dataSource);
      return new CockpitClientImpl(state);
    }
  }

  @Override
  public <T extends CockpitAware<T>> Uni<T> register(T aware) {
    // TODO Auto-generated method stub
    return null;
  }
}
