package io.digiexpress.eveli.client.api;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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
import java.time.ZonedDateTime;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.smallrye.mutiny.Uni;
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
    List<Attachment> createMany() throws ProcessNotFoundException, AttachmentUploadUrlException;
  }
  
  interface UserMessagesQuery {
    List<UserMessage> findAllByActionId(String actionId) throws ProcessNotFoundException;
    List<UserMessage> findAllByUserId();
  }
  
  interface UserActionQuery {
    List<UserAction> findAll();
  }
  
  interface UserActionBuilder {
    UserActionBuilder actionId(String actionId);
    UserActionBuilder clientLocale(String clientLocale); 
    UserActionBuilder inputContextId(String inputContextId);
    UserActionBuilder inputParentContextId(String inputParentContextId);
    Uni<UserAction> createOne();
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
    String getReviewUri();
    String getMessagesUri();
    String getFormUri();
    String getFormId();
    LocalDateTime getCreated();
    LocalDateTime getUpdated();
    
    @Nullable String getInputContextId();
    @Nullable String getInputParentContextId();
    

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
  
  public static class UserActionNotAllowedException extends RuntimeException {
    private static final long serialVersionUID = 1781444267360040922L;
    public UserActionNotAllowedException(String message) {
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
