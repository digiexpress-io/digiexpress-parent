
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
import java.util.Optional;
import java.util.Set;

import org.immutables.value.Value;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;



public interface TaskCommands {
  // used by Wrench flow tasks
  TaskBuilder create();
  Optional<Task> find(String id, List<String> userRoles, boolean adminsearch);
  
  // not used?
  void complete(String id);
  void reject(String id);
  void delete(String id);

  interface TaskBuilder {
    TaskBuilder subject(String subject);
    TaskBuilder description(String description);
    TaskBuilder clientIdentificator(String clientIdentificator);
    TaskBuilder priority(TaskPriority priority);
    TaskBuilder label(String label);
    TaskBuilder dueDate(LocalDate dueDate);
    TaskBuilder questionnaireId(String questionnaireId);
    TaskBuilder assignedId(String assignedId);
    TaskBuilder status(TaskStatus status);
    Task build();
  }

  enum TaskStatus { NEW, OPEN, COMPLETED, REJECTED, DELEGATED }
  enum TaskPriority { LOW, NORMAL, HIGH }
  enum TaskCommentSource { FRONTDESK, PORTAL }
  
  
  @JsonSerialize(as = ImmutableTaskLink.class)
  @JsonDeserialize(as = ImmutableTaskLink.class)
  @Value.Immutable
  interface TaskLink {
    // null on new task
    @Nullable Long getId();
    String getLinkKey();
    String getLinkAddress();
  }

  @JsonSerialize(as = ImmutableTask.class)
  @JsonDeserialize(as = ImmutableTask.class)
  @Value.Immutable
  interface Task {
    // null on new task
    @Nullable Long getId();
    @Nullable ZonedDateTime getCreated();
    @Nullable ZonedDateTime getUpdated();
    @Nullable String getUpdaterId();
    @Nullable String getTaskRef(); // Task reference, semantic ID for task.
    @Nullable TaskStatus getStatus();
    @Nullable ZonedDateTime getCompleted();
    @Nullable Integer getVersion();

    // optional props
    @Nullable String getDescription();
    @Nullable String getClientIdentificator();
    @Nullable LocalDate getDueDate();
    @Nullable String getAssignedId();
    @Nullable String getAssignedUser();    
    @Nullable String getAssignedUserEmail();
    
    String getSubject();
    TaskPriority getPriority();

    List<String> getKeyWords();
    List<TaskLink> getTaskLinks();
    Set<String> getAssignedRoles();
  }
  
  
  @JsonSerialize(as = ImmutableTaskComment.class)
  @JsonDeserialize(as = ImmutableTaskComment.class)
  @Value.Immutable
  interface TaskComment {
    // null on new
    @Nullable Long getId();
    @Nullable ZonedDateTime getCreated();
    

    @Nullable Boolean getExternal();
    @Nullable String getUserName();
    @Nullable Long getReplyToId();
    
    Long getTaskId();
    String getCommentText();
    TaskCommentSource getSource(); 
  }

}
