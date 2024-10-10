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

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.ImmutableAttachment;
import io.thestencil.iam.api.UserActionsClient.Attachment;
import io.thestencil.iam.api.UserActionsClient.AttachmentQuery;
import io.thestencil.iam.api.UserActionsClient.UserActionsClientConfig;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.client.HttpResponse;

public class AttachmentQueryDefault extends MessagesQueryBuilderDefault implements AttachmentQuery {

  private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentQueryDefault.class);
  
  public AttachmentQueryDefault(RequestOptions init, UserActionsClientConfig config, JsonWebToken idToken) {
    super(init, config, idToken);
  }

  @Override
  public Uni<List<Attachment>> processId(String processId) {
    final var uri = getUri("/attachments/process/" + processId + "/files/");
    return get(uri).send().onItem()
        .transform(resp -> mapToElement(resp, null, processId));
  }

  @Override
  public Uni<List<Attachment>> taskId(String taskId, String processId) {
    final var uri = getUri("/attachments/task/" + taskId + "/files/");
    return get(uri).send().onItem()
        .transform(resp -> mapToElement(resp, taskId, processId));
  }
  
  
  private List<Attachment> mapToElement(HttpResponse<?> resp, String taskId, String processId) {
    if (resp.statusCode() != 200) {
      String error = "Attachments query: Can't create response, e = " + resp.statusCode() + " | " + resp.statusMessage() + " | " + resp.headers();
      LOGGER.error(error);
      return Collections.emptyList();
    }
    
    final JsonArray data = resp.bodyAsJsonArray();
    if(data == null) {
      return Collections.emptyList();
    }
    return data.stream().map(e -> mapToAttachment((JsonObject) e, taskId, processId))
        .collect(Collectors.toList());
  }
  
  public static Attachment mapToAttachment(JsonObject entity, String taskId, String processId) {
    final var name = entity.getString("name");
    return ImmutableAttachment.builder()
        .processId(processId)
        .taskId(taskId)
        .id(id(name, taskId, processId))
        .name(name)
        .created(entity.getString("created"))
        .size(entity.getLong("size"))
        .status(entity.getString("status"))
        .build();
  }
  
  public static String id(String processId, String taskId, String fileName) {
    return Hashing
        .murmur3_128()
        .hashString(processId + "::" + taskId + "::" + fileName, Charsets.UTF_8)
        .toString();
  }
}
