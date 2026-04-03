package io.resys.thena.datasource;

/*-
 * #%L
 * thena-db-client
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

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.resys.thena.api.entities.Alias;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.ThenaSqlDataSource.TenantCache;
import io.resys.thena.support.TableUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantCacheImpl implements TenantCache {

  private final Cache<String, Tenant> tenants_by_id;
  private final Cache<UUID, Alias> alias_by_id;
  
  public TenantCacheImpl() {
    tenants_by_id = Caffeine.newBuilder()
        .maximumSize(50)
        .expireAfterWrite(Duration.ofDays(1))
        .build();
    alias_by_id = Caffeine.newBuilder()
        .maximumSize(50)
        .expireAfterWrite(Duration.ofDays(1))
        .build();
    log.info("Tenant cache created with caffeine, expire after writing: 1 day");
  }
  
  public TenantCacheImpl(Cache<String, Tenant> tenants_by_id, Cache<UUID, Alias> alias_by_id) {
    this.tenants_by_id = tenants_by_id;
    this.alias_by_id = alias_by_id;
    log.info("Tenant cache created with caffeine, with user configuration");
  }

  @Override
  public Optional<Tenant> getTenant(String idOrName) {
    final var result = tenants_by_id.getIfPresent(idOrName);
  
    if(result != null) {
      log.debug("Tenant by id: {} found from cache", idOrName);
      return Optional.of(result);
    }
    
    final var found = tenants_by_id.asMap().values().stream().filter(entry -> {
      if(entry.getId().equals(idOrName)) {
        log.debug("Tenant by id: {} found from cache", idOrName);
        return true;
      }
      if(entry.getExternalId() != null && entry.getExternalId().equals(idOrName)) {
        log.debug("Tenant by external id: {} found from cache", idOrName);
        return true;
      }
      
      if(entry.getName().equals(idOrName)) {
        log.debug("Tenant by name: {} found from cache", idOrName);
        return true;
      }
      
      return false;
    }).findFirst();
    
    if(found.isEmpty()) {
      log.debug("Tenant '{}' not found from cache", idOrName);
    }
    return found;

  }

  @Override
  public void setTenant(Tenant tenant) {
    if(tenant == null) {
      log.warn("Null tenant passed to cache, ignoring it...");
    } else {
      this.tenants_by_id.put(tenant.getId(), tenant);
    }
    
  }

  @Override
  public void invalidateAll() {
    tenants_by_id.invalidateAll();
  }

  @Override
  public Optional<Alias> getAlias(String id) {
    return Optional.ofNullable(this.alias_by_id.getIfPresent(TableUtils.toUuid(id)));
  }

  @Override
  public void setAlias(Alias alias) {
    if(alias == null) {
      log.warn("Null alias passed to cache, ignoring it...");
    } else {
      this.alias_by_id.put(alias.getId(), alias);
    }
    
  }
}
