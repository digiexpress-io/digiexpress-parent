package io.thestencil.iam.spi.integrations;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.ImmutableUserAction;
import io.thestencil.iam.api.ImmutableUserMessage;
import io.thestencil.iam.api.UserActionsClient.Attachment;
import io.thestencil.iam.api.UserActionsClient.AttachmentQuery;
import io.thestencil.iam.api.UserActionsClient.UserAction;
import io.thestencil.iam.api.UserActionsClient.UserActionQuery;
import io.thestencil.iam.api.UserActionsClient.UserActionsClientConfig;
import io.thestencil.iam.api.UserActionsClient.UserMessage;
import io.thestencil.iam.spi.support.BuilderTemplate;
import io.thestencil.iam.spi.support.PortalAssert;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class UserActionQueryDefault extends BuilderTemplate implements UserActionQuery {
  private final UserActionsClientConfig config;
  private final Supplier<MessagesQueryBuilderDefault> messages;
  private final Supplier<AttachmentQuery> attachments;
  private final Supplier<TaskQueryBuilderDefault> tasks;
  private String userId;
  private String processId;
  private Integer limit;
  private String userName;
  private String representativeUserName;
  
  public UserActionQueryDefault(
      RequestOptions init, UserActionsClientConfig config, 
      Supplier<MessagesQueryBuilderDefault> messages,
      Supplier<AttachmentQuery> attachments, 
      Supplier<TaskQueryBuilderDefault> tasks) {
    super(config.getWebClient(), init);
    this.config = config;
    this.messages = messages;
    this.attachments = attachments;
    this.tasks = tasks;
  }
  @Override
  public UserActionQuery userId(String userId) {
    this.userId = userId;
    return this;
  }
  @Override
  public UserActionQuery processId(String processId) {
    this.processId = processId;
    return this;
  }
  @Override
  public UserActionQuery userName(String userName) {
    this.userName = userName;
    return this;
  }
  @Override
  public UserActionQuery limit(Integer limit) {
    this.limit = limit;
    return this;
  }
  @Override
  public Multi<UserAction> list() {
    PortalAssert.notEmpty(userId, () -> "userId must be defined!");
    PortalAssert.notEmpty(userName, () -> "userName must be defined!");
    final var tasks = messages.get().getUnreadTasks(userId);
    
    if(processId != null) {
      final var process = super.get(getUri("/processes/" + processId)).send();
      
      return Uni.combine().all().unis(process, tasks).asTuple()
          .onItem()
          .transformToMulti(tuple -> 
            findOne(tuple.getItem1(), tuple.getItem2(), config.getFillPath(), config.getReviewPath(), config.getMessagesPath())
          )
          .onItem()
          .transformToUni(action -> addAttachments(action))
          .concatenate();
    } else {
      final var processes  = super.get(getUri("/processesSearch"))
          .addQueryParam("unpaged", "true")
          .addQueryParam("size", limit == null ? "300" : limit + "")
          .addQueryParam("userId", userId)
          .send();  
      
      return Uni.combine().all().unis(processes, tasks).asTuple()
          .onItem()
          .transformToMulti(tuple -> 
            findAll(tuple.getItem1(), tuple.getItem2(), config.getFillPath(), config.getReviewPath(), config.getMessagesPath())
          )
          .onItem()
          .transformToUni(action -> addAttachments(action))
          .concatenate();
    }
  }

  private Uni<UserAction> addAttachments(UserAction action) {
    final var processAttachments = attachments.get().processId(action.getId());
    final var taskAttachments = action.getTaskId() == null ? 
        Uni.createFrom().item(new ArrayList<Attachment>()) : 
        attachments.get().taskId(action.getTaskId(), action.getId());
    
    return Uni.combine().all().unis(processAttachments, taskAttachments)
        .asTuple().onItem().transform(tuple -> {
          
          final var attachments = new HashMap<>(tuple.getItem1().stream().collect(Collectors.toMap(e -> e.getName(), e -> e)));
          for(final var item : tuple.getItem2()) {
            if(!attachments.containsKey(item.getName())) {
              attachments.put(item.getName(), item);
            }
          }
          
          return ImmutableUserAction.builder()
          .from(action)
          .addAllAttachments(attachments.values())
          .build();
          
        });
        
  }
  
  private Multi<UserAction> findOne(HttpResponse<?> resp, List<String> unreadTasks, String fillUri, String reviewUri, String replyUri) {
    if (resp.statusCode() != 200) {
      log.error("USER ACTIONS, find one: Can't create response, code: {}, message: {}, headers:{}", resp.statusCode(), resp.statusMessage(), resp.headers());
      return Multi.createFrom().empty();
    }
    
    final JsonObject data = resp.bodyAsJsonObject();
    if(data == null) {
      return Multi.createFrom().empty();
    }
    return Multi.createFrom()
        .items(mapToUserAction(data, fillUri, reviewUri, replyUri))
        .onItem().transformToUniAndMerge(action -> createAction(action, unreadTasks));
  }
  
  private Multi<UserAction> findAll(HttpResponse<?> resp, List<String> unreadTasks, String fillUri, String reviewUri, String replyUri) {
    if (resp.statusCode() != 200) {
      log.error("USER ACTIONS, find all: code: {}, message: {}, headers:{} ", resp.statusCode(), resp.statusMessage(), resp.headers());
      return Multi.createFrom().empty();
    }
    
    final JsonObject paged = resp.bodyAsJsonObject();
    final var content = paged.getJsonObject("_embedded");
    if(content == null) {
      return Multi.createFrom().empty();
    }
    
    final var datalist = content.getJsonArray("processDataList");
    if(datalist == null) {
      return Multi.createFrom().empty();
    }
    return Multi.createFrom()
        .items(datalist.stream().map(e -> mapToUserAction((JsonObject) e, fillUri, reviewUri, replyUri)))
        .onItem().transformToUniAndMerge(action -> createAction(action, unreadTasks));
  }
  
  private Uni<UserAction> createAction(UserAction action, List<String> unreadTasks) {
    if(action.getTaskId() != null) {

      return Uni.combine().all()
          .unis(
              messages.get().getTask(action.getTaskId()),
              tasks.get().getTask(action.getTaskId())
          ).asTuple()
          .onItem().transform(tuple -> {
            
            final var src = tuple.getItem1();
            final var task = tuple.getItem2();
            
            var lastUpdate = action.getUpdated();
            final var userMessages = new ArrayList<UserMessage>();
            for(final var msg : src) {

              final var userMsg = ImmutableUserMessage.builder()
                  .from(msg)
                  .userName(getMessageUserName(msg))
                  .build();
              userMessages.add(userMsg);
              
              final var msgCreated = OffsetDateTime.parse(msg.getCreated()).toLocalDateTime();
              if(lastUpdate.isBefore(msgCreated)) {
                lastUpdate = msgCreated;
              }
            }
            
            return ImmutableUserAction.builder()
              .from(action)
              .taskStatus(task.getStatus())
              .taskCreated(task.getCreated())
              .taskUpdated(task.getUpdated())
              .updated(lastUpdate)
              .addAllMessages(userMessages)
              .viewed(userMessages.isEmpty() || !unreadTasks.contains(action.getTaskId()))
              .build();
          });
    }
    return Uni.createFrom().item(action);    
  }
  
  private String getMessageUserName(UserMessage msg) {
    final var start = msg.getUserName();
    if(start.equals(userName)) {
      return msg.getUserName();
    }
    if(start.equals(representativeUserName)) {
      return msg.getUserName();
    } 
    return "";
  }

  public static UserAction mapToUserAction(JsonObject entity, String fillUri, String reviewUri, String replyUri) {
    final var workflow = entity.getJsonObject("workflow");
    final var status = entity.getString("status");
    final var formInProgress = "ANSWERING".equalsIgnoreCase(status) || "CREATED".equalsIgnoreCase(status);
    final var formId = entity.getString("questionnaire");
    return ImmutableUserAction.builder()
        .id(entity.getLong("id") + "")
        .status(status)
        .created(LocalDateTime.parse(entity.getString("created")))
        .updated(LocalDateTime.parse(entity.getString("updated")))
        .name(workflow.getString("name"))
        .taskId(entity.getString("task"))
        .messagesUri(replyUri)
        .reviewUri(reviewUri)
        .formUri(fillUri)
        .formId(formId)
        .viewed(true)
        .formInProgress(formInProgress)
        .build();
  }
  @Override
  public UserActionQuery representativeUserName(String representativeUserName) {
    this.representativeUserName = representativeUserName;
    return this;
  }
}
