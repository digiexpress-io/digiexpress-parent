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
import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.assets.spi.EveliAssetsClientImpl;
import io.digiexpress.eveli.assets.spi.EveliAssetsComposerImpl;
import io.digiexpress.eveli.assets.spi.EveliAssetsDeserializer;
import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.client.api.PortalClient;
import io.digiexpress.eveli.client.web.resources.AssetsDialobController;
import io.digiexpress.eveli.client.web.resources.AssetsStencilController;
import io.digiexpress.eveli.client.web.resources.AssetsWorkflowController;
import io.digiexpress.eveli.client.web.resources.AssetsWorkflowTagController;
import io.digiexpress.eveli.client.web.resources.AssetsWrenchController;
import io.digiexpress.eveli.client.web.resources.AssetsReleaseController;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.hdes.client.spi.HdesClientImpl;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.resys.hdes.client.spi.ThenaStore;
import io.resys.hdes.client.spi.composer.ComposerEntityMapper;
import io.resys.hdes.client.spi.config.HdesClientConfig.DependencyInjectionContext;
import io.resys.hdes.client.spi.config.HdesClientConfig.ServiceInit;
import io.resys.hdes.client.spi.flow.validators.IdValidator;
import io.resys.thena.docdb.spi.pgsql.PgErrors;
import io.resys.thena.docdb.sql.DocDBFactorySql;
import io.thestencil.client.spi.StencilClientImpl;
import io.thestencil.client.spi.StencilComposerImpl;
import io.thestencil.client.spi.StencilStoreImpl;
import io.thestencil.client.spi.serializers.ZoeDeserializer;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.SslMode;
import io.vertx.sqlclient.PoolOptions;
import lombok.extern.slf4j.Slf4j;



@Configuration
@Slf4j
public class EveliAutoConfigAssets {
  
  @Value("${spring.datasource.url}")
  private String datasourceUrl;
  @Value("${spring.datasource.username}")
  private String datasourceUsername;
  @Value("${spring.datasource.password}")
  private String datasourcePassword;
  
  // TODO @Value("${app.version}")
  private String version = "alpha";

  // TODO  @Value("${build.timestamp}")
  private String timestamp = "";

  @Bean
  public AssetsDialobController assetsDialobController(PortalClient client, ObjectMapper objectMapper) {
    return new AssetsDialobController(client.dialob(), objectMapper);
  }
  @Bean 
  public AssetsReleaseController assetReleaseController(EveliContext context, AuthClient security) {
    return new AssetsReleaseController(new EveliAssetsComposerImpl(context.getAssets(), context.getStencil(), context.getWrench()), security);
  }
  @Bean 
  public AssetsWorkflowController workflowController(EveliContext context) {
    return new AssetsWorkflowController(new EveliAssetsComposerImpl(context.getAssets(), context.getStencil(), context.getWrench()), context.getProgramEnvir());
  }
  @Bean 
  public AssetsWorkflowTagController workflowReleaseController(EveliContext context, AuthClient security) {
    return new AssetsWorkflowTagController(new EveliAssetsComposerImpl(context.getAssets(), context.getStencil(), context.getWrench()), security);
  }
  @Bean
  public AssetsWrenchController wrenchComposerController(EveliContext context, ObjectMapper objectMapper) {
    return new AssetsWrenchController(new HdesComposerImpl(context.getWrench()), objectMapper, version, timestamp);
  }
  @Bean
  public AssetsStencilController assetsStencilController(EveliContext context, ObjectMapper objectMapper) {
    return new AssetsStencilController(new StencilComposerImpl(context.getStencil()), objectMapper);
  }
  

