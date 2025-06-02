package io.resys.thena.spi;

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

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.registry.TenantRegistry;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlSchemaFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.resys.thena.registry.TenantRegistrySqlImpl;
import io.resys.thena.spi.TenantDataSource.InternalTenantQuery;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;



@Slf4j(topic = LogConstants.SHOW_SQL)
public abstract class InternalTenantQueryImpl implements InternalTenantQuery {
  protected final ThenaSqlDataSource dataSource;
  protected final TenantRegistry registry;
  
  public InternalTenantQueryImpl(ThenaSqlDataSource dataSource) {
    super();
    this.dataSource = dataSource;
    this.registry = new TenantRegistrySqlImpl(dataSource.getRegistry());
  }

  public ThenaSqlClient getClient() {
    return dataSource.getClient();
  }

  @Override
  public Uni<Tenant> getByName(String name) {
    final var cached = this.dataSource.getTenantCache().getTenant(name);
    if(cached.isPresent()) {
      return Uni.createFrom().item(cached.get());
    }
    
    final var sql = registry.getByName(name);
    if(log.isDebugEnabled()) {
      log.debug("Repo by name query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
        .mapping(registry.defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Tenant> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> {
          
          
          dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't find 'REPOS' by 'name'!", sql, e));
        });
  }

  @Override
  public Uni<Tenant> getByNameOrId(String nameOrId) {
    final var cached = this.dataSource.getTenantCache().getTenant(nameOrId);
    if(cached.isPresent()) {
      return Uni.createFrom().item(cached.get());
    }
    final var sql = registry.getByNameOrId(nameOrId);
    
    if(log.isDebugEnabled()) {
      log.debug("Repo by nameOrId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
        .mapping(registry.defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Tenant> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onItem().invoke(tenant -> this.dataSource.getTenantCache().setTenant(tenant))
        .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't find 'REPOS' by 'name' or 'id'!", sql, e)));
  }
  
  @Override
  public Multi<Tenant> findAll() {
    final var sql = this.registry.findAll();
    if(log.isDebugEnabled()) {
      log.debug("Fina all tenants query, with props: {} \r\n{}", 
          "", 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
      .mapping(registry.defaultMapper())
      .execute()
      .onItem()
      .transformToMulti((RowSet<Tenant> rowset) -> Multi.createFrom().iterable(rowset))
      .onItem().invoke(newTenant -> this.dataSource.getTenantCache().setTenant(newTenant))
      .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithCompletion()
      .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlFailed("Can't find 'REPOS'!", sql, e)));
  }
  
  

  @Override
  public Uni<Void> delete() {
    final var tenantDelete = registry.dropTable();
    final var pool = dataSource.getPool();
    return  pool.query(tenantDelete.getValue()).execute()
        .onItem().transformToUni(rowSet -> {
          this.dataSource.getTenantCache().invalidateAll();
          return Uni.createFrom().voidItem();
        })
        .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlSchemaFailed("Can't drop tenant table!", tenantDelete.getValue(), e)));
    
  }
}
