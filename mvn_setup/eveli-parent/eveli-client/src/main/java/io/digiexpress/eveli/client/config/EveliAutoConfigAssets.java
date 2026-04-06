package io.digiexpress.eveli.client.config;

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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.eveli.client.web.resources.assets.AssetsDeploymentController;
import io.digiexpress.eveli.client.web.resources.assets.AssetsDialobController;
import io.digiexpress.eveli.client.web.resources.assets.AssetsPublicationController;
import io.digiexpress.eveli.client.web.resources.assets.AssetsStencilController;
import io.digiexpress.eveli.client.web.resources.assets.AssetsTagomiController;
import io.digiexpress.eveli.client.web.resources.assets.AssetsWrenchController;
import io.digiexpress.eveli.client.web.resources.worker.CockpitApiController;
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
import io.resys.limaone.authoring.Authoring;
import io.resys.limaone.persistence.AuthoringImpl;
import io.resys.limaone.persistence.ImmutableAuthoringConfig;
import io.resys.limaone.program.Runtime.EnvironmentProperties;
import io.resys.limaone.spi.compiler.CompilerImpl;
import io.resys.thena.git.spi.GitDataSourceImpl;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@ConditionalOnBean(name = EveliAutoConfigAssets.BEAN_NAME)
@Configuration
@Slf4j
public class EveliAutoConfigAssets {
  public static final String BEAN_NAME = "eveliEditEnvir";

  @Getter @RequiredArgsConstructor
  public static class EveliEditEnvir {  
    private final EnvironmentProperties envirProps;
    private final io.resys.limaone.program.Runtime runtime;
    private final Authoring authoring;
    private final EveliPropsAssets assetProps;
    private final TagomiStoreConfig tagomi;
  }

  @Bean
  @ConditionalOnBooleanProperty(value = "eveli.cockpit.enabled", havingValue = true, matchIfMissing = false)
  public CockpitApiController cockpitApiController(EveliEditEnvir envir) {
    return new CockpitApiController(envir.getAuthoring());
  }
  
  @Bean
  public AssetsTagomiController assetsTagomiController(
      EveliEditEnvir context, 
      WorkerAuthClient security, 
      EveliPropsTagomi tagomiProps,
      RestTemplate restTemplate,
      ObjectMapper objectMapper) {

    final var datasource = new TagomiClient.WorldDatasource() {
      @SuppressWarnings("unchecked")
      @Override
      public Uni<JsonObject> get(Service service, JsonObject props) {
        if(StringUtils.isEmpty(service.getOrchestratorName())) {
          return Uni.createFrom().item(new JsonObject());
        }
        final var flow = context.getRuntime().getBundle().queryFlows().name(service.getOrchestratorName()).findOne();
        final var input = props.mapTo(Map.class);
        final var result = flow.get().run(input).andGetBody();
        return Uni.createFrom().item(new JsonObject(new HashMap<>(result.getReturns())));
      }
    };
    
    final var tagomiClient = new TagomiClientImpl(objectMapper, datasource, restTemplate, tagomiProps.getServiceUrl());
    final var composer = new TagomiComposerImpl(new TagomiStoreImpl(context.getTagomi()), context.getTagomi().getImageStorage());
    return new AssetsTagomiController(tagomiClient, composer);
  }

  @Bean
  public AssetsDeploymentController assetsDeploymentController(EveliEditEnvir context) {
    return new AssetsDeploymentController(context.getAuthoring());
  }
  @Bean 
  public AssetsPublicationController assetReleaseController(
      EveliEditEnvir context, 
      WorkerAuthClient security,
      ApplicationEventPublisher publisher
  ) {
    return new AssetsPublicationController(context.getAuthoring(), false);
  }
  
  @Bean
  public AssetsDialobController assetsDialobController(EveliEditEnvir context) {
    return new AssetsDialobController(context.getEnvirProps().getFormDb());
  }

  @Bean
  public AssetsWrenchController wrenchComposerController(EveliEditEnvir context) {
    return new AssetsWrenchController(context.getAuthoring(), new CompilerImpl(context.getEnvirProps()));
  }
  @Bean
  public AssetsStencilController assetsStencilController(EveliEditEnvir context) {
    return new AssetsStencilController(context.getAuthoring());
  }

  /**
   * Create this bean for edit envir
   */
  public static EveliEditEnvir eveliEditEnvir(
      ApplicationContext context,
      io.vertx.mutiny.sqlclient.Pool pgPool,
      EnvironmentProperties envir,
      io.resys.limaone.program.Runtime runtime,
      EveliPropsAssets assetProps
    ) {
    
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
    
    
    final var authoring = new AuthoringImpl(ImmutableAuthoringConfig.builder()
        .persistence(envir.getModelDb())
        .envir(envir)
        .build());
    
    
    final var dev = new EveliEditEnvir(envir, runtime, authoring, assetProps, tagomi);
    getOrCreateDb(dev).await().atMost(Duration.ofMinutes(1));
    return dev;
  }
  
  /**
   * Call this for get/create wrench/stencil db-s only needed for edit envir
   */
  public static Uni<EveliEditEnvir> getOrCreateDb(EveliEditEnvir envir) {
    final var createModelWorld = envir.envirProps.getModelDb().createModelWorldDb().createDbIfNotPresent();
    
    final var createTagomi = new TagomiStoreImpl(envir.getTagomi()).tenantBuilder().createIfNot();
    return Uni.combine().all()
        .unis(createModelWorld, createTagomi)
        .asTuple().onItem().transform(e -> envir);
  }
  

}
