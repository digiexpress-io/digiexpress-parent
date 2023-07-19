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

import java.util.function.Supplier;

import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.ImmutableUserMessage;
import io.thestencil.iam.api.UserActionsClient.UserActionsClientConfig;
import io.thestencil.iam.api.UserActionsClient.ReplyToBuilder;
import io.thestencil.iam.api.UserActionsClient.UserActionQuery;
import io.thestencil.iam.api.UserActionsClient.UserMessage;
import io.thestencil.iam.spi.support.BuilderTemplate;
import io.thestencil.iam.spi.support.PortalAssert;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.client.HttpResponse;


public class ReplyToBuilderDefault extends BuilderTemplate implements ReplyToBuilder {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReplyToBuilderDefault.class);
  private final UserActionsClientConfig config;
  private final Supplier<UserActionQuery> query;
  
  private String userName;
  private String userId;
  private String replyToId;
  private String text;
  private String processId;
  
  public ReplyToBuilderDefault(RequestOptions init, UserActionsClientConfig config, Supplier<UserActionQuery> query) {
    super(config.getWebClient(), init);
    this.config = config;
    this.query = query;
  }
  
  @Value.Immutable
  public interface UserActionReplyInit {
    String getCommentText();
    Boolean getExternal();
    String getReplyToId();
    String getTaskId();
    String getUserName();
  }
  @Override
  public ReplyToBuilder userId(String userId) {
    this.userId = userId;
    return this;
  }
  @Override
  public ReplyToBuilder replyToId(String replyToId) {
    this.replyToId = replyToId;
    return this;
  }
  @Override
  public ReplyToBuilder processId(String processId) {
    this.processId = processId;
    return this;
  }
  @Override
  public ReplyToBuilder text(String text) {
    this.text = text;
    return this;
  }
  @Override
  public ReplyToBuilder userName(String userName) {
    this.userName = userName;
    return this;
  }
  @Override
  public Uni<UserMessage> build() {
    PortalAssert.notEmpty(userId, () -> "userId must be defined!");
    PortalAssert.notEmpty(userName, () -> "userName must be defined!");
    PortalAssert.notEmpty(replyToId, () -> "replyToId must be defined!");
    PortalAssert.notEmpty(text, () -> "text must be defined!");
    PortalAssert.notEmpty(processId, () -> "processId must be defined!");

    return query.get().processId(processId).userId(userId).userName(userName).limit(1).list().collect()
        .asList().onItem().ifNotNull()
        .transformToUni(list -> {
          
          if(list.size() == 1) {
            final var action = list.get(0);
            PortalAssert.isTrue(action.getId().equals(processId), () -> "processId != action.id!");
            
            final var replyTo = action.getMessages().stream()
                .filter(m -> m.getId().equals(replyToId))
                .findFirst();
            if(replyTo.isPresent()) {
              return createReplyTo(replyTo.get());
            }
            LOGGER.error("USER ACTIONS CREATE REPLY: User is trying to associate tasks/messages that do not belong to them: " + processId + "!");
          }
          LOGGER.error("USER ACTIONS CREATE REPLY: There are no messages for the process: " + processId + "!");
          return Uni.createFrom().item(ImmutableUserMessage.builder().id("").taskId("").created("").userName(userId).replyToId(replyToId).commentText(text).build());
        });
  }
  
  private Uni<UserMessage> createReplyTo(UserMessage parent) {
    final var uri = getUri("/externalComment");
    final var init = ImmutableUserActionReplyInit.builder()
        .replyToId(parent.getId())
        .taskId(parent.getTaskId())
        .commentText(text)
        .userName(userName)
        .external(true)
        .build();

    return post(uri)
        .addQueryParam("userId", userId)
        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json")
        .sendBuffer(io.vertx.mutiny.core.buffer.Buffer.newInstance(JsonObject.mapFrom(init).toBuffer()))
        .onItem().transform(resp -> map(resp, uri));
  }
  
  private UserMessage map(HttpResponse<?> resp, String uri) {
    int code = resp.statusCode();
    if (code < 200 || code >= 300) {
      String error = "USER ACTIONS CREATE REPLY: Can't create response, uri: " + uri + ", e = " + resp.statusCode() + " | " + resp.statusMessage() + " | " + resp.headers();
      LOGGER.error(error);
      return ImmutableUserMessage.builder()
          .id("")
          .taskId("")
          .created("")
          .userName(userId)
          .replyToId(replyToId)
          .commentText(text)
          .build();
    }
    final JsonObject data = resp.bodyAsJsonObject();
    return MessagesQueryBuilderDefault.mapToUserMessage(data, config.getMessagesPath());
  }
}
