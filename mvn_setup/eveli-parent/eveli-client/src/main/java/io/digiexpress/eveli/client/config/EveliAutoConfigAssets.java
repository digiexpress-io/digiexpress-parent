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
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.eveli.client.web.resources.assets.AssetsAnyTagController;
import io.digiexpress.eveli.client.web.resources.assets.AssetsDeploymentController;
import io.digiexpress.eveli.client.web.resources.assets.AssetsDialobController;
import io.digiexpress.eveli.client.web.resources.assets.AssetsMigrationController;
import io.digiexpress.eveli.client.web.resources.assets.AssetsPublicationController;
import io.digiexpress.eveli.client.web.resources.assets.AssetsStencilController;
import io.digiexpress.eveli.client.web.resources.assets.AssetsTagomiController;
import io.digiexpress.eveli.client.web.resources.assets.AssetsWorkflowController;
import io.digiexpress.eveli.client.web.resources.assets.AssetsWrenchController;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.envir.api.ExternalDeploymentProvider;
import io.digiexpress.eveli.envir.spi.ExternalDeploymentProviderDevEnvir;
import io.digiexpress.tagomi.api.ImmutableImage;
import io.digiexpress.tagomi.api.ImmutableImageEnvlope;
import io.digiexpress.tagomi.api.ImmutableTagomiStoreConfig;
import io.digiexpress.tagomi.api.TagomiClient;
import io.digiexpress.tagomi.api.TagomiImageStorage;
import io.digiexpress.tagomi.api.TagomiStoreConfig;
import io.digiexpress.tagomi.api.entities.TagomiContainer.Service;
import io.digiexpress.tagomi.spi.TagomiClientImpl;
import io.digiexpress.tagomi.spi.TagomiComposerImpl;
import io.digiexpress.tagomi.spi.TagomiStoreImpl;
import io.digiexpress.tagomi.spi.json.FromJsonObject;
import io.digiexpress.tagomi.spi.json.ToJsonObject;
import io.digiexpress.thena.cockpit.client.api.CockpitAware.CockpitAwareProvider;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.spi.HdesClientImpl;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.resys.hdes.client.spi.config.HdesClientConfig.DependencyInjectionContext;
import io.resys.hdes.client.spi.config.HdesClientConfig.ServiceInit;
import io.resys.hdes.client.spi.flow.validators.IdValidator;
import io.resys.hdes.client.spi.store.ThenaStore;
import io.resys.thena.api.ThenaAware;
import io.resys.thena.git.spi.GitDataSourceImpl;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.spi.StencilClientImpl;
import io.thestencil.client.spi.StencilComposerImpl;
import io.thestencil.client.spi.StencilStoreImpl;
import io.thestencil.client.spi.serializers.ZoeDeserializer;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@ConditionalOnBean(name = EveliAutoConfigAssets.BEAN_NAME)
@Configuration
@Slf4j
public class EveliAutoConfigAssets {
  public static final String BEAN_NAME = "eveliEditEnvir";

  @Getter
  @RequiredArgsConstructor
  public static class EveliEditEnvir {  
    private final StencilClient stencil;
    private final HdesClient wrench;
    private final EveliPropsAssets assetProps;
    private final TagomiStoreConfig tagomi;
  }

  @Bean
  public AssetsTagomiController assetsTagomiController(
      EveliEditEnvir context, 
      WorkerAuthClient security, 
      DialobClient dialobClient,
      EveliEnvirClient envir,
      EveliPropsTagomi tagomiProps,
      RestTemplate restTemplate,
      ObjectMapper objectMapper
  ) {
    final var datasource = new TagomiClient.WorldDatasource() {
      @Override
      public Uni<JsonObject> get(Service service, JsonObject props) {
        if(StringUtils.isEmpty(service.getOrchestratorName())) {
          return Uni.createFrom().item(new JsonObject());
        }
        return envir.runtimeQuery().getOne()
            .onItem().transform(runtime -> runtime.getWrench()
                .inputJson(props)
                .flow(service.getOrchestratorName())
                .andGetBody())
            .onItem().transform(result -> new JsonObject(new HashMap<>(result.getReturns())));
      }
    }; 
    
    final var tagomiClient = new TagomiClientImpl(objectMapper, datasource, restTemplate, tagomiProps.getServiceUrl());
    return new AssetsTagomiController(
        tagomiClient,
        new TagomiComposerImpl(new TagomiStoreImpl(context.getTagomi()), context.getTagomi().getImageStorage()), 
        envir);
  }
  @Bean
  public AssetsAnyTagController assetsAnyTagController(
      EveliEditEnvir context, 
      WorkerAuthClient security, 
      DialobClient dialobClient,
      EveliEnvirClient envir
  ) {

    return new AssetsAnyTagController(context.getStencil(), context.getWrench());
  }
  @Bean
  public AssetsDeploymentController assetsDeploymentController(
      ApplicationEventPublisher publisher,
      EveliEditEnvir context, 
      WorkerAuthClient auth, 
      DialobClient dialobClient,
      EveliEnvirClient envir) {
    
    return new AssetsDeploymentController(auth, envir, publisher);
  }
  @Bean 
  public AssetsPublicationController assetReleaseController(
      EveliEditEnvir context, 
      WorkerAuthClient security,
      DialobClient dialobClient,
      EveliEnvirClient envir,
      ApplicationEventPublisher publisher
  ) {
    return new AssetsPublicationController(envir, context.getStencil(), context.getWrench(), dialobClient, security, publisher);
  }
  
