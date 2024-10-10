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
import javax.inject.Inject;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.thestencil.iam.api.ImmutableRemoteIntegration;
import io.thestencil.iam.spi.integrations.UserActionsClientDefault;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;

@ApplicationScoped
public class UserActionsProducer {

  /**
   * Injection point for the ID Token issued by the OpenID Connect Provider
   */
  @Inject JsonWebToken idToken;
  
  
  private RuntimeConfig runtimeConfig;
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
        
        .replyTo(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.tasks.host)).path(cleanPath(runtimeConfig.tasks.path))
            .protocol(runtimeConfig.tasks.protocol).port(runtimeConfig.tasks.port).build())
        .processes(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.processes.host)).path(cleanPath(runtimeConfig.processes.path))
            .protocol(runtimeConfig.processes.protocol).port(runtimeConfig.processes.port).build())
        .fill(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.fill.host)).path(cleanPath(runtimeConfig.fill.path))
            .protocol(runtimeConfig.fill.protocol).port(runtimeConfig.fill.port).build())
        .review(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.review.host)).path(cleanPath(runtimeConfig.review.path))
            .protocol(runtimeConfig.review.protocol).port(runtimeConfig.review.port).build())
        .attachments(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.attachments.host)).path(cleanPath(runtimeConfig.attachments.path))
            .protocol(runtimeConfig.attachments.protocol).port(runtimeConfig.attachments.port).build())
        ).build(idToken);
    return new UserActionsContext(client);
  }
  
  public static String cleanPath(String value) {
    final var start = value.startsWith("/") ? value.substring(1) : value;
    return start.endsWith("/") ? value.substring(0, start.length() -2) : start;
  }
}
