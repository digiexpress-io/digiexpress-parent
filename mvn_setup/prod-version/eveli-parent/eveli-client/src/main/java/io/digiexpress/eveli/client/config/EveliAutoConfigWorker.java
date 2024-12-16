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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.client.api.FeedbackClient;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.iam.PortalAccessValidator;
import io.digiexpress.eveli.client.iam.PortalAccessValidatorImpl;
import io.digiexpress.eveli.client.persistence.repositories.TaskAccessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import io.digiexpress.eveli.client.web.resources.comms.EmailNotificationController;
import io.digiexpress.eveli.client.web.resources.comms.EmailNotificationController.EmailFilter;
import io.digiexpress.eveli.client.web.resources.comms.PrintoutController;
import io.digiexpress.eveli.client.web.resources.worker.AttachmentApiController;
import io.digiexpress.eveli.client.web.resources.worker.CommentApiController;
import io.digiexpress.eveli.client.web.resources.worker.FeedbackApiController;
import io.digiexpress.eveli.client.web.resources.worker.ProcessApiController;
import io.digiexpress.eveli.client.web.resources.worker.TaskApiController;
import io.digiexpress.eveli.dialob.api.DialobClient;



@Configuration
public class EveliAutoConfigWorker {
  @Bean 
  public AttachmentApiController attachmentApiController(ProcessClient processClient, AuthClient security, TaskClient taskClient, AttachmentCommands attachments) {
    return new AttachmentApiController(attachments, taskClient, security, processClient);
  }
  @Bean 
  public CommentApiController commentApiController(TaskClient taskClient, AuthClient security) {
    
    return new CommentApiController(taskClient, security);
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
  public TaskApiController taskApiController(
      FeedbackClient feedback,
      AuthClient security, 
      TaskClient taskClient, 
      TaskAccessRepository taskAccessRepository, 
      TaskRepository taskRepository) {
    
    return new TaskApiController(security, taskClient, taskAccessRepository, taskRepository);
  }
  @Bean 
  public ProcessApiController processApiController(ProcessClient client) {
    return new ProcessApiController(client);
  }
  @Bean
  public PortalAccessValidator portalAccessValidator(ProcessClient client) {
      return new PortalAccessValidatorImpl(client);
  }
  @Bean 
  public EmailNotificationController emailNotificationController(EveliPropsEmail emailProps) {
    return new EmailNotificationController(emailProps, new EmailFilter(emailProps));
  }
  @Bean 
  public FeedbackApiController feedbackApiController(AuthClient authClient, FeedbackClient feedbackClient, ObjectMapper objectMapper) {
    return new FeedbackApiController(authClient, feedbackClient, objectMapper);
  }
}
