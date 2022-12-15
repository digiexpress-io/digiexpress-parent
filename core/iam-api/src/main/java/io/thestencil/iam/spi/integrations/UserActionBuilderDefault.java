package io.thestencil.iam.spi.integrations;

/*-
 * #%L
 * iam-api
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

import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.ImmutableUserAction;
import io.thestencil.iam.api.UserActionsClient.ClientConfig;
import io.thestencil.iam.api.UserActionsClient.UserAction;
import io.thestencil.iam.api.UserActionsClient.UserActionBuilder;
import io.thestencil.iam.spi.support.BuilderTemplate;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.client.HttpResponse;


public class UserActionBuilderDefault extends BuilderTemplate implements UserActionBuilder {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserActionBuilderDefault.class);
  private final ClientConfig config;
  private String actionName;
  private String firstName;
  private String lastName;
  private String userId;
  private String language;
  private String address;
  private String email;
  private Boolean protectionOrder;
  
  @Value.Immutable
  public interface ProcessesInit {
    String getFirstName();
    String getLastName();
    String getIdentity();
    String getWorkflowName();
    String getEmail();
    String getAddress();
    String getLanguage();
    Boolean getProtectionOrder();
  }
  
  public UserActionBuilderDefault(RequestOptions init, ClientConfig config) {
    super(config.getWebClient(), init);
    this.config = config;
  }
  @Override
  public UserActionBuilder actionName(String actionName) {
    this.actionName = actionName;
    return this;
  }
  @Override
  public UserActionBuilder userName(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
    return this;
  }
  @Override
  public UserActionBuilder userId(String userId) {
    this.userId = userId;
    return this;
  }
  @Override
  public UserActionBuilder language(String language) {
    this.language = language;
    return this;
  }
  @Override
  public UserActionBuilder address(String address) {
    this.address = address;
    return this;
  }
  @Override
  public UserActionBuilder email(String email) {
    this.email = email;
    return this;
  }
  @Override
  public UserActionBuilder protectionOrder(Boolean protectionOrder) {
    this.protectionOrder = protectionOrder;
    return this;
  }
  @Override
  public Uni<UserAction> build() {

    
    final var init = ImmutableProcessesInit.builder()
        .firstName(firstName)
        .lastName(lastName)
        .workflowName(actionName)
        .protectionOrder(protectionOrder)
        .identity(userId)
        .email(email == null ? "" : email)
        .address(address == null ? "" : address)
        .language(language == null ? config.getDefaultLanguage() : language)
        .build();
    
    return post(getUri("/processes/"))
        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json")
        .sendBuffer(io.vertx.mutiny.core.buffer.Buffer.newInstance(JsonObject.mapFrom(init).toBuffer()))
        .onItem().transform(resp -> map(resp, config.getFillPath(), config.getReviewPath(), config.getMessagesPath()));
  }
  
  private static UserAction map(HttpResponse<?> resp, String fillUri, String reviewUri, String replyToUri) {
    if (resp.statusCode() != 201) {
      String error = "USER ACTIONS: Can't create response, e = " + resp.statusCode() + " | " + resp.statusMessage() + " | " + resp.headers();
      LOGGER.error(error);
      return ImmutableUserAction.builder()
          .id("").name("").status("")
          .formId("")
          .reviewUri("")
          .formUri(fillUri)
          .viewed(true)
          .formInProgress(false)
          .build();
    }
    final var body = resp.bodyAsJsonObject();
    if(LOGGER.isDebugEnabled()) {
      LOGGER.debug("USER ACTIONS: task created!");
    }
    return UserActionQueryDefault.mapToUserAction(body, fillUri, reviewUri, replyToUri);
  }
}
