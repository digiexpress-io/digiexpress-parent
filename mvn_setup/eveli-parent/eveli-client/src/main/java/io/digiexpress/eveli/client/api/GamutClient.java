package io.digiexpress.eveli.client.api;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.eveli.client.api.GamutAuthClient.Customer;
import io.digiexpress.eveli.client.api.GamutAuthClient.CustomerRoles;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.MigrationBuilder.TopicLink;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;



public interface GamutClient {
  
  UserActionBuilder userActionBuilder();
  UserAttachmentBuilder userAttachmentBuilder();
  UserActionQuery userActionQuery();
  UserMessagesQuery userMessagesQuery();
  ReplyToBuilder replyToBuilder();
  AttachmentDownloadQuery attachmentDownloadQuery();
  CancelUserActionBuilder cancelUserActionBuilder();
  UserActionFillEventBuilder fillEvent();
  UserActionMetaQuery userActionMetaQuery();
  UserActionViewBuilder userActionViewBuilder();
  
  ProcessAuthorizationQuery queryAuthorization();

  
  interface ProcessAuthorizationQuery {
    ProcessAuthorizationQuery cockpitId(@Nullable String cockpitId);
    ProcessAuthorizationQuery userRoles(List<String> userRoles);
    Uni<ProcessAuthorization> getOne();
  }
  

  
  interface UserActionViewBuilder {
    UserActionViewBuilder actionId(String actionId);
    Uni<Void> create();
  }
  
  interface UserActionMetaQuery {
    UserActionMetaQuery locale(String locale);
    UserActionMetaQuery actionId(String actionId);
    UserActionMetaQuery cockpitId(String cockpitId);
    Uni<UserActionMeta> getOne();
  }
  
  interface UserActionFillEventBuilder {
    UserActionFillEventBuilder sessionId(String sessionId);
    UserActionFillEventBuilder requestBody(String req);
    UserActionFillEventBuilder responseBody(String resp);
    UserActionFillEvent create();
  }
  
  interface CancelUserActionBuilder {
    CancelUserActionBuilder actionId(String id);
    UserAction cancelOne() throws ProcessNotFoundException, ProcessCantBeDeletedException;
  }
  
  
  interface AttachmentDownloadQuery {
    AttachmentDownloadQuery filename(String filename);
    AttachmentDownloadQuery actionId(String actionId);
    AttachmentDownloadUrl getOne() throws ProcessNotFoundException;
  }
  
  interface ReplyToBuilder {
    ReplyToBuilder actionId(String actionId);
    ReplyToBuilder from(ReplayToInit init);
    UserMessage createOne() throws ProcessNotFoundException;;
  }
  
  
  interface UserAttachmentBuilder {
    UserAttachmentBuilder actionId(String actionId);
    UserAttachmentBuilder addAll(List<UserAttachmentUploadInit> init);
    List<UserActionAttachment> createMany() throws ProcessNotFoundException, AttachmentUploadUrlException;
  }
  
  interface UserMessagesQuery {
    List<UserMessage> findAllByActionId(String actionId) throws ProcessNotFoundException;
    List<UserMessage> findAllByUserId();
  }
  
  interface UserActionQuery {
    UserActionQuery cockpitId(String cockpitId);
    Multi<UserAction> findAll();
    Uni<Optional<UserAction>> findOneById(String id);
    Uni<Optional<UserAction>> findOneAnonById(String id); // only anon forms can be fetched by id
  }
  
