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

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import org.immutables.value.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface TaskClient {

  PaginateTasks paginateTasks();
  QueryTasks queryTasks();
  QueryUnreadUserTasks queryUnreadUserTasks();
  TaskCommandBuilder taskBuilder();
  
  QueryTaskComments queryComments();
  QueryTaskKeywords queryKeywords();
  
  
  interface TaskCommandBuilder {
    TaskCommandBuilder userId(String userId, String userEmail);
    Uni<Task> createTask(CreateTaskCommand command);
    Uni<Task> modifyTask(String taskId, ModifyTaskCommand command);
    Uni<Task> deleteTask(String taskId);
    Uni<TaskComment> createTaskComment(CreateTaskCommentCommand command);
  }
  
  interface QueryTaskComments {
    Uni<List<TaskComment>> findAllByTaskId(String taskId);
    Uni<TaskComment> getOneById(String commentId);
  }
  
  interface QueryTaskKeywords {
    Uni<List<String>> findAllKeywords();
  }
  
  interface QueryUnreadUserTasks {
    QueryUnreadUserTasks userId(String userId);
    QueryUnreadUserTasks requireAnyRoles(List<String> roles);
    Uni<List<Long>> findAll();
  }
  
  interface QueryTasks {
    Uni<Task> getOneById(String taskId);
  }
  
  interface PaginateTasks {
    PaginateTasks page(@Nullable Pageable pageable);
    PaginateTasks subject(@Nullable String subject); 
    PaginateTasks clientIdentificator(@Nullable String clientIdentificator);
    PaginateTasks assignedUser(@Nullable String assignedUser);
    PaginateTasks status(@Nullable List<TaskStatus> status);
    PaginateTasks priority(@Nullable List<TaskPriority> priority);
    PaginateTasks dueDate(@Nullable String dueDate);
    PaginateTasks role(@Nullable String role); // find task assigned to the role
    
    PaginateTasks requireAnyRoles(List<String> roles); // secondary role filter, must contain at least one of these
    Page<Task> findAll();
  }
  

  @JsonSerialize(as = ImmutableCreateTaskCommentCommand.class)
  @JsonDeserialize(as = ImmutableCreateTaskCommentCommand.class)
  @Value.Immutable
  interface CreateTaskCommentCommand {
    @Nullable Boolean getExternal();
    @Nullable String getReplyToId();
    String getTaskId();
    String getCommentText();
    TaskCommentSource getSource(); 
  }
  

  @JsonSerialize(as = ImmutableCreateTaskCommand.class)
  @JsonDeserialize(as = ImmutableCreateTaskCommand.class)
  @Value.Immutable
  interface CreateTaskCommand {
    // null on new task
    @Nullable TaskStatus getStatus();
    @Nullable ZonedDateTime getCompleted();

    // optional props
    @Nullable String getDescription();
    @Nullable String getClientIdentificator();
    @Nullable LocalDate getDueDate();
    
    @Nullable String getAssignedId();
    @Nullable String getAssignedUser();    
    @Nullable String getAssignedUserEmail();
    
    @Nullable String getQuestionnaireId();
    
    String getSubject();
    @Nullable TaskPriority getPriority();

    List<String> getKeyWords();
    Set<String> getAssignedRoles();
  }
  
  
  @JsonSerialize(as = ImmutableModifyTaskCommand.class)
  @JsonDeserialize(as = ImmutableModifyTaskCommand.class)
  @Value.Immutable
  interface ModifyTaskCommand {
    @Nullable TaskStatus getStatus();
    @Nullable ZonedDateTime getCompleted();
    @Nullable Integer getVersion();

    @Nullable String getDescription();
    @Nullable String getClientIdentificator();
    @Nullable LocalDate getDueDate();
    @Nullable String getAssignedId();
    
    @Nullable String getAssignedUser();    
    @Nullable String getAssignedUserEmail();
    @Nullable TaskPriority getPriority();
    
    String getSubject();
    Set<String> getAssignedRoles();
  }
  
  
  
  
  
  
  

  enum TaskStatus { NEW, OPEN, COMPLETED, REJECTED, DELEGATED }
  enum TaskPriority { LOW, NORMAL, HIGH }
  enum TaskCommentSource { FRONTDESK, PORTAL }
  
  @JsonSerialize(as = ImmutableTask.class)
  @JsonDeserialize(as = ImmutableTask.class)
  @Value.Immutable
  interface Task {
    // null on new task
    String getId();
    ZonedDateTime getCreated();
    @Nullable ZonedDateTime getUpdated();
    String getUpdaterId();
    
    String getTaskRef(); // Task reference, semantic ID for task.
    TaskStatus getStatus();
    @Nullable ZonedDateTime getCompleted();
    String getVersion();

    // optional props
    @Nullable String getQuestionnaireId();
    @Nullable String getDescription();
    @Nullable String getClientIdentificator();
    @Nullable LocalDate getDueDate();
    
    @Nullable String getAssignedId();
    @Nullable String getAssignedUser();    
    @Nullable String getAssignedUserEmail();
    
    String getSubject();
    TaskPriority getPriority();

    List<String> getKeyWords();
    Set<String> getAssignedRoles();
  }
  
  
  @JsonSerialize(as = ImmutableTaskComment.class)
  @JsonDeserialize(as = ImmutableTaskComment.class)
  @Value.Immutable
  interface TaskComment {
    // null on new
    @Nullable String getId();
    @Nullable ZonedDateTime getCreated();
    
    @Nullable Boolean getExternal();
    @Nullable String getUserName();
    @Nullable String getReplyToId();
    
    String getTaskId();
    String getCommentText();
    TaskCommentSource getSource(); 
  }
}
