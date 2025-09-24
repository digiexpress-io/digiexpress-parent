package io.digiexpress.eveli.client.config;

import org.springframework.context.ApplicationEventPublisher;

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

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.FeedbackClient;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.TaskAuditClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.eveli.client.iam.PortalAccessValidator;
import io.digiexpress.eveli.client.iam.PortalAccessValidatorImpl;
import io.digiexpress.eveli.client.spi.dialob.DialobCreateEventPublisher;
import io.digiexpress.eveli.client.spi.dialob.DialobScheduler;
import io.digiexpress.eveli.client.spi.mq.MqEventPublisher;
import io.digiexpress.eveli.client.spi.task.TaskViewerPublisher;
import io.digiexpress.eveli.client.web.resources.comms.PrintoutController;
import io.digiexpress.eveli.client.web.resources.worker.AttachmentApiController;
import io.digiexpress.eveli.client.web.resources.worker.FeedbackApiController;
import io.digiexpress.eveli.client.web.resources.worker.ProcessApiController;
import io.digiexpress.eveli.client.web.resources.worker.SchedulerApiCotroller;
import io.digiexpress.eveli.client.web.resources.worker.TaskApiController;
import io.digiexpress.eveli.client.web.resources.worker.UserProfileController;
import io.digiexpress.eveli.client.web.resources.worker.WorkerIamController;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.dialob.api.DialobReviewClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.userprofile.client.api.UserProfileClient;



@Configuration
public class EveliAutoConfigWorker {
  @Bean 
  public AttachmentApiController attachmentApiController(ProcessClient processClient, WorkerAuthClient security, TaskClient taskClient, AttachmentCommands attachments) {
    return new AttachmentApiController(attachments, taskClient, security, processClient);
  }
  @Bean 
  public PrintoutController printoutController(
      WorkerAuthClient authClient,  
      RestTemplate restTemplate,
      DialobClient dialobClient,
      TaskClient taskClient,
      EveliPropsPrintout printoutConfig
  ) {
    return new PrintoutController(taskClient, authClient, dialobClient, restTemplate, printoutConfig.getServiceUrl());
  }
  
  @Bean
  public UserProfileController userProfileController(UserProfileClient useProfileClient, WorkerAuthClient authClient) {
    return new UserProfileController(useProfileClient, authClient);
  }
  
  @Bean 
  public TaskApiController taskApiController(
      FeedbackClient feedback,
      WorkerAuthClient security, 
      TaskClient taskclient, 
      DialobClient dialobClient,
      DialobReviewClient dialobReviewClient,
      MqEventPublisher mqEventPublisher,
      TaskViewerPublisher viewerEventPublisher,
      TaskAuditClient taskAuditClient,
      DialobCreateEventPublisher dialobCreateEventPublisher) {
    
    return new TaskApiController(dialobCreateEventPublisher, security, taskclient, dialobClient, dialobReviewClient, mqEventPublisher, viewerEventPublisher, taskAuditClient);
  }
  @Bean 
  public ProcessApiController processApiController(ProcessClient procClient, TaskClient taskClient) {
    return new ProcessApiController(procClient, taskClient);
  }
  @Bean
  public PortalAccessValidator portalAccessValidator(ProcessClient client) {
      return new PortalAccessValidatorImpl(client);
  }
  @Bean
  public WorkerIamController workerIamController(WorkerAuthClient authClient) {
    return new WorkerIamController(authClient);
  } 
  @Bean 
  public FeedbackApiController feedbackApiController(WorkerAuthClient authClient, FeedbackClient feedbackClient) {
    return new FeedbackApiController(authClient, feedbackClient);
  }
  @Bean
  public MqEventPublisher mqEventPublisher(ApplicationEventPublisher publisher) {
    return new MqEventPublisher(publisher);
  }
  @Bean
  public DialobCreateEventPublisher dialobCreateEventPublisher(
      ApplicationEventPublisher publisher,
      TaskClient taskClient,
      ProcessClient processClient,
      DialobClient dialobClient,
      EveliEnvirClient envir,
      MqEventPublisher mqEventPublisher
  ) {
    return new DialobCreateEventPublisher(publisher, taskClient, processClient, dialobClient, envir, mqEventPublisher);
  }
  
  
  @Bean
  public TaskViewerPublisher viewerEventPublisher(ApplicationEventPublisher publisher, TaskClient client) {
    return new TaskViewerPublisher(publisher, client);
  }
  
  @Bean
  public SchedulerApiCotroller schedulerApiCotroller(DialobScheduler dialob) {
    return new SchedulerApiCotroller(dialob);
  }
}
