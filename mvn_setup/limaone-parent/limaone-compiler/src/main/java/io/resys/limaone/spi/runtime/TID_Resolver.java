package io.resys.limaone.spi.runtime;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.resys.limaone.program.Runtime.EnvironmentProperties;
import io.resys.thena.api.actions.TenantActions.TenantAware;
import io.resys.thena.api.actions.TenantActions.TenantDb;
import io.resys.thena.api.entities.Alias;
import io.resys.thena.api.entities.Member;
import io.resys.thena.support.TableUtils;
import io.smallrye.mutiny.Uni;


public class TID_Resolver implements TenantDb {

  private final EnvironmentProperties envir;
  @SuppressWarnings("rawtypes")
  private final TenantAware client;

  private final Cache<String, Alias> aliasCache;
  private final Cache<String, Member> memberCache;
  
  @SuppressWarnings("rawtypes")
  public TID_Resolver(EnvironmentProperties envir, TenantAware client) {
    super();
    this.client = client;
    this.envir = envir;
    this.aliasCache = Caffeine.newBuilder()
      .maximumSize(10)
      .expireAfterWrite(8, TimeUnit.HOURS)
      .build();
    
    this.memberCache = Caffeine.newBuilder()
      .maximumSize(10)
      .expireAfterWrite(1, TimeUnit.MINUTES)
      .build();
  }


  @Override
  public String getTenantByAnything(String id) {
    final var isTenantDefined = id != null && !id.isBlank();

    // resolve by alias
    if(isTenantDefined) {
      return findByAlias(id).onItem().transform(found -> {
        if(found.isPresent()) {
          return found.get().getAliasTenantId();
        }
        return envir.getDefaultTenantName();
      })
      .runSubscriptionOn(envir.getWorkerPool())
      .await().atMost(envir.getWorkerPoolMaxTimeout());
    }
    
    // resolve by user or fallback to default
    final var user = envir.getCurrentUser().get();
    final var isUserDefined = !user.getUserName().isBlank();    
    if(isUserDefined) {
      return findByMember(envir, user.getUserName()).onItem().transform(found -> {
        if(found.isPresent()) {
          return found.get().getAliasTenantId();
        }
        return envir.getDefaultTenantName();
      })
      .runSubscriptionOn(envir.getWorkerPool())
      .await().atMost(envir.getWorkerPoolMaxTimeout());
    }

    return envir.getDefaultTenantName();
  }
  
  private Uni<Optional<Alias>> findByMember(EnvironmentProperties envir, String userName) {
    final var cached = memberCache.getIfPresent(userName);
    if(cached != null) {
      return findByAlias(cached.getAliasId().toString());
    }
    
    
    return client.getActions().queryMembers()
      .externalId(userName)
      .refTenant(envir.getDefaultTenantName())
      .findAll().collect().asList()
      .onItem().transformToUni(member -> {
        if(member.isEmpty() || !member.getFirst().getAliasStatus()) {
          return Uni.createFrom().item(Optional.empty());
        }
        memberCache.put(userName, member.getFirst());
        return findByAlias(member.getFirst().getAliasId().toString());
      }); 
  }
  
  
  private Uni<Optional<Alias>> findByAlias(String id) {
    final var alias = aliasCache.getIfPresent(id);
    if(alias != null) {
      return Uni.createFrom().item(Optional.ofNullable(alias));
    }
    
    UUID uuid = null;
    try {
      uuid = TableUtils.toUuid(id);
      final var byId = aliasCache.getIfPresent(uuid.toString());
      if(byId != null) {
        return Uni.createFrom().item(Optional.ofNullable(byId));
      }
    } catch(Exception e) {
      // ignore
    }
    
    if(uuid == null) {
      return client.getActions().queryAliases().findAll()
          .onItem().invoke(toCache -> {
            aliasCache.put(toCache.getAliasName(), toCache);
            aliasCache.put(toCache.getId().toString(), toCache);
          })
          .collect().asList()
          .onItem().transform(values -> values.stream()
              .filter(e -> e.getAliasName().equals(id))
              .findAny());
    }
    
    return client.getActions().queryAliases().id(uuid).getOne()
        .onItem().invoke(toCache -> {
          aliasCache.put(toCache.getAliasName(), toCache);
          aliasCache.put(toCache.getId().toString(), toCache);
        })
        .onItem().transform(Optional::of);
  }


  @Override
  public String getCurrentUserTenant() {
    final var user = envir.getCurrentUser().get();
    final var isUserDefined = !user.getUserName().isBlank();
    if(isUserDefined) {
      final Optional<Alias> alias = client.getActions().queryMembers()
        .externalId(user.getUserName())
        .refTenant(envir.getDefaultTenantName())
        .findAll().collect().asList()
        .onItem().transformToUni(member -> {
          if(member.isEmpty() || !member.getFirst().getAliasStatus()) {
            return Uni.createFrom().item(Optional.<Alias>empty());
          }
          memberCache.put(user.getUserName(), member.getFirst());
          return findByAlias(member.getFirst().getAliasId().toString());
        })
        .runSubscriptionOn(envir.getWorkerPool())
        .await().atMost(envir.getWorkerPoolMaxTimeout());
     
      if(alias.isEmpty()) {
        return null;
      }
      return alias.get().getAliasName();
    }
    return null;
  }

}
