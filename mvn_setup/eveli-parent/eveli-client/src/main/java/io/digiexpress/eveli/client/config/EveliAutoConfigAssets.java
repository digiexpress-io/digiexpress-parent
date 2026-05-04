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

import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.eveli.client.web.resources.assets.*;
import io.digiexpress.eveli.client.web.resources.worker.CockpitApiController;
import io.resys.limaone.authoring.Authoring;
import io.resys.limaone.persistence.AuthoringImpl;
import io.resys.limaone.persistence.ImmutableAuthoringConfig;
import io.resys.limaone.program.Runtime.EnvironmentProperties;
import io.resys.limaone.spi.compiler.CompilerImpl;
import io.smallrye.mutiny.Uni;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;


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
  }

  @Bean
  @ConditionalOnBooleanProperty(value = "eveli.cockpit.enabled", havingValue = true, matchIfMissing = false)
  public CockpitApiController cockpitApiController(EveliEditEnvir envir) {
    return new CockpitApiController(envir.getAuthoring());
  }
  
  @Bean
  public AssetsTagomiController assetsTagomiController(EveliEditEnvir context) {
    return new AssetsTagomiController(context.getAuthoring(), context.getRuntime());
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
  @Bean
  public AssetsFsController assetsFsController(EveliEditEnvir context) {
    return new AssetsFsController(context.getAuthoring());
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

    
    
    final var authoring = new AuthoringImpl(ImmutableAuthoringConfig.builder()
        .persistence(envir.getModelDb())
        .envir(envir)
        .build());
    
    
    final var dev = new EveliEditEnvir(envir, runtime, authoring, assetProps);
    getOrCreateDb(dev).await().atMost(Duration.ofMinutes(1));
    return dev;
  }
  
  /**
   * Call this for get/create wrench/stencil db-s only needed for edit envir
   */
  public static Uni<EveliEditEnvir> getOrCreateDb(EveliEditEnvir envir) {
    final var createModelWorld = envir.envirProps.getModelDb().createModelWorldDb().createDbIfNotPresent();
    return createModelWorld.onItem().transform(e -> envir);
  }

}
