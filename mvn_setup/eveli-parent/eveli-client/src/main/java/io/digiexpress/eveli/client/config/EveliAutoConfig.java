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
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.FeedbackCategoriesReader;
import io.digiexpress.eveli.client.api.FeedbackClient;
import io.digiexpress.eveli.client.api.PdfClient;
import io.digiexpress.eveli.client.api.QuestionnaireAttachmentCommands;
import io.digiexpress.eveli.client.api.TaskAuditClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TenantConfigClient;
import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.eveli.client.spi.dialob.DialobScheduler;
import io.digiexpress.eveli.client.spi.dialob.SyncDialobAndProcess;
import io.digiexpress.eveli.client.spi.dms.DocContainerClient;
import io.digiexpress.eveli.client.spi.feedback.FeedbackCategoriesReaderImpl;
import io.digiexpress.eveli.client.spi.feedback.FeedbackClientImpl;
import io.digiexpress.eveli.client.spi.feedback.FeedbackWithHistory;
import io.digiexpress.eveli.client.spi.health.HealthClientImpl;
import io.digiexpress.eveli.client.spi.process.PdfClientRest;
import io.digiexpress.eveli.client.spi.process.ProcessQuestionnaireAttachmentCommand;
import io.digiexpress.eveli.client.spi.task.ImmutableTaskStoreConfig;
import io.digiexpress.eveli.client.spi.task.TaskClientImpl;
import io.digiexpress.eveli.client.spi.task.TaskFileClientImpl;
import io.digiexpress.eveli.client.spi.task.TaskStoreImpl;
import io.digiexpress.eveli.client.spi.taskaudit.TaskAuditClientImpl;
import io.digiexpress.eveli.client.spi.tenant.TenantConfigClientProps;
import io.digiexpress.eveli.client.web.resources.worker.HealthApiController;
import io.digiexpress.eveli.client.web.resources.worker.TenantApiController;
import io.digiexpress.eveli.userprofile.client.api.UserProfileClient;
import io.digiexpress.eveli.userprofile.client.spi.UserProfileClientImpl;
import io.digiexpress.eveli.userprofile.client.spi.UserProfileStore;
import io.digiexpress.thena.mq.client.api.ThenaMqAppConfig;
import io.digiexpress.thena.mq.client.api.ThenaMqClient;
import io.resys.limaone.program.ImmutableCurrentUser;
import io.resys.limaone.program.Runtime.CurrentUser;
import io.resys.limaone.program.Runtime.EnvironmentProperties;
import io.resys.limaone.spi.compiler.CompilerImpl;
import io.resys.limaone.spi.dialob.FormDb;
import io.resys.limaone.spi.runtime.DefaultEnvironmentProperties;
import io.resys.limaone.spi.runtime.DefaultRuntime;
import io.resys.limaone.spi.runtime.DefaultEnvironmentProperties.DI;
import io.resys.limaone.spi.runtime.DefaultEnvironmentProperties.ModelDbConfig;
import io.resys.limaone.spi.runtime.DefaultEnvironmentProperties.WSP;
import io.resys.thena.fs.spi.FileSystem_ThenaImpl;
import io.resys.thena.grim.spi.GrimClientImpl;
import io.resys.thena.jackson.JsonArrayDeserializer;
import io.resys.thena.jackson.JsonObjectDeserializer;
import io.resys.thena.storesql.PgErrors;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.VertxModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Configuration
@Import(value = { 
    EveliAutoConfigBatches.class,
    EveliAutoConfigBatchesDev.class,
    EveliAutoConfigContract.class,
    EveliAutoConfigDb.class,
    DialobAutoConfig.class,
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
    EveliPropsTagomi.class,
    EveliPropsContract.class,
    EveliPropsCockpit.class,
    DialobConfigProps.class
})
@Slf4j
public class EveliAutoConfig {
  
  @RequiredArgsConstructor
  public static class DI_Impl implements DI {
    private final ApplicationContext context;
    @Override
    public <T> T getBean(Class<T> type) {
      return context.getBean(type);
    }
  }
  
  @RequiredArgsConstructor
  public static class CurrentUserSupplier implements Supplier<CurrentUser> {
    
    private final Optional<WorkerAuthClient> workerAuth;
    
    @Override
    public CurrentUser get() {
      if(workerAuth.isEmpty() || !workerAuth.get().getUser().isAuthenticated()) {
        return ImmutableCurrentUser.builder()
            .userId("")
            .userName("")
            .build();
      }
      
      return ImmutableCurrentUser.builder()
          .userId(workerAuth.get().getUser().getPrincipal().getSub())
          .userName(workerAuth.get().getUser().getPrincipal().getUsername())
          .build();
    }
    
  }
  
  @Bean
  public io.resys.limaone.program.Runtime l1Runtime(EnvironmentProperties envir) {
    if(envir.isDev()) {
      return DefaultRuntime.of(envir);
    }
    final var world = envir.getModelDb().worldQuery().findAllSync();
    return new CompilerImpl(envir).compile(world).build();
  }
  