  interface UserActionBuilder {
    UserActionBuilder customer(Customer customer);
    UserActionBuilder customerRoles(CustomerRoles customerRoles);
    UserActionBuilder actionId(String actionId);
    UserActionBuilder taskId(@Nullable String taskId);
    UserActionBuilder cockpitId(@Nullable String cockpitId);
    UserActionBuilder anon(boolean anon);
    UserActionBuilder clientLocale(String clientLocale); 
    UserActionBuilder inputContextId(String inputContextId);
    UserActionBuilder inputParentContextId(String inputParentContextId);
    UserActionBuilder customerAssignment(boolean isCustomerAssignment); 
    Uni<UserAction> createOne();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableUserActionMeta.class)
  @JsonDeserialize(as = ImmutableUserActionMeta.class)
  interface UserActionMeta {
    String getActionId();
    TopicLink getTopicLink();
    @Nullable Long getExpiresInSeconds();
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableUserAttachmentUploadInit.class)
  @JsonDeserialize(as = ImmutableUserAttachmentUploadInit.class)
  interface UserAttachmentUploadInit {
    String getName();
    String getFileType();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableReplayToInit.class)
  @JsonDeserialize(as = ImmutableReplayToInit.class)
  interface ReplayToInit {
    String getSubjectId();
    String getText();
  }
  
  @RequiredArgsConstructor @Data @Builder
  public class UserActionFillEvent {
    private final String sessionId;
    private final String requestBody;
    private final String responseBody;    
  }
  
  

  
  @JsonSerialize(as = ImmutableUserAction.class)
  @JsonDeserialize(as = ImmutableUserAction.class)
  @Value.Immutable
  interface UserAction {
    String getId();
    String getName();
    String getStatus();
    @Nullable String getFormId();
    OffsetDateTime getCreated();
    OffsetDateTime getUpdated();
    
    @Nullable String getInputContextId();
    @Nullable String getInputParentContextId();
    
    @Nullable
    String getCockpitId();
    @Nullable
    String getTaskId();
    @Nullable
    String getTaskRef();
    @Nullable
    String getTaskStatus();
    @Nullable
    ZonedDateTime getTaskCreated();
    @Nullable
    ZonedDateTime getTaskUpdated();
    
    Boolean getAssigned();    
    Boolean getViewed();
    List<UserMessage> getMessages();
    List<UserActionAttachment> getAttachments();
    List<UserSubAction> getSubActions();
    Boolean getFormInProgress();
  }
  
  
  @JsonSerialize(as = ImmutableUserSubAction.class)
  @JsonDeserialize(as = ImmutableUserSubAction.class)
  @Value.Immutable
  interface UserSubAction {
    String getId();
    String getFormId();
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
  
  
  @JsonSerialize(as = ImmutableAttachmentDownloadUrl.class)
  @JsonDeserialize(as = ImmutableAttachmentDownloadUrl.class)
  @Value.Immutable
  interface AttachmentDownloadUrl {
    String getDownload();
  }
  

  @JsonSerialize(as = ImmutableUserActionAttachment.class)
  @JsonDeserialize(as = ImmutableUserActionAttachment.class)
  @Value.Immutable
  interface UserActionAttachment {
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
  @JsonSerialize(as = ImmutableProcessAuthorization.class)
  @JsonDeserialize(as = ImmutableProcessAuthorization.class)
  interface ProcessAuthorization {
    List<String> getUserRoles();
    List<String> getAllowedProcessNames();
  }  
  
  public static class UserActionNotAllowedException extends RuntimeException {
    private static final long serialVersionUID = 1781444267360040922L;
    public UserActionNotAllowedException(String message) {
      super(message);
    }
  }
  
  public static class DialobFormNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1781444267360040922L;
    public DialobFormNotFoundException(String message) {
      super(message);
    }
  }
  
  public static class WorkflowNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1781444267360040922L;
    public WorkflowNotFoundException(String message) {
      super(message);
    }
  }
  
  
  public static class ProcessCantBeDeletedException extends Exception {
    private static final long serialVersionUID = 1781444267360040922L;
    public ProcessCantBeDeletedException(String message) {
      super(message);
    }
  }
  
  public static class ProcessNotFoundException extends Exception {
    private static final long serialVersionUID = 1781444267360040922L;
    public ProcessNotFoundException(String message) {
      super(message);
    }
  }
  
  public static class AttachmentUploadUrlException extends Exception {
    private static final long serialVersionUID = 1781444267360040922L;
    public AttachmentUploadUrlException(String message) {
      super(message);
    }
  }
}
