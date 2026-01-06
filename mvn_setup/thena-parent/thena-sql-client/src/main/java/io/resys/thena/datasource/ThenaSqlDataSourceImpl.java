package io.resys.thena.datasource;

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

import io.resys.thena.api.entities.ImmutableTenant;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.ThenaSqlClient.ThenaSqlPool;

public class ThenaSqlDataSourceImpl implements ThenaSqlDataSource {
  private final Tenant tenant;
  private final TenantContext tenantTableNames;
  private final ThenaSqlPool pool;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  private final Optional<ThenaSqlClient> tx;
  private final boolean isTenantLoaded;
  private final TenantCache tenantCache;
  
  public ThenaSqlDataSourceImpl(
      Tenant tenant, 
      TenantContext tenantTableNames, 
      ThenaSqlPool pool,
      ThenaSqlDataSourceErrorHandler errorHandler, 
      Optional<ThenaSqlClient> tx,
      TenantCache tenantCache) {
    super();
    this.tenant = tenant;
    this.tenantTableNames = tenantTableNames.withTenant(tenant);
    this.errorHandler = errorHandler;
    this.pool = pool;
    this.tx = tx;
    this.isTenantLoaded = !tenant.getId().equals("") && !tenant.getPrefix().equals("");
    this.tenantCache = tenantCache;
  }
  
  public ThenaSqlDataSourceImpl(
      String tenantName, 
      TenantContext tenantTableNames, 
      ThenaSqlPool pool,
      ThenaSqlDataSourceErrorHandler errorHandler, 
      Optional<ThenaSqlClient> tx, 
      TenantCache tenantCache) {
    super();
    this.tenantCache = tenantCache;
    this.isTenantLoaded = false;
    this.tenant = ImmutableTenant.builder()
        .name(tenantName)
        .type(StructureType.git)
        .id("")
        .rev("")
        .prefix("")
        .build();
    this.tenantTableNames = tenantTableNames.withTenant(this.tenant);
    this.errorHandler = errorHandler;
    this.pool = pool;
    this.tx = tx;
  }
  
  @Override
  public Tenant getTenant() {
    return tenant;
  }
  @Override
  public TenantContext getTenantContext() {
    return tenantTableNames;
  }
  @Override
  public ThenaSqlPool getPool() {
    return pool;
  }
  @Override
  public ThenaSqlDataSourceErrorHandler getErrorHandler() {
    return errorHandler;
  }
  @Override
  public Optional<ThenaSqlClient> getTx() {
    return tx;
  }
  @Override
  public ThenaSqlDataSource withTenant(Tenant tenant) {
    return new ThenaSqlDataSourceImpl(tenant, tenantTableNames, pool, errorHandler, tx, tenantCache);
  }

  @Override
  public boolean isLocked(Throwable t) {
    return this.errorHandler.isLocked(t);
  }

  @Override
  public ThenaSqlDataSource withTx(ThenaSqlClient tx) {
    return new ThenaSqlDataSourceImpl(tenant, tenantTableNames, pool, errorHandler, Optional.of(tx), tenantCache);
  }

  @Override
  public boolean isTenantLoaded() {
    return isTenantLoaded;
  }
  @Override
  public TenantContext getRegistry() {
    return tenantTableNames;
  }
  @Override
  public TenantCache getTenantCache() {
    return tenantCache;
  }
}
