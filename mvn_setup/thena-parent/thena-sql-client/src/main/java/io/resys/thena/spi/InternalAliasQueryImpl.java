package io.resys.thena.spi;

import java.util.Optional;

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
import io.resys.thena.api.entities.Alias;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.resys.thena.spi.TenantDataSource.InternalAliasQuery;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;



@Slf4j(topic = LogConstants.SHOW_SQL)
public class InternalAliasQueryImpl implements InternalAliasQuery {
  protected final ThenaSqlDataSource dataSource;
  protected final AliasRegistry registry;
  
  public InternalAliasQueryImpl(ThenaSqlDataSource dataSource) {
    super();
    this.dataSource = dataSource;
    this.registry = new AliasRegistrySqlImpl(dataSource.getRegistry());
  }

  public ThenaSqlClient getClient() {
    return dataSource.getClient();
  }
  

  @Override
  public Uni<Alias> getByName(String name) {
    final var cached = this.dataSource.getTenantCache().getAlias(name);
    if(cached.isPresent()) {
      return Uni.createFrom().item(cached.get());
    }
    
    final var sql = registry.getByName(name);
    if(log.isDebugEnabled()) {
      log.debug("Alias by name query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
        .mapping(registry.defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Alias> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> {
          dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't find 'alias' by 'name'!", sql, e));
        });
  }

  @Override
  public Uni<Alias> getByNameOrId(String nameOrId) {
    final var cached = this.dataSource.getTenantCache().getAlias(nameOrId);
    if(cached.isPresent()) {
      return Uni.createFrom().item(cached.get());
    }
    final var sql = registry.getByNameOrId(nameOrId);
    
    if(log.isDebugEnabled()) {
      log.debug("Get alias by nameOrId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
        .mapping(registry.defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Alias> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onItem().invoke(tenant -> this.dataSource.getTenantCache().setAlias(tenant))
        .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't find 'alias' by 'name' or 'id'!", sql, e)));
  }
  
  @Override
  public Uni<Optional<Alias>> findByNameOrId(String nameOrId) {
    final var cached = this.dataSource.getTenantCache().getAlias(nameOrId);
    if(cached.isPresent()) {
      return Uni.createFrom().item(Optional.of(cached.get()));
    }
    final var sql = registry.getByNameOrId(nameOrId);
    
    if(log.isDebugEnabled()) {
      log.debug("Find alias by nameOrId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
        .mapping(registry.defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Alias> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return Optional.of(it.next());
          }
          return Optional.<Alias>empty();
        })
        .onItem().invoke(tenant -> {
          if(tenant.isPresent()) {
            this.dataSource.getTenantCache().setAlias(tenant.get());  
          }
        })
        .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithItem(Optional.empty())
        .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't find 'alias' by 'name' or 'id'!", sql, e)));
  }
  
  
  @Override
  public Multi<Alias> findAll() {
    final var sql = this.registry.findAll();
    if(log.isDebugEnabled()) {
      log.debug("Find all aliases query, with props: {} \r\n{}", 
          "", 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
      .mapping(registry.defaultMapper())
      .execute()
      .onItem()
      .transformToMulti((RowSet<Alias> rowset) -> Multi.createFrom().iterable(rowset))
      .onItem().invoke(newTenant -> this.dataSource.getTenantCache().setAlias(newTenant))
      .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithCompletion()
      .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlFailed("Can't find 'alias'!", sql, e)));
  }

  @Override
  public Uni<Alias> delete(Alias existingalias) {
    final var sql = this.registry.deleteOne(existingalias);
    if(log.isDebugEnabled()) {
      log.debug("Delete alias query, with props: {} \r\n{}", 
          sql.getPropsDeepString(), 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
      .mapping(registry.defaultMapper())
      .execute(sql.getProps())
      .onItem()
      .transform((RowSet<Alias> rowset) -> {
        final var it = rowset.iterator();
        if(it.hasNext()) {
          return it.next();
        }
        return null;
      })
      .onItem().invoke(newTenant -> this.dataSource.getTenantCache().setAlias(newTenant))
      .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithNull()
      .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't delete 'alias'!", sql, e)));
  }

  @Override
  public Uni<Alias> insert(Alias newalias) {
    final var sql = this.registry.insertOne(newalias);
    if(log.isDebugEnabled()) {
      log.debug("Insert alias query, with props: {} \r\n{}", 
          sql.getPropsDeepString(), 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
      .mapping(registry.defaultMapper())
      .execute(sql.getProps())
      .onItem()
      .transform((RowSet<Alias> rowset) -> {
        final var it = rowset.iterator();
        if(it.hasNext()) {
          return it.next();
        }
        return newalias;
      })
      .onItem().invoke(e -> this.dataSource.getTenantCache().setAlias(e))
      .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithNull()
      .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't insert 'alias'!", sql, e)));
  }

  @Override
  public Uni<Alias> merge(Alias existingalias) {
    final var sql = this.registry.updateOne(existingalias);
    if(log.isDebugEnabled()) {
      log.debug("Update alias query, with props: {} \r\n{}", 
          sql.getPropsDeepString(), 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
      .mapping(registry.defaultMapper())
      .execute(sql.getProps())
      .onItem()
      .transform((RowSet<Alias> rowset) -> {
        final var it = rowset.iterator();
        if(it.hasNext()) {
          return it.next();
        }
        return null;
      })
      .onItem().invoke(newTenant -> this.dataSource.getTenantCache().setAlias(newTenant))
      .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithNull()
      .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't update 'alias'!", sql, e)));
  }
}
