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

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.event.NotificationMessagingComponent;
import io.digiexpress.eveli.client.event.TaskEventPublisher;
import io.digiexpress.eveli.client.event.TaskNotificator;
import io.digiexpress.eveli.client.iam.PortalAccessValidator;
import io.digiexpress.eveli.client.iam.PortalAccessValidatorImpl;
import io.digiexpress.eveli.client.persistence.entities.TaskRefGenerator;
import io.digiexpress.eveli.client.persistence.repositories.CommentRepository;
import io.digiexpress.eveli.client.persistence.repositories.ProcessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskAccessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import io.digiexpress.eveli.client.spi.process.CreateProcessExecutorImpl.SpringTransactionWrapper;
import io.digiexpress.eveli.client.spi.process.CreateProcessExecutorImpl.TransactionWrapper;
import io.digiexpress.eveli.client.spi.process.DialobCallbackController;
import io.digiexpress.eveli.client.spi.process.ProcessClientImpl;
import io.digiexpress.eveli.client.spi.task.TaskClientImpl;
import io.digiexpress.eveli.client.web.resources.comms.EmailNotificationController;
import io.digiexpress.eveli.client.web.resources.comms.EmailNotificationController.EmailFilter;
import io.digiexpress.eveli.client.web.resources.comms.PrintoutController;
import io.digiexpress.eveli.client.web.resources.worker.AttachmentApiController;
import io.digiexpress.eveli.client.web.resources.worker.CommentApiController;
import io.digiexpress.eveli.client.web.resources.worker.ProcessApiController;
import io.digiexpress.eveli.client.web.resources.worker.TaskApiController;
import io.digiexpress.eveli.dialob.api.DialobClient;
import jakarta.persistence.EntityManager;



@Configuration
@EnableConfigurationProperties( value = {
    EveliProps.class, 
    EveliPropsAssets.class,  
    EveliPropsEmail.class, 
    EveliPropsGamut.class, 
    EveliPropsPrintout.class,
    EveliPropsTask.class
})
public class EveliAutoConfig {


  @Bean 
  public AttachmentApiController attachmentApiController(ProcessClient processClient, AuthClient security, TaskClient taskClient, AttachmentCommands attachments) {
    return new AttachmentApiController(attachments, taskClient, security, processClient);
  }
  @Bean 
  public CommentApiController commentApiController(
      TaskClient taskClient, AuthClient security) {
    
    return new CommentApiController(taskClient, security);
  }
  
  @Bean 
  public EmailNotificationController emailNotificationController(EveliPropsEmail emailProps) {
    return new EmailNotificationController(emailProps, new EmailFilter(emailProps));
  }

  @Bean 
  public PrintoutController printoutController(
      AuthClient authClient,  
      RestTemplate restTemplate,
      DialobClient dialobClient,
      TaskClient taskClient,
      EveliPropsPrintout printoutConfig
  ) {
    return new PrintoutController(taskClient, authClient, dialobClient, restTemplate, printoutConfig.getServiceUrl());
  }

  @Bean 
  public TaskApiController taskApiController(AuthClient security, TaskClient taskClient, TaskAccessRepository taskAccessRepository, TaskRepository taskRepository) {
    return new TaskApiController(security, taskClient, taskAccessRepository, taskRepository);
  }
  
  
  @Bean 
  public ProcessApiController processApiController(ProcessClient client) {
    return new ProcessApiController(client);
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
  public TransactionWrapper transactionWrapper(EntityManager entityManager) {
      return new SpringTransactionWrapper(entityManager);
  }
  @Bean
  public PortalAccessValidator portalAccessValidator(ProcessClient client) {
      return new PortalAccessValidatorImpl(client);
  }  
  @Bean
  public TaskRefGenerator taskRefGenerator(EntityManager client) {
      return new TaskRefGenerator(client);
  }  
  
  @Bean 
  public TaskClient taskClient(

      TaskRepository taskRepository,
      TaskNotificator taskNotificator,      
      TaskRefGenerator taskRefGenerator,
      JdbcTemplate jdbcTemplate,
      
      TaskAccessRepository taskAccessRepository,
      CommentRepository commentRepository
      
  ) {
  
    return new TaskClientImpl(jdbcTemplate, taskRepository, taskRefGenerator, taskNotificator, taskAccessRepository, commentRepository);
  }
  
  
  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jacksonConfig() {
      return builder -> builder
          .modules(new GuavaModule(), new JavaTimeModule(), new Jdk8Module())
          .build();
  }

  @Bean
  public DialobCallbackController dialobCallbackController(
      ThreadPoolTaskScheduler submitTaskScheduler, 
      ProcessClient processClient, 
      DialobClient dialobClient,
      ObjectMapper objectMapper) {
    
    return new DialobCallbackController(submitTaskScheduler, processClient, dialobClient, objectMapper);
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
      EveliContext eveliContext
      ) {

    return new ProcessClientImpl(processJPA, eveliContext.getWrench(), eveliContext.getProgramEnvir(), ts, eveliContext.getAssets());
  }

}
