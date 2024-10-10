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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.ImmutableAttachment;
import io.thestencil.iam.api.UserActionsClient.Attachment;
import io.thestencil.iam.api.UserActionsClient.AttachmentBuilder;
import io.thestencil.iam.api.UserActionsClient.UserAction;
import io.thestencil.iam.api.UserActionsClient.UserActionQuery;
import io.thestencil.iam.api.UserActionsClient.UserActionsClientConfig;
import io.thestencil.iam.spi.support.PortalAssert;
import io.vertx.core.http.RequestOptions;

public class AttachmentBuilderDefault extends MessagesQueryBuilderDefault implements AttachmentBuilder {
  private static final Logger LOGGER = LoggerFactory.getLogger(MarkUserActionBuilderDefault.class);
  
  private final Supplier<UserActionQuery> query;
  
  private String processId;
  private String userName;
  private String userId;
  private Map<String, String> data = new HashMap<>();
  
  public AttachmentBuilderDefault(RequestOptions init, UserActionsClientConfig config, Supplier<UserActionQuery> query, JsonWebToken idToken) {
    super(init, config, idToken);
    this.query = query;
  }
  @Override
  public AttachmentBuilder processId(String processId) {
    this.processId = processId;
    return this;
  }
  @Override
  public AttachmentBuilder data(String name, String fileType) {
    this.data.put(name, fileType);
    return this;
  }
  @Override
  public AttachmentBuilder userName(String userName) {
    this.userName = userName;
    return this;
  }
  @Override
  public AttachmentBuilder userId(String userId) {
    this.userId = userId;
    return this;
  }
  @Override
  public AttachmentBuilder call(Consumer<AttachmentBuilder> callback) {
    callback.accept(this);
    return this;
  }
  @Override
  public Multi<Attachment> build() {
    PortalAssert.notEmpty(processId, () -> "processId must be defined!");
    PortalAssert.notEmpty(userName, () -> "userName must be defined!");
    PortalAssert.notEmpty(userId, () -> "userId must be defined!");
    
    return query.get().processId(processId).userId(userId).userName(userName).limit(1)
        .list().collect().first()
        .onItem().ifNotNull()
        .transformToMulti(src -> Multi.createFrom()
            .items(data.entrySet().stream()
            .map(entry -> new UserActionFileData(src, entry.getKey(), entry.getValue()))))
        .onItem().transformToUni(entry -> createAttachment(entry))
        .concatenate();
  }

  private Uni<Attachment> createAttachment(UserActionFileData data) {
    final String uri;
    if(data.getAction().getTaskId() == null) {
      uri = getUri("/attachments/process/" + data.getAction().getId() + "/files/");
    } else {
      uri = getUri("/attachments/task/" + data.getAction().getTaskId() + "/files/");
    }
    return post(uri)
        .addQueryParam("filename", data.getFileName())
        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), data.getFileType())
        .send()
        .onItem().transform(resp -> {
          
          if(resp.statusCode() != 200) {
            String error = "USER ACTIONS CREATE ATTACHMENT: Can't create response, uri: " + uri + ", e = " + resp.statusCode() + " | " + resp.statusMessage() + " | ";
            LOGGER.error(error);
            return (Attachment) ImmutableAttachment.builder()
                .created(LocalDateTime.now().toString())
                .size(0L)
                .name(data.getFileName())
                .processId(data.getAction().getId())
                .taskId(data.getAction().getTaskId())
                .status("ERROR")
                .build();
          }
          
          final var putCommand = resp.bodyAsJsonObject();
          final var putRequestUrl = putCommand.getString("putRequestUrl");
          return (Attachment) ImmutableAttachment.builder()
              .id(uri)
              .created(LocalDateTime.now().toString())
              .size(0L)
              .name(data.getFileName())
              .upload(putRequestUrl)
              .processId(data.getAction().getId())
              .taskId(data.getAction().getTaskId())
              .status("OK")
              .build();
        });
  }
  
  private static class UserActionFileData {
    private final UserAction action;
    private final String fileName;
    private final String fileType;

    public UserActionFileData(UserAction action, String fileName, String fileType) {
      super();
      this.action = action;
      this.fileName = fileName;
      this.fileType = fileType;
    }
    public String getFileType() {
      return fileType;
    }
    public UserAction getAction() {
      return action;
    }
    public String getFileName() {
      return fileName;
    }
  }
}
