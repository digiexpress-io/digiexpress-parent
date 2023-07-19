package io.thestencil.quarkus.useractions;

/*-
 * #%L
 * quarkus-stencil-user-actions
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.thestencil.iam.api.ImmutableRemoteIntegration;
import io.thestencil.iam.spi.integrations.UserActionsClientDefault;
import io.thestencil.iam.spi.mock.UserActionsClientMock;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;

@ApplicationScoped
public class UserActionsProducer {

  private RuntimeConfig runtimeConfig;
  private boolean mockEnabled;
  private String mockFormId; 
  private String mockApiKey;
  private String servicePath;
  private String reviewPath;
  private String fillPath;
  private String messagesPath;
  private String attachmentsPath;
  private String authorizationsPath;
  
  public UserActionsProducer setRuntimeConfig(RuntimeConfig runtimeConfig) {
    this.runtimeConfig = runtimeConfig;
    return this;
  }
  public UserActionsProducer setMockEndabled(boolean mockEnabled, String apiKey, String formId) {
    this.mockEnabled = mockEnabled;
    this.mockFormId = formId;
    this.mockApiKey = apiKey;
    return this;
  }  
  public UserActionsProducer setServicePath(String servicePath) {
    this.servicePath = servicePath;
    return this;
  }
  public UserActionsProducer setReviewPath(String reviewPath) {
    this.reviewPath = reviewPath;
    return this;
  }
  public UserActionsProducer setFillPath(String fillPath) {
    this.fillPath = fillPath;
    return this;
  }
  public UserActionsProducer setMessagesPath(String messagesPath) {
    this.messagesPath = messagesPath;
    return this;
  }
  public UserActionsProducer setAttachmentsPath(String attachmentsPath) {
    this.attachmentsPath = attachmentsPath;
    return this;
  }
  public UserActionsProducer setAuthorizationsPath(String authorizationsPath) {
    this.authorizationsPath = authorizationsPath;
    return this;
  }
  @Produces
  @ApplicationScoped
  public UserActionsContext userActionsContext(Vertx vertx) {
    final var webClient = WebClient.create(vertx, new WebClientOptions());
    if(mockEnabled) {
      final var mockClient = UserActionsClientMock.builder()
          .webClient(webClient)
          .setApiKey(mockApiKey)
          .setFormId(mockFormId)
          .config(b -> b
            .webClient(webClient)
            .defaultLanguage(runtimeConfig.defaultLocale)
            
            .attachmentsPath(attachmentsPath)
            .servicePath(servicePath)
            .fillPath(fillPath)
            .reviewPath(reviewPath)
            .messagesPath(messagesPath)
            .authorizationsPath(authorizationsPath)
            
            .replyTo(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.tasks.host)).path(cleanPath(runtimeConfig.tasks.path)).build())
            .processes(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.processes.host)).path(cleanPath(runtimeConfig.processes.path)).build())
            .fill(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.fill.host)).path(cleanPath(runtimeConfig.fill.path)).build())
            .review(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.review.host)).path(cleanPath(runtimeConfig.review.path)).build())
            
            .attachments(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.attachments.host)).path(cleanPath(runtimeConfig.attachments.path)).build())
          ).build();
      return new UserActionsContext(mockClient);
    }
      
    final var client = UserActionsClientDefault.builder()
      .config(b -> b
        .webClient(webClient)
        .defaultLanguage(runtimeConfig.defaultLocale)
        
        .attachmentsPath(attachmentsPath)
        .servicePath(servicePath)
        .fillPath(fillPath)
        .reviewPath(reviewPath)
        .messagesPath(messagesPath)
        .authorizationsPath(authorizationsPath)
        
        .replyTo(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.tasks.host)).path(cleanPath(runtimeConfig.tasks.path)).build())
        .processes(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.processes.host)).path(cleanPath(runtimeConfig.processes.path)).build())
        .fill(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.fill.host)).path(cleanPath(runtimeConfig.fill.path)).build())
        .review(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.review.host)).path(cleanPath(runtimeConfig.review.path)).build())
        
        .attachments(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.attachments.host)).path(cleanPath(runtimeConfig.attachments.path)).build())
        ).build();
    return new UserActionsContext(client);
  }
  
  public static String cleanPath(String value) {
    final var start = value.startsWith("/") ? value.substring(1) : value;
    return start.endsWith("/") ? value.substring(0, start.length() -2) : start;
  }
}
