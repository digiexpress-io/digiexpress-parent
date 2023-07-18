package io.resys.hdes.quarkus.composer.pg;

/*-
 * #%L
 * quarkus-composer-pg
 * %%
 * Copyright (C) 2020 - 2022 Copyright 2020 ReSys OÜ
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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

import io.quarkus.jackson.ObjectMapperCustomizer;
import io.resys.hdes.client.api.HdesStore.HdesCredsSupplier;
import io.resys.hdes.client.api.ImmutableHdesCreds;
import io.resys.hdes.client.spi.HdesClientImpl;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.resys.hdes.client.spi.ThenaStore;
import io.resys.hdes.client.spi.config.HdesClientConfig.DependencyInjectionContext;
import io.resys.hdes.client.spi.config.HdesClientConfig.ServiceInit;
import io.resys.hdes.client.spi.flow.validators.IdValidator;
import io.resys.hdes.client.spi.util.HdesAssert;
import io.resys.hdes.client.spi.web.HdesWebConfig;
import io.resys.hdes.client.spi.web.HdesWebContext;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.pgclient.PgPool;

@ApplicationScoped
public class ComposerBeansProducer {

  private ComposerRuntimeConfig runtimeConfig;
  private HdesWebConfig hdesWebConfig; // context root for the rest of services
  public ComposerBeansProducer setRuntimeConfig(ComposerRuntimeConfig runtimeConfig) {
    this.runtimeConfig = runtimeConfig;
    return this;
  }

  @Singleton
  public static class RegisterGuava implements ObjectMapperCustomizer {
    @Override
    public void customize(ObjectMapper objectMapper) {
      //new GuavaModule(), new JavaTimeModule(), new Jdk8Module();
      objectMapper.registerModule(new GuavaModule());
    }
  }

  @Produces
  @ApplicationScoped
  public RegisterGuava registerGuava() {
    return new RegisterGuava();
  }
  
  @Produces
  @ApplicationScoped
  public HdesWebContext hdesWebContext(
      Vertx vertx, ObjectMapper objectMapper, PgPool pgPool) {
    
    HdesAssert.notNull(hdesWebConfig, () -> "hdesWebConfig must be defined!");
    final ServiceInit init = new ServiceInit() {
      @SuppressWarnings("unchecked")
      @Override
      public <T> T get(Class<T> type) {
        try {
          return (T) type.getDeclaredConstructors()[0].newInstance();
        } catch (Exception e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      }
    };

    HdesCredsSupplier creds;
    final var cdiCreds = CDI.current().select(HdesCredsSupplier.class);
    if(cdiCreds.isUnsatisfied()) {
      creds = () -> ImmutableHdesCreds.builder().user("assetManager").email("assetManager@resys.io").build();  
    } else {
      creds = cdiCreds.get();
    }
    
    final var store = ThenaStore.builder()
        .objectMapper(objectMapper)
        .pgPool(pgPool)
        .repoName(runtimeConfig.repo.repoName)
        .headName(runtimeConfig.repo.headName)
        .authorProvider(() -> creds.get().getUser())
        .objectMapper(objectMapper)
        .build();
    
    final var client = HdesClientImpl.builder()
        .store(store)
        .objectMapper(objectMapper)
        .serviceInit(init)
        .dependencyInjectionContext(new DependencyInjectionContext() {
          @Override
          public <T> T get(Class<T> type) {
            return CDI.current().select(type).get();
          }
        })
        .flowVisitors(new IdValidator())
        .build();
    
    final var composer = new HdesComposerImpl(client);
    
    // create repo if not present
    return new HdesWebContext(composer, client, hdesWebConfig);
  }
  
  public ComposerBeansProducer setHdesWebConfig(HdesWebConfig hdesWebConfig) {
    this.hdesWebConfig = hdesWebConfig;
    return this;
  }

  public static String cleanPath(String value) {
    final var start = value.startsWith("/") ? value.substring(1) : value;
    return start.endsWith("/") ? value.substring(0, start.length() -2) : start;
  }
}
