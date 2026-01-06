/*-
 * #%L
 * eveli-client
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

package io.digiexpress.eveli.client.config;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.eveli.client.spi.cockpit.CockpitAwareProviderImpl;
import io.digiexpress.eveli.userprofile.client.api.UserProfileClient;
import io.digiexpress.thena.cockpit.client.api.CockpitAware;
import io.digiexpress.thena.cockpit.client.api.CockpitAware.CockpitAwareProvider;
import io.digiexpress.thena.cockpit.client.api.CockpitAware.CockpitContainerCache;
import io.digiexpress.thena.cockpit.client.api.CockpitClient;
import io.digiexpress.thena.cockpit.client.api.CockpitContainer;
import io.digiexpress.thena.cockpit.client.spi.CockpitClientImpl;
import io.digiexpress.thena.cockpit.client.spi.CockpitContainerCacheImpl;
import io.resys.thena.storesql.PgErrors;
import lombok.extern.slf4j.Slf4j;


@ConditionalOnBooleanProperty(matchIfMissing = false, havingValue = true, prefix = EveliPropsCockpit.PREFIX, name = "enabled")
@ConditionalOnBean(name = EveliAutoConfigAssets.BEAN_NAME)
@Configuration
@Slf4j
public class EveliAutoConfigCockpit {

  @Bean
  public CockpitAwareProvider cockpitAwareProvider(
      CockpitClient client,
      WorkerAuthClient auth, 
      UserProfileClient userProfileClient, 
      Optional<CockpitContainerCache> cache) {

    final var cacheImpl = cache.orElseGet(EveliAutoConfigCockpit::cockpitContainerCache);
    return new CockpitAwareProviderImpl(client, auth, userProfileClient, cacheImpl);
  }
  
  @Bean
  public CockpitClient cockpitClient(List<CockpitAware<?>> aware, io.vertx.mutiny.sqlclient.Pool pgPool) {
    final var client = CockpitClientImpl.create()
        .client(pgPool)
        .errorHandler(new PgErrors())
        .build();
    client.tenants().createOneTenant().buildOnlyIfNotCreated().await().atMost(Duration.ofMinutes(5));
    return client;
  }
  
  
  public static CockpitContainerCacheImpl cockpitContainerCache() {
    final Cache<String, CockpitContainer> long_runtime_cache = Caffeine.newBuilder()
        .maximumSize(5)
        .expireAfterWrite(Duration.ofMinutes(15))
        .build();      
    return new CockpitContainerCacheImpl(long_runtime_cache);
  }
}