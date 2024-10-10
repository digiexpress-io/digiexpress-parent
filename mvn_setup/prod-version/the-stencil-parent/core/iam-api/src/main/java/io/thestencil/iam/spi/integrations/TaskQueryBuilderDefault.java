package io.thestencil.iam.spi.integrations;

/*-
 * #%L
 * stencil-iam-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.time.ZonedDateTime;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.ImmutableUserMessage;
import io.thestencil.iam.api.ImmutableUserTask;
import io.thestencil.iam.api.UserActionsClient.UserActionsClientConfig;
import io.thestencil.iam.api.UserActionsClient.UserMessage;
import io.thestencil.iam.api.UserActionsClient.UserTask;
import io.thestencil.iam.spi.support.BuilderTemplate;
import io.thestencil.iam.spi.support.PortalAssert;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TaskQueryBuilderDefault extends BuilderTemplate {
  private final UserActionsClientConfig config;
  
  public TaskQueryBuilderDefault(RequestOptions init, UserActionsClientConfig config, JsonWebToken idToken) {
    super(config.getWebClient(), init, idToken);
    this.config = config;
  }
  
  public Uni<UserTask> getTask(String taskId) {
    PortalAssert.notEmpty(taskId, () -> "taskId must be defined!");
    
    final var uri = getUri("/task/" + taskId);
    var request = super.get(uri);
    return request.send().onItem().transform(resp -> mapToTask(resp, uri, config.getMessagesPath()));
  }
  
  
  private static UserTask mapToTask(HttpResponse<?> resp, String uri, String replyToUri) {
    if (resp.statusCode() != 200) {
      log.error("TASK QUERY uri: {}, code: {}, message: {}, headers:{} ", uri, resp.statusCode(), resp.statusMessage(), resp.headers());
      return ImmutableUserTask.builder().id("").status("").created(ZonedDateTime.now()).build();
    }
    
    final JsonObject json = resp.bodyAsJsonObject();

    final var updated = json.getString("updated");
    return ImmutableUserTask.builder()
        .id(json.getString("id"))
        .status(json.getString("status"))
        .taskRef(json.getString("taskRef"))
        .created(ZonedDateTime.parse(json.getString("created")))
        .updated(updated == null ? null : ZonedDateTime.parse(updated))
        .build();
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
