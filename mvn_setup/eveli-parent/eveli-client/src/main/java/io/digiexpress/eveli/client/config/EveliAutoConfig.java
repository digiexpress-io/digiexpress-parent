package io.digiexpress.eveli.client.config;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.digiexpress.eveli.client.api.*;
import io.digiexpress.eveli.client.event.NotificationMessagingComponent;
import io.digiexpress.eveli.client.event.TaskEventPublisher;
import io.digiexpress.eveli.client.event.TaskNotificator;
import io.digiexpress.eveli.client.persistence.repositories.ProcessRepository;
import io.digiexpress.eveli.client.spi.crm.CustomerAccountClientImpl;
import io.digiexpress.eveli.client.spi.dms.DocContainerClient;
import io.digiexpress.eveli.client.spi.feedback.FeedbackClientImpl;
import io.digiexpress.eveli.client.spi.feedback.FeedbackWithHistory;
import io.digiexpress.eveli.client.spi.process.CreateProcessExecutorImpl.SpringTransactionWrapper;
import io.digiexpress.eveli.client.spi.process.CreateProcessExecutorImpl.TransactionWrapper;
import io.digiexpress.eveli.client.spi.process.DialobScheduler;
import io.digiexpress.eveli.client.spi.process.ProcessClientImpl;
import io.digiexpress.eveli.client.spi.process.SyncDialobAndProcess;
import io.digiexpress.eveli.client.spi.task.ImmutableTaskStoreConfig;
import io.digiexpress.eveli.client.spi.task.TaskClientImpl;
import io.digiexpress.eveli.client.spi.task.TaskFileClientImpl;
import io.digiexpress.eveli.client.spi.task.TaskStoreImpl;
import io.digiexpress.eveli.client.spi.tenant.TenantConfigClientProps;
import io.digiexpress.eveli.client.web.resources.worker.TenantApiController;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.userprofile.client.api.UserProfileClient;
import io.digiexpress.eveli.userprofile.client.spi.UserProfileClientImpl;
import io.digiexpress.eveli.userprofile.client.spi.UserProfileStore;
import io.resys.thena.grim.spi.GrimClientImpl;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.SslMode;
import io.vertx.sqlclient.PoolOptions;
import jakarta.persistence.EntityManager;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Objects;
import java.util.regex.Pattern;



@Configuration
@Import(value = { 
    EveliBatchesAutoConfig.class,
    EveliBatchesDevAutoConfig.class
})
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
    EveliPropsBatch.class,
    
    EveliPropsDb.class
})
@Slf4j
public class EveliAutoConfig {

  @Data
  @Builder
  public static class EveliPropsDbResolved {
    private String host;
    private int port;
    private String username;
    private String password;
    private String database;
    @Builder.Default
    private String sslMode = "allow"; // default ssl mode, can be overridden by EveliPropsDb.sslMode
    @Builder.Default
    private Boolean sslTrustAll = false; // By default, don't trust all, can be overridden by EveliPropsDb.sslTrustAll
    private String certPath; // Path to the certificate file, can be set in EveliPropsDb.certPath
  }
  
  @Bean
  @ConditionalOnMissingBean
  public EveliPropsDbResolved eveliPropsDbResolved(
    @Value("${spring.datasource.url}") String datasourceUrl,
    @Value("${spring.datasource.username}") String datasourceUsername,
    @Value("${spring.datasource.password}") String datasourcePassword
  ) {
    Assert.isTrue(datasourceUrl.startsWith("jdbc:postgresql://"), "postgresql is only supported database type.");
    var matcher = Pattern.compile("^jdbc:postgresql://(?<host>[\\p{Lower}-.\\d]+)(:(?<port>\\d+))?/(?<database>[^?]+)(\\?(?<params>.*))?$")
      .matcher(datasourceUrl);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid datasource URL: " + datasourceUrl);
    }
    var pgHost = matcher.group("host");
    var portMatch = matcher.group("port");
    var port = Integer.parseInt(Objects.toString(portMatch, "5432")); // default PostgreSQL port
    var database = matcher.group("database");
    var params = matcher.group("params");
    var builder = EveliPropsDbResolved.builder()
      .host(pgHost)
      .port(port)
      .database(database)
      .username(datasourceUsername)
      .password(datasourcePassword);

