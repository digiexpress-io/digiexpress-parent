package io.digiexpress.eveli.client.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

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

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.digiexpress.eveli.client.api.FeedbackClient;
import io.digiexpress.eveli.client.api.ImmutableTenantConfig;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TenantConfigClient;
import io.digiexpress.eveli.client.event.NotificationMessagingComponent;
import io.digiexpress.eveli.client.event.TaskEventPublisher;
import io.digiexpress.eveli.client.event.TaskNotificator;
import io.digiexpress.eveli.client.persistence.repositories.ProcessRepository;
import io.digiexpress.eveli.client.spi.feedback.FeedbackClientImpl;
import io.digiexpress.eveli.client.spi.feedback.FeedbackWithHistory;
import io.digiexpress.eveli.client.spi.process.CreateProcessExecutorImpl.SpringTransactionWrapper;
import io.digiexpress.eveli.client.spi.process.CreateProcessExecutorImpl.TransactionWrapper;
import io.digiexpress.eveli.client.spi.process.DialobScheduler;
import io.digiexpress.eveli.client.spi.process.ProcessClientImpl;
import io.digiexpress.eveli.client.spi.task.ImmutableTaskStoreConfig;
import io.digiexpress.eveli.client.spi.task.TaskClientImpl;
import io.digiexpress.eveli.client.spi.task.TaskStoreImpl;
import io.digiexpress.eveli.client.web.resources.worker.TenantApiController;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.resys.thena.storesql.DbStateSqlImpl;
import io.smallrye.mutiny.Uni;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.SslMode;
import io.vertx.sqlclient.PoolOptions;
import jakarta.persistence.EntityManager;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;



@Configuration
@EnableConfigurationProperties( value = {
    EveliProps.class, 
    EveliPropsAssets.class,  
    EveliPropsEmail.class, 
    EveliPropsGamut.class,
    EveliPropsFeedback.class,
    EveliPropsPrintout.class,
    EveliPropsTask.class,
    EveliPropsMq.class,
    EveliPropsEnvir.class,
    
    EveliPropsDb.class
})
@Slf4j
public class EveliAutoConfig {
  @Value("${spring.datasource.url}")
  private String datasourceUrl;
  @Value("${spring.datasource.username}")
  private String datasourceUsername;
  @Value("${spring.datasource.password}")
  private String datasourcePassword;
  
  @Data
  @Builder
  public static class EveliPropsDbResolved {
    private String host;
    private int port;
    private String username;
    private String password;
    private String database;
  }
  
  @Bean
  @ConditionalOnMissingBean
  public EveliPropsDbResolved eveliPropsDbResolved() {
    final var datasourceConfig = datasourceUrl.split(":");
    final var portAndDb = datasourceConfig[datasourceConfig.length -1].split("\\/");

    
    final var pgHost = datasourceConfig[2].substring(2);
    final var pgPort = Integer.parseInt(portAndDb[0]);
    final var database = portAndDb[1];
    
    return EveliPropsDbResolved.builder()
        .username(datasourceUsername)
        .password(datasourcePassword)
        .port(pgPort)
        .host(pgHost)
        .database(database)
        .build();
  }
  
  @Bean 
  public TaskNotificator taskNotificator() {
    return new NotificationMessagingComponent();
  }
  @Bean
  public TaskEventPublisher taskEventPublisher(ApplicationEventPublisher publisher) {
    return new TaskEventPublisher(publisher);
  }
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
  @Bean
  public FeedbackClient feedbackClient(
      TaskClient taskClient,
      ProcessClient processClient,
      JdbcTemplate jdbc,
      ObjectMapper om,
      TransactionTemplate tx,
      EveliPropsFeedback feedbackProps,
      DialobClient dialobClient
  ) {
    final var history = new FeedbackWithHistory(tx, jdbc, om);
    return new FeedbackClientImpl(taskClient, processClient, dialobClient, jdbc, history, feedbackProps);

  }
  @Bean
  public TransactionWrapper transactionWrapper(EntityManager entityManager) {
    return new SpringTransactionWrapper(entityManager);
  }  
  @Bean 
  public TaskClient taskClient(TaskNotificator taskNotificator, io.vertx.mutiny.pgclient.PgPool pgPool) {    
    final var config = ImmutableTaskStoreConfig.builder()
        .tenantName("task-tenant")
        .client(DbStateSqlImpl.create().client(pgPool).build())
        .build();
    final var store = new TaskStoreImpl(config);
    store.query().createIfNot().await().atMost(Duration.ofMinutes(1));
    return new TaskClientImpl(taskNotificator, store);
  }
  
  @Bean
  public io.vertx.mutiny.pgclient.PgPool pgPool(EveliPropsDb db, EveliPropsDbResolved dbConfig) {
    final var sslMode = SslMode.ALLOW;
    
    final io.vertx.mutiny.pgclient.PgPool pgPool = io.vertx.mutiny.pgclient.PgPool.pool(
        new PgConnectOptions()
          .setHost(dbConfig.getHost())
          .setPort(dbConfig.getPort())
          .setDatabase(dbConfig.getDatabase())
          .setUser(dbConfig.getUsername())
          .setPassword(dbConfig.getPassword())
          .setSslMode(sslMode), 
        new PoolOptions().setMaxSize(db.getPoolMaxSize() == null ? 5 : db.getPoolMaxSize()));
    
    return pgPool;
  }
  

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jacksonConfig() {
      return builder -> builder
          .modules(new GuavaModule(), new JavaTimeModule(), new Jdk8Module())
          .build();
  }

  @Bean
  public DialobScheduler dialobScheduler(
      ProcessClient processClient, 
      DialobClient dialobClient,
      ObjectMapper objectMapper) {
    
    return new DialobScheduler(processClient, dialobClient, objectMapper);
  }
  

  @Bean(name="submitTaskScheduler")
  public ThreadPoolTaskScheduler submitTaskScheduler() {
    ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setPoolSize(10);
    threadPoolTaskScheduler.setThreadNamePrefix("SubmitTaskScheduler-");
    return threadPoolTaskScheduler;
  }

  @Bean
  public ProcessClient processClient(
      ProcessRepository processJPA,
      TransactionWrapper ts,
      EveliEnvirClient envir
      ) {

    return new ProcessClientImpl(processJPA, ts, envir);
  }

  @Bean
  public TenantApiController tenantApiController() {
    final TenantConfigClient client = new TenantConfigClient() {
      @Override
      public TenantConfigClientConfigQuery createConfigQuery() {
        return new TenantConfigClientConfigQuery() {
          
          @Override
          public Uni<TenantConfig> getOne() {
            return Uni.createFrom().item(ImmutableTenantConfig
                .builder()
                .build());
          }
        };
      }
    };
    return new TenantApiController(client);
  }

}