  @Bean
  public EnvironmentProperties environmentProperties(
      ApplicationContext context,
      io.vertx.mutiny.sqlclient.Pool sqlDb, 
      Optional<WSP> wsp, 
      Optional<WorkerAuthClient> workerAuth,
      FormDb formDb, 
      EveliPropsAssets assetConfig,
      EveliPropsCockpit cockpitConfig) {
    
    
    final var isDevMode = Boolean.TRUE.equals(assetConfig.getEnabled()) || assetConfig.getEnabled() == null;
    if(wsp.isEmpty()) {
      RepoAssert.isTrue(isDevMode, () -> "can't use external asset provider in dev mode!");
    } else {
      RepoAssert.isTrue(wsp.isPresent(), () -> "prod mode requires external asset provider!");
    }
    

    final ModelDbConfig modelDb;
    final Boolean tid;
    if(isDevMode) {
      final var fileSystem = FileSystem_ThenaImpl.createInstance()
          .tenantName("assets")
          .client(sqlDb)
          .errorHandler(new PgErrors())
          .build();
      modelDb = ModelDbConfig.filesystem(fileSystem);
      tid = Boolean.TRUE.equals(cockpitConfig.getEnabled());
    } else {
      modelDb = ModelDbConfig.external(wsp.get());
      tid = false;
    }

    return DefaultEnvironmentProperties.builder()
        .developmentMode(isDevMode)
        .defaultTenantName("assets")
        .formDb(formDb)
        .dbConfig(modelDb)
        .di(new DI_Impl(context))
        .tid(tid)
        .currentUser(new CurrentUserSupplier(workerAuth))
        .tid(tid)
        .build();
  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }
  @Bean
  public FeedbackClient feedbackClient(
      TaskClient taskClient,
      JdbcTemplate jdbc,
      ObjectMapper om,
      TransactionTemplate tx,
      EveliPropsFeedback feedbackProps,
      FeedbackCategoriesReader feedbackCategoriesReader
  ) {
    final var history = new FeedbackWithHistory(tx, jdbc, om);
    return new FeedbackClientImpl(taskClient, jdbc, history, feedbackProps, om, feedbackCategoriesReader);
  }
  @Bean
  public FeedbackCategoriesReader feedbackCategoriesReader(ObjectMapper objectMapper) {
    return new FeedbackCategoriesReaderImpl(objectMapper);
  }

  @Bean 
  public TaskClient taskClient(
      DocContainerClient docContainerClient,
      AttachmentCommands attachmentCommands,
      RestTemplate restTemplate,
      io.vertx.mutiny.sqlclient.Pool pgPool,
      io.resys.limaone.program.Runtime envirClient) {
    
    final var config = ImmutableTaskStoreConfig.builder()
        .tenantName("task-tenant")
        .client(GrimClientImpl.create().client(pgPool).build())
        .build();
    final var store = new TaskStoreImpl(config);
    store.query().createIfNot().await().atMost(Duration.ofMinutes(1));
    
    final var fileClient = new TaskFileClientImpl(attachmentCommands, restTemplate);    
    return new TaskClientImpl(fileClient, docContainerClient, store, envirClient);
  }

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jacksonConfig() {
      SimpleModule module = new VertxModule();
      module.addDeserializer(JsonObject.class, new JsonObjectDeserializer());
      module.addDeserializer(JsonArray.class, new JsonArrayDeserializer());
      
      return builder -> builder
          .modules(new GuavaModule(), new JavaTimeModule(), new Jdk8Module(), module)
          .build();
  }
  
  @Bean(name="submitTaskScheduler")
  public ThreadPoolTaskScheduler submitTaskScheduler() {
    ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setPoolSize(10);
    threadPoolTaskScheduler.setThreadNamePrefix("SubmitTaskScheduler-");
    return threadPoolTaskScheduler;
  }

  @Bean
  public TaskAuditClient taskAuditClient(
      Optional<ThenaMqClient> mqClient,
      Optional<ThenaMqAppConfig> mqConfig,
      TaskClient taskClient
      ) {
    return new TaskAuditClientImpl(taskClient, mqClient, mqConfig);
  }

  @Bean @ConditionalOnMissingBean
  public TenantConfigClient tenantConfigClient(EveliProps props) {
    return new TenantConfigClientProps(props);
  }

  @Bean
  public TenantApiController tenantApiController(TenantConfigClient tenantClient, EveliProps props) {
    return new TenantApiController(tenantClient);
  }

  @Bean 
  public HealthApiController healthApiController(TaskClient task, WorkerAuthClient worker) {
    return new HealthApiController(new HealthClientImpl(task), worker);
  }

  @Bean
  public UserProfileClient userProfileClient(io.vertx.mutiny.sqlclient.Pool pgPool) {    
    final var store = UserProfileStore.builder()
        .repoName("worker-profile")
        .pgPool(pgPool)
        .build();
    store.query().createIfNot().await().atMost(Duration.ofMinutes(1));
    return new UserProfileClientImpl(store);
  }

  @Bean
  public PdfClient pdfClient(
      TaskClient client, 
      FormDb dialob, 
      EveliPropsPrintout printoutConfig, 
      RestTemplate restTemplate, 
      ObjectMapper om) {
    return new PdfClientRest(client, dialob, restTemplate, printoutConfig.getServiceUrl(), om);
  }

  @Bean
  public QuestionnaireAttachmentCommands questionnaireAttachmentCommands(
      AttachmentCommands attachments, 
      PdfClient pdf, 
      TaskClient tasks) {
    return new ProcessQuestionnaireAttachmentCommand(attachments, pdf, tasks);
  }

  @Bean
  public SyncDialobAndProcess syncDialobAndProcess(io.resys.limaone.program.Runtime eveliEnvirClient, ObjectMapper objectMapper, TaskClient taskClient) {
    return new SyncDialobAndProcess(taskClient, eveliEnvirClient, objectMapper);
  }

  @Bean
  public DialobScheduler dialobScheduler(TaskClient taskClient, SyncDialobAndProcess sync) {
    return new DialobScheduler(taskClient, sync);
  }
}
