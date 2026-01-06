package io.digiexpress.eveli.client.config;

import java.time.Duration;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

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
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.CustomerAccountClient;
import io.digiexpress.eveli.client.api.FeedbackCategoriesReader;
import io.digiexpress.eveli.client.api.FeedbackClient;
import io.digiexpress.eveli.client.api.PdfClient;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.QuestionnaireAttachmentCommands;
import io.digiexpress.eveli.client.api.TaskAuditClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TenantConfigClient;
import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.eveli.client.persistence.repositories.ProcessRepository;
import io.digiexpress.eveli.client.spi.crm.CustomerAccountClientImpl;
import io.digiexpress.eveli.client.spi.dialob.DialobScheduler;
import io.digiexpress.eveli.client.spi.dialob.SyncDialobAndProcess;
import io.digiexpress.eveli.client.spi.dms.DocContainerClient;
import io.digiexpress.eveli.client.spi.feedback.FeedbackCategoriesReaderImpl;
import io.digiexpress.eveli.client.spi.feedback.FeedbackClientImpl;
import io.digiexpress.eveli.client.spi.feedback.FeedbackWithHistory;
import io.digiexpress.eveli.client.spi.health.HealthClientImpl;
import io.digiexpress.eveli.client.spi.process.CreateProcessExecutorImpl.SpringTransactionWrapper;
import io.digiexpress.eveli.client.spi.process.CreateProcessExecutorImpl.TransactionWrapper;
import io.digiexpress.eveli.client.spi.process.PdfClientRest;
import io.digiexpress.eveli.client.spi.process.ProcessClientImpl;
import io.digiexpress.eveli.client.spi.process.ProcessQuestionnaireAttachmentCommand;
import io.digiexpress.eveli.client.spi.task.ImmutableTaskStoreConfig;
import io.digiexpress.eveli.client.spi.task.TaskClientImpl;
import io.digiexpress.eveli.client.spi.task.TaskFileClientImpl;
import io.digiexpress.eveli.client.spi.task.TaskStoreImpl;
import io.digiexpress.eveli.client.spi.taskaudit.TaskAuditClientImpl;
import io.digiexpress.eveli.client.spi.tenant.TenantConfigClientProps;
import io.digiexpress.eveli.client.web.resources.worker.HealthApiController;
import io.digiexpress.eveli.client.web.resources.worker.TenantApiController;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.userprofile.client.api.UserProfileClient;
import io.digiexpress.eveli.userprofile.client.spi.UserProfileClientImpl;
import io.digiexpress.eveli.userprofile.client.spi.UserProfileStore;
import io.digiexpress.thena.mq.client.api.ThenaMqAppConfig;
import io.digiexpress.thena.mq.client.api.ThenaMqClient;
import io.resys.thena.grim.spi.GrimClientImpl;
import io.resys.thena.jackson.JsonArrayDeserializer;
import io.resys.thena.jackson.JsonObjectDeserializer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.VertxModule;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;



@Configuration
@Import(value = { 
    EveliAutoConfigBatches.class,
    EveliAutoConfigBatchesDev.class,
    EveliAutoConfigContract.class,
    EveliAutoConfigDb.class
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
    EveliPropsCockpit.class
})
@Slf4j
public class EveliAutoConfig {

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
      DialobClient dialobClient,
      FeedbackCategoriesReader feedbackCategoriesReader
  ) {
    final var history = new FeedbackWithHistory(tx, jdbc, om);
    return new FeedbackClientImpl(taskClient, processClient, dialobClient, jdbc, history, feedbackProps, om, feedbackCategoriesReader);
  }
  @Bean
  public FeedbackCategoriesReader feedbackCategoriesReader(ObjectMapper objectMapper) {
    return new FeedbackCategoriesReaderImpl(objectMapper);
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
      io.vertx.mutiny.sqlclient.Pool pgPool,
      EveliEnvirClient envirClient) {
    
    final var config = ImmutableTaskStoreConfig.builder()
        .tenantName("task-tenant")
        .client(GrimClientImpl.create().client(pgPool).build())
        .build();
    final var store = new TaskStoreImpl(config);
    store.query().createIfNot().await().atMost(Duration.ofMinutes(1));
    
    final var fileClient = new TaskFileClientImpl(attachmentCommands, restTemplate);    
    return new TaskClientImpl(envirClient, fileClient, docContainerClient, store, crmClient);
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
  
  
  @Bean
  public SyncDialobAndProcess syncDialobAndProcess(
      ProcessClient processClient, 
      DialobClient dialobClient,
      ObjectMapper objectMapper,
      TaskClient taskClient) {
    return new SyncDialobAndProcess(processClient, taskClient, dialobClient, objectMapper);
  }

  @Bean
  public DialobScheduler dialobScheduler(ProcessClient processClient, SyncDialobAndProcess sync) {
    return new DialobScheduler(processClient, sync);
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
  public TaskAuditClient taskAuditClient(
      Optional<ThenaMqClient> mqClient,
      Optional<ThenaMqAppConfig> mqConfig,
      TaskClient taskClient
      ) {
    return new TaskAuditClientImpl(taskClient, mqClient, mqConfig);
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
      DialobClient dialob, 
      EveliPropsPrintout printoutConfig, 
      RestTemplate restTemplate, 
      ObjectMapper om) {
    return new PdfClientRest(client, dialob, restTemplate, printoutConfig.getServiceUrl(), om);
  }
  
  @Bean
  public QuestionnaireAttachmentCommands questionnaireAttachmentCommands(
      AttachmentCommands attachments, 
      PdfClient pdf, 
      TaskClient tasks, 
      ProcessClient processClient) {
    return new ProcessQuestionnaireAttachmentCommand(attachments, pdf, tasks, processClient);
  }
}
