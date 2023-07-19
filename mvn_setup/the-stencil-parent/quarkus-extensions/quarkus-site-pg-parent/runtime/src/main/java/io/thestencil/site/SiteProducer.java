package io.thestencil.site;

/*-
 * #%L
 * quarkus-stencil-ide-services
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import java.io.IOException;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.jackson.ObjectMapperCustomizer;
import io.resys.thena.docdb.spi.pgsql.PgErrors;
import io.resys.thena.docdb.sql.DocDBFactorySql;
import io.thestencil.client.spi.StencilClientImpl;
import io.thestencil.client.spi.StencilComposerImpl;
import io.thestencil.client.spi.StencilStoreImpl;
import io.thestencil.client.spi.serializers.ZoeDeserializer;
import io.thestencil.site.handlers.SiteHandlerContext;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.pgclient.PgPool;

@ApplicationScoped
public class SiteProducer {

  private RuntimeConfig runtimeConfig;
  private String servicePath;

  public SiteProducer setRuntimeConfig(RuntimeConfig runtimeConfig) {
    this.runtimeConfig = runtimeConfig;
    return this;
  }

  @Singleton
  public static class RegisterGuava implements ObjectMapperCustomizer {
    @Override
    public void customize(ObjectMapper objectMapper) {
      objectMapper.registerModule(new GuavaModule());
      objectMapper.registerModule(new Jdk8Module());
      objectMapper.registerModule(new JavaTimeModule());
    }
  }

  @Produces
  @ApplicationScoped
  public RegisterGuava registerGuava() {
    return new RegisterGuava();
  }
  
  @Produces
  @ApplicationScoped
  public SiteHandlerContext stencilIdeServicesContext(Vertx vertx, ObjectMapper objectMapper, PgPool pgPool) {
    
    
    final var docDb = DocDBFactorySql.create().client(pgPool).errorHandler(new PgErrors()).build();
    final var deserializer = new ZoeDeserializer(objectMapper);
    final var store = StencilStoreImpl.builder()
        .config((builder) -> builder
            .client(docDb)
            .objectMapper(objectMapper)
            .repoName(runtimeConfig.repo.repoName)
            .headName(runtimeConfig.repo.headName)
            .deserializer(deserializer)
            .serializer((entity) -> {
              try {
                return objectMapper.writeValueAsString(entity);
              } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
              }
            })
            .gidProvider(type -> UUID.randomUUID().toString())
            .authorProvider(() -> "no-author"))
        .build();
    
    final var client = new StencilClientImpl(store);
    final var composer = new StencilComposerImpl(client);

    // create repo if not present
    return new SiteHandlerContext(composer, client, objectMapper, servicePath);
  }
  
  public static String cleanPath(String value) {
    final var start = value.startsWith("/") ? value.substring(1) : value;
    return start.endsWith("/") ? value.substring(0, start.length() -2) : start;
  }

  public SiteProducer setServicePath(String servicePath) {
    this.servicePath = servicePath;
    return this;
  }
}