  @Bean
  public EveliContext eveliContext(
      EveliProps eveliProps, 
      ObjectMapper objectMapper,
      ApplicationContext context
    ) {
    
    final var datasourceConfig = datasourceUrl.split(":");
    final var portAndDb = datasourceConfig[datasourceConfig.length -1].split("\\/");

    
    final var pgHost = datasourceConfig[2].substring(2);
    final var pgPort = Integer.parseInt(portAndDb[0]);
    final var pgDb = portAndDb[1];
    final var sslMode = SslMode.ALLOW;
    
    final io.vertx.mutiny.pgclient.PgPool pgPool = io.vertx.mutiny.pgclient.PgPool.pool(
        new PgConnectOptions()
          .setHost(pgHost)
          .setPort(pgPort)
          .setDatabase(pgDb)
          .setUser(datasourceUsername)
          .setPassword(datasourcePassword)
          .setSslMode(sslMode), 
        new PoolOptions().setMaxSize(5));
    
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
            .client(DocDBFactorySql.create().client(pgPool).errorHandler(new PgErrors()).build())
            .objectMapper(objectMapper)
            .repoName("stencil-assets")
            .headName("main")
            .deserializer(new ZoeDeserializer(objectMapper))
            .serializer((entity) -> {
              try {
                return objectMapper.writeValueAsString(entity);
              } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
              }
            })
            .gidProvider(type -> UUID.randomUUID().toString())
            .authorProvider(() -> "eveli")
        ).build());

    final Supplier<ProgramEnvir> programEnvir = () -> {
      final var state = wrenchClient.store().query().get().await().atMost(Duration.ofMinutes(1));
      return ComposerEntityMapper.toEnvir(wrenchClient.envir(), state).build();          
    };


    final var assetClient = EveliAssetsClientImpl.builder()
        .config((builder) -> builder
            .client(DocDBFactorySql.create().client(pgPool).errorHandler(new PgErrors()).build())
            .repoName("eveli-assets")
            .headName("main")
            .deserializer(new EveliAssetsDeserializer(objectMapper))
            .objectMapper(objectMapper)
            .serializer((entity) -> {
              try {
                return objectMapper.writeValueAsString(entity);
              } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
              }
            })
            .gidProvider(() -> UUID.randomUUID().toString())
            .authorProvider(() -> "junit-test"))
            
        .build();
    

    final var createdAssets = assetClient.repoBuilder().createIfNot().await().atMost(Duration.ofSeconds(5));
    final var createdWrench = wrenchClient.store().repo().createIfNot().await().atMost(Duration.ofSeconds(5));
    final var createdStencil = stencilClient.getStore().repo().createIfNot().await().atMost(Duration.ofSeconds(5));
    
    
    final var msg = new StringBuilder("\r\n")
      .append("Creating asset DB:").append("\r\n")
      .append("  parsed-datasource-url: ").append(datasourceUrl).append("\r\n")
      .append("  pgHost: ").append(pgHost).append("\r\n")
      .append("  pgPort: ").append(pgPort).append("\r\n")
      .append("  pgDb: ").append(pgDb).append("\r\n")
      .append("  sslMode: ").append(sslMode).append("\r\n")
      
      .append("  workflows: ").append("\r\n")
      .append("    created-db: ").append(createdAssets).append("\r\n")
      .append("    connected-to-repo: ").append(assetClient.getConfig().getRepoName()).append("\r\n")
      
      .append("  wrench: ").append("\r\n")
      .append("    created-db: ").append(createdWrench).append("\r\n")
      .append("    connected-to-repo: ").append(wrenchClient.store().getRepoName()).append("\r\n")
      
      
      .append("  stencil: ").append("\r\n")
      .append("    created-db: ").append(createdStencil).append("\r\n")
      .append("    connected-to-repo: ").append(stencilClient.getStore().getRepoName()).append("\r\n")
      ;
    
    EveliAutoConfigAssets.log.info(msg.toString());
    
    return EveliContext.builder()
        .stencil(stencilClient)
        .wrench(wrenchClient)
        .programEnvir(programEnvir)
        .assets(assetClient)
        .build();
  }
  
  
  /*

  @Bean
  public WrenchConfig hdesReadonly(EveliProps eveliProps, EveliPropsHdes hdesProps, ObjectMapper objectMapper, ApplicationContext context) {

        WorkflowCommandsJPA.builder().workflowJPA(workflowRepository.get()).releaseJPA(workflowReleaseRepository.get()).build();

    HdesClientImpl.builder().objectMapper(objectMapper)
        .dependencyInjectionContext(new DependencyInjectionContext() {
          @Override
          public <T> T get(Class<T> type) {
            return context.getBean(type);
          }
        })
        .serviceInit(new ServiceInit() {
          @Override
          public <T> T get(Class<T> type) {
            return context.getAutowireCapableBeanFactory().createBean(type);
          }
        })
        .store(HdesInMemoryStore.builder().objectMapper(objectMapper).build()).build();
    
    final var state = hdesClient.store().query().get().await().atMost(Duration.ofMinutes(1));
    final var envir = ComposerEntityMapper.toEnvir(hdesClient.envir(), state).build();
    final Supplier<ProgramEnvir> programEnvir = () -> envir;
    
    
    final var client = StencilClientImpl.builder().defaultObjectMapper().inmemory().build()
        .sites().source(buildItem.getContent())
        .imagePath("")
        .created(System.currentTimeMillis());
    final var content = client.build();
    final var contentValues = content.getSites().entrySet().stream()
        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    
    if(!contentValues.containsKey(config.defaultLocale)) {
      throw new ConfigurationException("Markdowns must have localization for default-locale: '" + config.defaultLocale + "'!");
    } 
    
    WorkflowCommandsJson.builder().workflows(jsonWorkflowLocation.get(), null).build()
  }
  
  

  /*
  @Bean 
  public NotificationController notificationController() {
  }*/
}
