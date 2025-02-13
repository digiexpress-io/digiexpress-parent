package io.thestencil.iam.api;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

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

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.WebClient;

public interface UserActionsClient {

  UserActionBuilder createUserAction();
  UserActionQuery queryUserAction();
  CancelUserActionBuilder cancelUserAction();
  ReplyToBuilder replyTo();
  MarkUserActionBuilder markUser();
  
  AttachmentBuilder attachment();
  AttachmentDownloadBuilder attachmentDownload();
  AttachmentQuery queryAttachments();
  AuthorizationActionQuery authorizationActionQuery();
  
  
  FillBuilder fill();
  ReviewBuilder review();
  UserActionsClientConfig config();
  
  
  interface AuthorizationActionQuery {
    AuthorizationActionQuery userRoles(List<String> userRoles);
    Uni<AuthorizationAction> get();
  }
  
  
  interface AttachmentQuery {
    Uni<List<Attachment>> processId(String processId);
    Uni<List<Attachment>> taskId(String taskId, String processId);
  }
  
  interface AttachmentBuilder {
    AttachmentBuilder processId(String processId);
    AttachmentBuilder userName(String userName);
    AttachmentBuilder userId(String userId);
    AttachmentBuilder data(String name, String fileType);
    AttachmentBuilder call(Consumer<AttachmentBuilder> callback);
    Multi<Attachment> build();
  }
  
  interface AttachmentDownloadBuilder {
    AttachmentDownloadBuilder processId(String processId);
    AttachmentDownloadBuilder userName(String userName);
    AttachmentDownloadBuilder userId(String userId);
    AttachmentDownloadBuilder attachmentId(String attachmentId);
    Uni<AttachmentDownloadUrl> build();
  }
  
  interface MarkUserActionBuilder {
    MarkUserActionBuilder userName(String userName);
    MarkUserActionBuilder userId(String userId);
    MarkUserActionBuilder processId(String processId);
    Uni<List<UserMessage>> build();    
  }
  
  interface ReplyToBuilder {
    ReplyToBuilder userName(String userName);
    ReplyToBuilder userId(String userId);
    ReplyToBuilder replyToId(String replyToId);
    ReplyToBuilder text(String text);
    ReplyToBuilder processId(String processId);
    Uni<UserMessage> build();
  }
  
  interface FillBuilder {
    FillBuilder path(String path);
    FillBuilder method(HttpMethod method);
    FillBuilder body(Buffer body);
    Uni<Buffer> build();
  }

  interface ReviewBuilder {
    ReviewBuilder path(String path);
    Uni<Buffer> build();
  }
  
  interface UserActionBuilder {
    UserActionBuilder actionName(String actionName);
    UserActionBuilder userName(String firstName, String lastName);
    UserActionBuilder companyName(String companyName);
    UserActionBuilder userId(String userId);
    UserActionBuilder language(String language);
    UserActionBuilder email(String email);
    UserActionBuilder address(String address);
    UserActionBuilder protectionOrder(Boolean protectionOrder);
    UserActionBuilder representative(String representativeFirstName, String representativeLastName, String representativeUserId);
    
    Uni<UserAction> build();
  }
  interface CancelUserActionBuilder {
    CancelUserActionBuilder userName(String userName);
    CancelUserActionBuilder processId(String processId);
    CancelUserActionBuilder userId(String userId);
    Uni<UserAction> build();
  }
  
  interface UserActionQuery {
    UserActionQuery userName(String userName);
    UserActionQuery representativeUserName(String userName);
    UserActionQuery processId(String processId);
    UserActionQuery limit(Integer limit);
    UserActionQuery userId(String userId);
    Multi<UserAction> list();
  }
  
  @JsonSerialize(as = ImmutableUserTask.class)
  @JsonDeserialize(as = ImmutableUserTask.class)
  @Value.Immutable
  interface UserTask {
    String getId();
    String getStatus();
    ZonedDateTime getCreated();
    @Nullable
    ZonedDateTime getUpdated();
  }
  
  @JsonSerialize(as = ImmutableUserAction.class)
  @JsonDeserialize(as = ImmutableUserAction.class)
  @Value.Immutable
  interface UserAction {
    String getId();
    String getName();
    String getStatus();
    String getReviewUri();
    String getMessagesUri();
    String getFormUri();
    String getFormId();
    LocalDateTime getCreated();
    LocalDateTime getUpdated();

    @Nullable
    String getTaskId();
    @Nullable
    String getTaskStatus();
    @Nullable
    ZonedDateTime getTaskCreated();
    @Nullable
    ZonedDateTime getTaskUpdated();
    
    
    Boolean getViewed();
    List<UserMessage> getMessages();
    List<Attachment> getAttachments();
    Boolean getFormInProgress();
  }
  
  @JsonSerialize(as = ImmutableUserMessage.class)
  @JsonDeserialize(as = ImmutableUserMessage.class)
  @Value.Immutable
  interface UserMessage {
    String getId();
    String getCreated();
    String getCommentText();
    String getUserName();
    @Nullable
    String getReplyToId();
    @Nullable
    String getTaskId();
  }
  
  @Value.Immutable
  interface UserActionsClientConfig {
    WebClient getWebClient();
    String getDefaultLanguage();
    
    String getServicePath();
    String getFillPath();
    String getReviewPath();
    String getMessagesPath();
    String getAttachmentsPath();
    String getAuthorizationsPath();
    
    RemoteIntegration getAttachments();
    RemoteIntegration getReplyTo();
    RemoteIntegration getProcesses();
    RemoteIntegration getFill();
    RemoteIntegration getReview();
  }

  @JsonSerialize(as = ImmutableAttachmentDownloadUrl.class)
  @JsonDeserialize(as = ImmutableAttachmentDownloadUrl.class)
  @Value.Immutable
  interface AttachmentDownloadUrl {
    String getDownload();
  }
  
  @JsonSerialize(as = ImmutableAttachment.class)
  @JsonDeserialize(as = ImmutableAttachment.class)
  @Value.Immutable
  interface Attachment {
    String getId();
    String getName();
    String getStatus();
    Long getSize();
    String getCreated();
    
    @Nullable
    String getUpload();
    @Nullable
    String getProcessId();
    @Nullable
    String getTaskId();
    
  }
  
  @Value.Immutable
  public interface AttachmentData {
    String getFileName();
    String getData(); // base64
    String getFileType();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableAuthorizationAction.class)
  @JsonDeserialize(as = ImmutableAuthorizationAction.class)
  interface AuthorizationAction {
    List<String> getUserRoles();
    List<String> getAllowedProcessNames();
  }
}