  @Bean 
  public AssetsMigrationController assetsMigrationController(
      EveliEditEnvir context, 
      WorkerAuthClient security,
      DialobClient dialobClient,
      EveliEnvirClient envir,
      ApplicationEventPublisher publisher
  ) {
    return new AssetsMigrationController(context.getStencil(), context.getWrench(), dialobClient);
  }
  
  @Bean
  public AssetsDialobController assetsDialobController(DialobClient client) {
    return new AssetsDialobController(client);
  }
  @Bean 
  public AssetsWorkflowController workflowController(
      EveliEditEnvir context,
      DialobClient dialobClient,
      EveliEnvirClient client
  ) {
    return new AssetsWorkflowController(context.getStencil(), dialobClient, client);
  }
  @Bean
  public AssetsWrenchController wrenchComposerController(EveliEditEnvir context, EveliEnvirClient client, ObjectMapper objectMapper) {
    return new AssetsWrenchController(objectMapper, new HdesComposerImpl(context.getWrench()), client);
  }
  @Bean
  public AssetsStencilController assetsStencilController(EveliEditEnvir context, ObjectMapper objectMapper, EveliEnvirClient client) {
    return new AssetsStencilController(objectMapper, new StencilComposerImpl(context.getStencil()), client);
  }

  @Bean
  public ExternalDeploymentProvider externalDeploymentProvider(EveliEditEnvir context, DialobClient dialob) {
    return new ExternalDeploymentProviderDevEnvir(context.getStencil(), context.getWrench(), dialob);
  }
  
  /**
   * Create this bean for edit envir
   */
  public static EveliEditEnvir eveliEditEnvir(
      EveliProps eveliProps, 
      EveliPropsAssets assetProps,
      ObjectMapper objectMapper,
      ApplicationContext context,
      io.vertx.mutiny.sqlclient.Pool pgPool
    ) {
    
    final var cockpitProvider = Optional.ofNullable(context.getBeanProvider(CockpitAwareProvider.class).getIfAvailable());
    
    final var wrenchClient = HdesClientImpl.builder()
        .cockpitAwareProvider(cockpitProvider)
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
            .client(GitDataSourceImpl.create().client(pgPool).build())
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
        ).build(), cockpitProvider);
    
    final var tagomi = ImmutableTagomiStoreConfig.builder()
        .client(GitDataSourceImpl.create().client(pgPool).build())
        .tenantName("tagomi-assets")
        .headName("main")
        .deserializer(new FromJsonObject())
        .serializer(new ToJsonObject())
        .authorProvider(() -> "junit-test")
        .imageStorage(new TagomiImageStorage() {
          @Override
          public Uni<ImageEnvlope> write(byte[] body) {
            return Uni.createFrom()
                .item(ImmutableImageEnvlope.builder()
                .operationStatus(OperationStatus.OK)
                .object(ImmutableImage.builder().id("1234")
                    .body(body)
                    .build())
                .build());
          }
          @Override
          public Uni<ImageEnvlope> read(String id) {
            return Uni.createFrom().item(ImmutableImageEnvlope
                .builder()
                .operationStatus(OperationStatus.OK)
                .object(ImmutableImage.builder().id("1234")
                    .body(new byte[] {})
                    .build())
                .build());
          }
        })
        .build();
    
    final var dev = new EveliEditEnvir(stencilClient, wrenchClient, assetProps, tagomi);
    
    context.getBean(ThenaAware.class).register(dev.getClass(), getOrCreateDb(dev));
    return dev;
  }
  
  /**
   * Call this for get/create wrench/stencil db-s only needed for edit envir
   */
  public static Uni<EveliEditEnvir> getOrCreateDb(EveliEditEnvir envir) {
    final var createdWrench = envir.getWrench().repo().create();
        /*
         * Creates default wrench assets
         * .onItem().transformToUni(init -> 
          new HdesDefaultAssets(init, Boolean.TRUE.equals(envir.getAssetProps().getOverwrite())).accept()
          .onItem().transform(junk -> init)
        );
        */
    final var createdStencil = envir.getStencil().repo().create();
    final var createTagomi = new TagomiStoreImpl(envir.getTagomi()).tenantBuilder().createIfNot();
    return Uni.combine().all()
        .unis(createdWrench, createdStencil, createTagomi)
        .asTuple().onItem().transform(e -> envir);
  }
}