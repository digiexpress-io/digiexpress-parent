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

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
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
import io.digiexpress.eveli.client.api.NotificationCommands;
import io.digiexpress.eveli.client.api.PortalClient;
import io.digiexpress.eveli.client.cache.DuplicateDetectionCache;
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
import io.digiexpress.eveli.client.spi.AttachmentCommandsDummy;
import io.digiexpress.eveli.client.spi.DialobCommandsImpl;
import io.digiexpress.eveli.client.spi.HdesCommandsImpl.SpringTransactionWrapper;
import io.digiexpress.eveli.client.spi.HdesCommandsImpl.TransactionWrapper;
import io.digiexpress.eveli.client.spi.NotificationCommandsDummy;
import io.digiexpress.eveli.client.spi.PortalClientImpl;
import io.digiexpress.eveli.client.web.resources.AttachmentApiController;
import io.digiexpress.eveli.client.web.resources.CommentApiController;
import io.digiexpress.eveli.client.web.resources.DialobCallbackController;
import io.digiexpress.eveli.client.web.resources.EmailNotificationController;
import io.digiexpress.eveli.client.web.resources.EmailNotificationController.EmailFilter;
import io.digiexpress.eveli.client.web.resources.FormsController;
import io.digiexpress.eveli.client.web.resources.PortalAttachmentController;
import io.digiexpress.eveli.client.web.resources.PortalCommentController;
import io.digiexpress.eveli.client.web.resources.PortalProcessAnonymousController;
import io.digiexpress.eveli.client.web.resources.PortalProcessController;
import io.digiexpress.eveli.client.web.resources.PortalTaskController;
import io.digiexpress.eveli.client.web.resources.PrintoutController;
import io.digiexpress.eveli.client.web.resources.ProcessApiController;
import io.digiexpress.eveli.client.web.resources.TaskApiController;
import jakarta.persistence.EntityManager;



@Configuration
@EnableConfigurationProperties( value = {
    EveliProps.class, 
    EveliPropsAssets.class, 
    EveliPropsDialob.class, 
    EveliPropsEmail.class, 
    EveliPropsGamut.class, 
    EveliPropsPrintout.class,
    EveliPropsTask.class
})
public class EveliAutoConfig {
  
  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jacksonConfig() {
      return builder -> builder
          .modules(new GuavaModule(), new JavaTimeModule(), new Jdk8Module())
          .build();
  }
  
  @Bean 
  public AttachmentApiController attachmentApiController(PortalClient client, EveliPropsTask config, AuthClient security) {
    return new AttachmentApiController(client, config.isAdminsearch(), security);
  }
  @Bean 
  public CommentApiController commentApiController(
      TaskRepository taskRepository, 
      CommentRepository commentRepository, 
      TaskNotificator notificator, 
      TaskAccessRepository taskAccessRepository, AuthClient security) {
    
    return new CommentApiController(taskRepository, commentRepository, notificator, taskAccessRepository, security);
  }
  @Bean 
  public DuplicateDetectionCache duplicateDetectionCache() {
    return new DuplicateDetectionCache();
  }
  
  @Bean(name="submitTaskScheduler")
  public ThreadPoolTaskScheduler submitTaskScheduler() {
    ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setPoolSize(10);
    threadPoolTaskScheduler.setThreadNamePrefix("SubmitTaskScheduler-");
    return threadPoolTaskScheduler;
  }

  @Bean 
  public DialobCallbackController dialobCallbackController(
      PortalClient client, 
      @Qualifier(value="submitTaskScheduler") ThreadPoolTaskScheduler submitTaskScheduler,
      DuplicateDetectionCache cache) {
    
    final var submitMessageDelay = 10000l;
    return new DialobCallbackController(client, submitTaskScheduler, cache, submitMessageDelay);
  }
  
  
  @Bean 
  public EmailNotificationController emailNotificationController(EveliPropsEmail emailProps) {
    return new EmailNotificationController(emailProps, new EmailFilter(emailProps));
  }
  
  @Bean 
  public FormsController formsController(
    RestTemplate restTemplate, EveliPropsDialob config
    ) {
    return new FormsController(restTemplate, config.getServiceUrl());
  }
  
