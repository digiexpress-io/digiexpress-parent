package io.digiexpress.eveli.client.config;

import java.time.Duration;

/*-
 * #%L
 * eveli-client
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

import java.util.Collections;
import java.util.Optional;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliRuntime;
import io.digiexpress.eveli.envir.api.ExternalDeploymentProvider;
import io.digiexpress.eveli.envir.spi.EveliEnvirClientImpl;
import io.digiexpress.eveli.envir.spi.EveliEnvirStore;
import io.digiexpress.eveli.envir.spi.EveliRuntimeCacheInMemory;
import io.digiexpress.eveli.envir.spi.actions.EveliRuntimeCache;
import io.resys.hdes.client.spi.HdesAstTypesImpl;
import io.resys.hdes.client.spi.HdesClientImpl.HdesClientConfigImpl;
import io.resys.hdes.client.spi.HdesTypeDefsFactory;
import io.resys.hdes.client.spi.cache.HdesClientEhCache;
import io.resys.hdes.client.spi.config.HdesClientConfig;
import io.resys.hdes.client.spi.config.HdesClientConfig.DependencyInjectionContext;
import io.resys.hdes.client.spi.config.HdesClientConfig.ServiceInit;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class EveliAutoConfigEnvir {
  
  @Bean
  public EveliEnvirClient eveliEnvirClient(
      io.vertx.mutiny.pgclient.PgPool pool,
      DialobClient dialobClient, 
      ObjectMapper objectMapper, 
      AuthClient authClient,
      Optional<ExternalDeploymentProvider> depProvider,
      ApplicationContext context,
      EveliPropsEnvir envirProps) {
    
    final boolean isDev = true;
    final ExternalDeploymentProvider externalProvider = depProvider.orElse(new ExternalDeploymentProvider() {
      @Override
      public Uni<Optional<EveliDeployment>> getDeployment(boolean emptyBranchBody) {
        return Uni.createFrom().item(Optional.empty());
      }
    });
    final EveliEnvirStore store = envirStore(pool, externalProvider, objectMapper, authClient);
    final EveliRuntimeCache cache = cache(envirProps);
    final var hdesClientConfig = hdesConfig(objectMapper, context);
    final var envir = new EveliEnvirClientImpl(store, hdesClientConfig, dialobClient, cache, isDev);
    
    
    store.query()
      .createIfNot()
      .await().atMost(Duration.ofMinutes(5));
    
    return envir;
  }

  private EveliRuntimeCache cache(EveliPropsEnvir envirProps) {
    final Cache<String, EveliDeployment> short_deployment_cache = Caffeine.newBuilder()
        .expireAfterWrite(envirProps.getCacheExpirations().getShortDeployment())
        .build();

    final Cache<String, EveliRuntime> long_runtime_cache = Caffeine.newBuilder()
        .maximumSize(5)
        .expireAfterWrite(envirProps.getCacheExpirations().getLongRuntime())
        .build();
    
    return new EveliRuntimeCacheInMemory(short_deployment_cache, long_runtime_cache);
  }

  private EveliEnvirStore envirStore(
      io.vertx.mutiny.pgclient.PgPool pool, 
      ExternalDeploymentProvider externalProvider, 
      ObjectMapper objectMapper, 
      AuthClient authClient) {
    
    return EveliEnvirStore
      .builder(externalProvider)
      .repoName("envir")
      .pgPool(pool)
      .objectMapper(objectMapper)
      .authorProvider(() -> {
        try {
          return authClient.getUser().getPrincipal().getUsername();
        } catch(Exception e) {
          return "system";
        }
      }).build();
  }

  private HdesClientConfig hdesConfig(ObjectMapper objectMapper, ApplicationContext context) {
    final var cache = HdesClientEhCache.builder().build("init");
    final var types = new HdesTypeDefsFactory(objectMapper);
    final var ast = new HdesAstTypesImpl(types, Collections.emptyList());
    final var serviceInit = new ServiceInit() { @Override public <T> T get(Class<T> type) { return context.getAutowireCapableBeanFactory().createBean(type); } };
    final var dependencyInjectionContext = new DependencyInjectionContext() { @Override public <T> T get(Class<T> type) { return context.getBean(type); } };
    return new HdesClientConfigImpl(cache, serviceInit, dependencyInjectionContext, types, ast);
  }
}
