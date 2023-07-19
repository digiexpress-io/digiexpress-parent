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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.ImmutableUserMessage;
import io.thestencil.iam.api.UserActionsClient.UserActionsClientConfig;
import io.thestencil.iam.api.UserActionsClient.UserMessage;
import io.thestencil.iam.spi.support.BuilderTemplate;
import io.thestencil.iam.spi.support.PortalAssert;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.client.HttpResponse;



public class MessagesQueryBuilderDefault extends BuilderTemplate {
  private static final Logger LOGGER = LoggerFactory.getLogger(MessagesQueryBuilderDefault.class);
  private final UserActionsClientConfig config;
  
  public MessagesQueryBuilderDefault(RequestOptions init, UserActionsClientConfig config) {
    super(config.getWebClient(), init);
    this.config = config;
  }
  
  public Uni<List<String>> getUnreadTasks(String userId) {
    final var uri = getUri("/externalTasksUnread");
    var request = super.get(uri).addQueryParam("userId", userId);
    
    //LOGGER.error("UNREAD TASKS: uri: '" + uri + "'" + JsonObject.mapFrom(request.getDelegate().queryParams()).encode());
    
    return request.send().onItem().transformToUni(resp -> mapToIds(resp, uri));
  }
  
  // marks comments read
  public Uni<List<UserMessage>> getTaskCommentsAndMarkThemViewed(String taskId, String userId) {
    final var uri = getUri("/task/" + taskId + "/externalComments");
    var request = super.get(uri).addQueryParam("userId", userId);
    return request.send().onItem().transformToUni(resp -> mapToMessages(resp, uri, config.getMessagesPath()));
  }  
  

  public Uni<List<UserMessage>> getTask(String taskId) {
    PortalAssert.notEmpty(taskId, () -> "taskId must be defined!");
    
    final var uri = getUri("/task/" + taskId + "/comments");
    var request = super.get(uri);
    return request.send().onItem().transformToUni(resp -> mapToMessages(resp, uri, config.getMessagesPath()));
  }
  
  
  private static Uni<List<String>> mapToIds(HttpResponse<?> resp, String uri) {
    if (resp.statusCode() != 200) {
      String error = "USER ACTIONS TASK UNREAD COMMENTS: query: '" + uri + "', can't create response, e = " + resp.statusCode() + " | " + resp.statusMessage() + " | " + resp.headers();
      LOGGER.error(error);
      return Uni.createFrom().item(Collections.emptyList());
    }
    
    final JsonArray data = resp.bodyAsJsonArray();
    return Uni.createFrom().item(data.stream().map(e -> e + "").collect(Collectors.toList()));
  }
  
  private static Uni<List<UserMessage>> mapToMessages(HttpResponse<?> resp, String uri, String replyToUri) {
    if (resp.statusCode() != 200) {
      String error = "USER ACTIONS TASK COMMENTS: query: '" + uri + "', can't create response, e = " + resp.statusCode() + " | " + resp.statusMessage() + " | " + resp.headers();
      LOGGER.error(error);
      return Uni.createFrom().item(Collections.emptyList());
    }
    
    final JsonObject paged = resp.bodyAsJsonObject();
    final var content = paged.getJsonObject("_embedded");
    if(content == null) {
      return Uni.createFrom().item(Collections.emptyList());
    }
    
    final var datalist = content.getJsonArray("comments");
    if(datalist == null) {
      return Uni.createFrom().item(Collections.emptyList());
    }
    
    return Uni.createFrom()
        .item(datalist.stream()
            .map(e -> (JsonObject) e)
            .filter(e -> Boolean.TRUE.equals(e.getBoolean("external")))
            .map(e -> mapToUserMessage(e, replyToUri))
            .collect(Collectors.toList()));
  }

  public static UserMessage mapToUserMessage(JsonObject entity, String replyToUri) {
    final var taskId = entity.getLong("taskId");
    final var replyToId = entity.getLong("replyToId");
    return ImmutableUserMessage.builder()
        .id(entity.getLong("id") + "")
        .taskId(taskId == null ? null : taskId + "")
        .replyToId(replyToId == null ? null : replyToId + "")
        .created(entity.getString("created"))
        .userName(entity.getString("userName"))
        .commentText(entity.getString("commentText"))
        .build();
  }
}
