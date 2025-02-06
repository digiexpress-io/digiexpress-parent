package io.digiexpress.eveli.client.config;

/*-
 * #%L
 * eveli-client
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

import java.io.IOException;
import java.util.UUID;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.client.spi.assets.HdesDefaultAssets;
import io.digiexpress.eveli.client.web.resources.assets.AssetsAnyTagController;
import io.digiexpress.eveli.client.web.resources.assets.AssetsDeploymentController;
import io.digiexpress.eveli.client.web.resources.assets.AssetsDialobController;
import io.digiexpress.eveli.client.web.resources.assets.AssetsPublicationController;
import io.digiexpress.eveli.client.web.resources.assets.AssetsStencilController;
import io.digiexpress.eveli.client.web.resources.assets.AssetsWorkflowController;
import io.digiexpress.eveli.client.web.resources.assets.AssetsWrenchController;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.resys.hdes.client.spi.HdesClientImpl;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.resys.hdes.client.spi.config.HdesClientConfig.DependencyInjectionContext;
import io.resys.hdes.client.spi.config.HdesClientConfig.ServiceInit;
import io.resys.hdes.client.spi.flow.validators.IdValidator;
import io.resys.hdes.client.spi.store.ThenaStore;
import io.resys.thena.storesql.DbStateSqlImpl;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.spi.StencilClientImpl;
import io.thestencil.client.spi.StencilComposerImpl;
import io.thestencil.client.spi.StencilStoreImpl;
import io.thestencil.client.spi.serializers.ZoeDeserializer;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;



@DependsOn(EveliEditEnvir.BEAN_NAME)
@Configuration
@Slf4j
public class EveliAutoConfigAssets {
  
  // TODO @Value("${app.version}")
  private String version = "alpha";

  // TODO  @Value("${build.timestamp}")
  private String timestamp = "";

  @Bean
  public AssetsAnyTagController assetsAnyTagController(
      EveliEditEnvir context, 
      AuthClient security, 
      DialobClient dialobClient,
      EveliEnvirClient envir
  ) {

    return new AssetsAnyTagController(context.getStencil(), context.getWrench());
  }
  @Bean
  public AssetsDeploymentController assetsDeploymentController(
      EveliEditEnvir context, 
      AuthClient auth, 
      DialobClient dialobClient,
      EveliEnvirClient envir) {
    
    return new AssetsDeploymentController(composer);
  }
  @Bean 
  public AssetsPublicationController assetReleaseController(
      EveliEditEnvir context, 
      AuthClient security,
      DialobClient dialobClient,
      EveliEnvirClient envir
  ) {
    return new AssetsPublicationController();
  }
  @Bean
  public AssetsDialobController assetsDialobController(DialobClient client, ObjectMapper objectMapper) {
    return new AssetsDialobController(client, objectMapper);
  }
  @Bean 
  public AssetsWorkflowController workflowController(
      EveliEditEnvir context,
      DialobClient dialobClient
  ) {
    return new AssetsWorkflowController(context.getStencil(), dialobClient);
  }
  @Bean
  public AssetsWrenchController wrenchComposerController(EveliEditEnvir context, EveliEnvirClient client, ObjectMapper objectMapper) {
    return new AssetsWrenchController(new HdesComposerImpl(context.getWrench()), objectMapper, client, version, timestamp);
  }
  @Bean
  public AssetsStencilController assetsStencilController(EveliEditEnvir context, ObjectMapper objectMapper) {
    return new AssetsStencilController(new StencilComposerImpl(context.getStencil()), objectMapper);
  }

  
  
  public static EveliEditEnvir eveliEditEnvir(
      EveliProps eveliProps, 
      EveliPropsAssets assetProps,
      ObjectMapper objectMapper,
      ApplicationContext context,
      io.vertx.mutiny.pgclient.PgPool pgPool
    ) {
    
    final var wrenchClient = HdesClientImpl.builder()
        .store(ThenaStore.builder()
            .pgPool(pgPool)
            .repoName("wrench-assets")
            .headName("main")
            .authorProvider(() -> "eveli")
            .objectMapper(objectMapper)
            .build())
        .objectMapper(objectMapper)
        .serviceInit(new ServiceInit() { @Override public <T> T get(Class<T> type) { return context.getAutowireCapableBeanFactory().createBean(type); } })
        .dependencyInjectionContext( new DependencyInjectionContext() { @Override public <T> T get(Class<T> type) { return context.getBean(type); } })
        .flowVisitors(new IdValidator())
        .build();
    
    final var stencilClient = new StencilClientImpl(StencilStoreImpl.builder()
        .config((builder) -> builder
            .client(DbStateSqlImpl.create().client(pgPool).build())
            .objectMapper(objectMapper)
            .repoName("stencil-assets")
            .headName("main")
            .deserializer(new ZoeDeserializer(objectMapper))
            .serializer((entity) -> {
              try {
                return new JsonObject(objectMapper.writeValueAsString(entity));
              } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
              }
            })
            .gidProvider(type -> UUID.randomUUID().toString())
            .authorProvider(() -> "eveli")
        ).build());

    return new EveliEditEnvir(stencilClient, wrenchClient, assetProps);
  }
  
  public static Uni<EveliEditEnvir> getOrCreateDb(EveliEditEnvir envir) {
    final var createdWrench = envir.getWrench().repo().create()
        .onItem().transformToUni(init -> 
          new HdesDefaultAssets(init, Boolean.TRUE.equals(envir.getAssetProps().getOverwrite())).accept()
          .onItem().transform(junk -> init)
        );
    final var createdStencil = envir.getStencil().repo().create();
    return Uni.combine().all()
        .unis(createdWrench, createdStencil)
        .asTuple().onItem().transform(e -> envir);
  }
}