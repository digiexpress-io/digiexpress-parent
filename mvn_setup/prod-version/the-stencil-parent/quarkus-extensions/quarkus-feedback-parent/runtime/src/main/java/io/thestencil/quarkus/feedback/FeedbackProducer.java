package io.thestencil.quarkus.feedback;

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

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.thestencil.iam.api.ImmutableRemoteIntegration;
import io.thestencil.iam.spi.integrations.UserActionsClientDefault;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;

@ApplicationScoped
public class FeedbackProducer {

  private RuntimeConfig runtimeConfig;
  private String servicePath;
  private String fillPath;
  private String allowedPath;
  
  public FeedbackProducer setRuntimeConfig(RuntimeConfig runtimeConfig) {
    this.runtimeConfig = runtimeConfig;
    return this;
  }
  public FeedbackProducer setServicePath(String servicePath) {
    this.servicePath = servicePath;
    return this;
  }
  public FeedbackProducer setFillPath(String fillPath) {
    this.fillPath = fillPath;
    return this;
  }
  public FeedbackProducer setAllowedPath(String allowedPath) {
    this.allowedPath = allowedPath;
    return this;
  }
  @Produces
  @ApplicationScoped
  public FeedbackContext feedbackContext(Vertx vertx) {
    final JsonWebToken idToken = null;
    final var webClient = WebClient.create(vertx, new WebClientOptions());
    final var client = UserActionsClientDefault.builder()
      .config(b -> b
        .webClient(webClient)
        .defaultLanguage(runtimeConfig.defaultLocale)
        .servicePath(servicePath)
        .fillPath(fillPath)
        
        
        .attachmentsPath("attachmentsPath/disabled")
        .reviewPath("reviewPath/disabled")
        .messagesPath("messagesPath/disabled")
        .authorizationsPath("authorizationsPath/disabled")
        

        .processes(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.processes.host)).path(cleanPath(runtimeConfig.processes.path))
            .protocol(runtimeConfig.processes.protocol).port(runtimeConfig.processes.port).build())
        .fill(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.fill.host)).path(cleanPath(runtimeConfig.fill.path))
            .protocol(runtimeConfig.fill.protocol).port(runtimeConfig.fill.port).build())

        
        
        ).build(idToken);
    
    
    return new FeedbackContext(
        client, 
        allowedPath,
        runtimeConfig.allowed,
        runtimeConfig.userName, 
        runtimeConfig.userId,
        runtimeConfig.firstName, 
        runtimeConfig.lastName,
        runtimeConfig.email, 
        runtimeConfig.address
    );
  }
  
  public static String cleanPath(String value) {
    final var start = value.startsWith("/") ? value.substring(1) : value;
    return start.endsWith("/") ? value.substring(0, start.length() -2) : start;
  }
}