    if (params != null && !params.isEmpty()) {
      for (String param : params.split("&")) {
        var paramNameAndValue = param.split("=");
        var paramName = paramNameAndValue[0];
        var paramValue = paramNameAndValue[1];
        if (StringUtils.isBlank(paramValue)) {
          log.warn("Parameter '{}' in datasource URL is empty, skipping.", paramName);
          continue;
        }
        switch (paramName) {
          case "sslmode":
            builder = builder.sslMode(paramValue);
            break;
          case "ssltrustall":
            builder = builder.sslTrustAll(Boolean.parseBoolean(paramValue));
            break;
          case "sslrootcert":
            builder = builder.certPath(paramValue);
            break;
          default:
            log.warn("Unknown parameter in datasource URL: {}", paramName);
        }
      }
    }
    return builder.build();
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
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
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
  public CustomerAccountClient customerAccountClient(ProcessClient processClient) {
    return new CustomerAccountClientImpl(processClient);
  }
  
  @Bean 
  public TaskClient taskClient(
      CustomerAccountClient crmClient,
      DocContainerClient docContainerClient,
      AttachmentCommands attachmentCommands,
      RestTemplate restTemplate,
      TaskNotificator taskNotificator, 
      io.vertx.mutiny.pgclient.PgPool pgPool,
      EveliEnvirClient envirClient) {
    
    final var config = ImmutableTaskStoreConfig.builder()
        .tenantName("task-tenant")
        .client(GrimClientImpl.create().client(pgPool).build())
        .build();
    final var store = new TaskStoreImpl(config);
    store.query().createIfNot().await().atMost(Duration.ofMinutes(1));
    
    final var fileClient = new TaskFileClientImpl(attachmentCommands, restTemplate);    
    return new TaskClientImpl(envirClient, taskNotificator, fileClient, docContainerClient, store, crmClient);
  }
  
  @Bean
  public io.vertx.mutiny.pgclient.PgPool pgPool(EveliPropsDb db, EveliPropsDbResolved dbConfig) {
    var trustOptions = new PemTrustOptions();
    if (StringUtils.isNotBlank(dbConfig.getCertPath())) {
      trustOptions.addCertPath(dbConfig.getCertPath());
    }
    final io.vertx.mutiny.pgclient.PgPool pgPool = io.vertx.mutiny.pgclient.PgPool.pool(
        new PgConnectOptions()
          .setHost(dbConfig.getHost())
          .setPort(dbConfig.getPort())
          .setDatabase(dbConfig.getDatabase())
          .setUser(dbConfig.getUsername())
          .setPassword(dbConfig.getPassword())
          .setTrustAll(dbConfig.getSslTrustAll())
          .setPemTrustOptions(trustOptions)
          .setSslMode(SslMode.of(dbConfig.getSslMode())),
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
  public SyncDialobAndProcess syncDialobAndProcess(
      ProcessClient processClient, 
      DialobClient dialobClient,
      ObjectMapper objectMapper) {
    return new SyncDialobAndProcess(processClient, dialobClient, objectMapper);
  }

  @Bean
  public DialobScheduler dialobScheduler(
      ProcessClient processClient, 
      DialobClient dialobClient,
      ObjectMapper objectMapper,
      SyncDialobAndProcess sync) {
    
    return new DialobScheduler(processClient, dialobClient, sync);
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
  @ConditionalOnMissingBean
  public TenantConfigClient tenantConfigClient(EveliProps props) {
    return new TenantConfigClientProps(props);
  }  
  
  @Bean
  public TenantApiController tenantApiController(TenantConfigClient tenantClient, EveliProps props) {
    return new TenantApiController(tenantClient);
  }
  
  @Bean
  public UserProfileClient userProfileClient(io.vertx.mutiny.pgclient.PgPool pgPool) {    
    final var store = UserProfileStore.builder()
        .repoName("worker-profile")
        .pgPool(pgPool)
        .build();
    store.query().createIfNot().await().atMost(Duration.ofMinutes(1));
    return new UserProfileClientImpl(store);
  }
}
