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
import io.resys.thena.api.registry.ThenaRegistry;
import io.resys.thena.datasource.ThenaSqlClient.ThenaSqlPool;

public class ThenaSqlDataSourceImpl implements ThenaSqlDataSource {
  private final Tenant tenant;
  private final TenantTableNames tenantTableNames;
  private final ThenaSqlPool pool;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  private final Optional<ThenaSqlClient> tx;
  private final ThenaRegistry registry;
  private final boolean isTenantLoaded;
  
  public ThenaSqlDataSourceImpl(
      Tenant tenant, 
      TenantTableNames tenantTableNames, 
      ThenaSqlPool pool,
      ThenaSqlDataSourceErrorHandler errorHandler, 
      Optional<ThenaSqlClient> tx,
      ThenaRegistry registry) {
    super();
    this.tenant = tenant;
    this.tenantTableNames = tenantTableNames.toRepo(tenant);
    this.registry = registry.withTenant(this.tenantTableNames);
    this.errorHandler = errorHandler.withOptions(this.tenantTableNames);
    this.pool = pool;
    this.tx = tx;
    this.isTenantLoaded = !tenant.getId().equals("") && !tenant.getPrefix().equals("");
  }
  
  public ThenaSqlDataSourceImpl(
      String tenant, 
      TenantTableNames tenantTableNames, 
      ThenaSqlPool pool,
      ThenaSqlDataSourceErrorHandler errorHandler, 
      Optional<ThenaSqlClient> tx, 
      ThenaRegistry registry) {
    super();
    this.isTenantLoaded = false;
    this.tenant = ImmutableTenant.builder()
        .name(tenant)
        .type(StructureType.git)
        .id("")
        .rev("")
        .prefix("")
        .build();
    this.tenantTableNames = tenantTableNames.toRepo(this.tenant);
    this.errorHandler = errorHandler.withOptions(this.tenantTableNames);
    this.registry = registry.withTenant(this.tenantTableNames);
    this.pool = pool;
    this.tx = tx;
  }
  
  @Override
  public Tenant getTenant() {
    return tenant;
  }
  @Override
  public TenantTableNames getTenantTableNames() {
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
    return new ThenaSqlDataSourceImpl(tenant, tenantTableNames, pool, errorHandler, tx, registry);
  }

  @Override
  public boolean isLocked(Throwable t) {
    return this.errorHandler.isLocked(t);
  }

  @Override
  public ThenaSqlDataSource withTx(ThenaSqlClient tx) {
    return new ThenaSqlDataSourceImpl(tenant, tenantTableNames, pool, errorHandler, Optional.of(tx), registry);
  }

  @Override
  public boolean isTenantLoaded() {
    return isTenantLoaded;
  }
  @Override
  public ThenaRegistry getRegistry() {
    return registry;
  }
}