  @Bean 
  public PortalAttachmentController portalAttachmentController(PortalClient client, PortalAccessValidator validator, AuthClient security) {
    return new PortalAttachmentController(client, validator, security);
  }
  @Bean 
  public PortalCommentController portalCommentController(
      TaskRepository taskRepository, 
      CommentRepository commentRepository, 
      TaskAccessRepository taskAccessRepository, 
      
      PortalClient client, 
      PortalAccessValidator validator, AuthClient security
  ) {
    return new PortalCommentController(taskRepository, commentRepository, taskAccessRepository, client, validator, security);
  }
  @Bean 
  public PortalProcessAnonymousController portalProcessAnonymousController(
      PortalClient client, PortalAccessValidator validator, EveliPropsGamut config, AuthClient security
  ) {
    return new PortalProcessAnonymousController(client, validator, config.getAnonymousUserId(), security);
  }
  @Bean 
  public PortalProcessController portalProcessController(
      PortalClient client, PortalAccessValidator validator, EveliPropsGamut config, AuthClient security
  ) {
    return new PortalProcessController(client, validator, config.getAnonymousUserId(), security);
  }
  @Bean 
  public PortalTaskController portalTaskController(
      TaskAccessRepository taskAccessRepository, TaskRepository taskRepository, PortalClient client, PortalAccessValidator validator, AuthClient security
  ) {
    return new PortalTaskController(taskAccessRepository, taskRepository, client, validator, security);
  }
  @Bean 
  public PrintoutController printoutController(
      PortalClient client,  
      RestTemplate restTemplate,
      EveliPropsTask taskConfig,
      EveliPropsPrintout printoutConfig
  ) {
    return new PrintoutController(client, restTemplate, taskConfig.isAdminsearch(), printoutConfig.getServiceUrl());
  }
  @Bean 
  public ProcessApiController processApiController(PortalClient client) {
    return new ProcessApiController(client);
  }
  @Bean 
  public TaskApiController taskApiController(
      TaskAccessRepository taskAccessRepository, 
      TaskRepository taskRepository, 
      TaskNotificator notificator, 
      JdbcTemplate jdbcTemplate,
      EveliPropsTask config, AuthClient security,
      TaskRefGenerator taskRefGenerator
  ) {
    
    return new TaskApiController(taskRefGenerator, taskAccessRepository, taskRepository, notificator, jdbcTemplate, config.isAdminsearch(), security);
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
  public PortalAccessValidator portalAccessValidator(PortalClient client) {
      return new PortalAccessValidatorImpl(client);
  }  
  @Bean
  public TaskRefGenerator taskRefGenerator(EntityManager client) {
      return new TaskRefGenerator(client);
  }  
  
  @Bean 
  public PortalClient portalClient(
      Optional<AttachmentCommands> attachment, 
      Optional<NotificationCommands> notification,
      
      ProcessRepository processRepository,
      TaskRepository taskRepository,
      TaskNotificator taskNotificator,
      
      TransactionWrapper transactionWrapper,
      TaskRefGenerator taskRefGenerator,
      
      RestTemplate restTemplate,
      ObjectMapper objectMapper,
      EveliPropsDialob dialobProps,
      EveliProps eveliProps,
      EveliContext eveliContext
  ) {
    
    final var dialob = DialobCommandsImpl.builder().objectMapper(objectMapper).client(restTemplate)
        .authorization(dialobProps.getAuthorization())
        .url(dialobProps.getServiceUrl() + "/dialob/api/questionnaires")
        .formUrl(dialobProps.getServiceUrl() + "/dialob/api/forms")
        .submitCallbackUrl(eveliProps.getServiceUrl() + "/dialobSubmitCallback")
        .build();
    
    return PortalClientImpl.builder()
        .dialobCommands(dialob)
        .attachmentCommands(attachment.orElse(new AttachmentCommandsDummy()))
        .notificationCommands(notification.orElse(new NotificationCommandsDummy()))
        
        .hdesClient(eveliContext.getWrench())
        .programEnvir(eveliContext.getProgramEnvir())
        .assetClient(eveliContext.getAssets())
        .transactionWrapper(transactionWrapper)
          
        .processRepository(processRepository)
        .taskNotificator(taskNotificator)
        .taskRepository(taskRepository)
        .taskRefGenerator(taskRefGenerator)
        .build();
  }
}
