package io.digiexpress.eveli.client.event;

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

import java.util.List;
import java.util.Set;

import io.digiexpress.eveli.client.api.TaskCommands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TaskEvent {
  
  public static enum TaskEventType {
    TASK_SAVE,
    TASK_COMMENT_SAVE,
    TASK_STATUS_CHANGE,
    TASK_GROUP_ASSIGNMENT_CHANGE,
    TASK_USER_ASSIGNMENT_CHANGE,
    TASK_USER_EMAIL_CHANGE,
  }
  
  private String eventId;
  
  @Singular
  private List<TaskEventType> taskEventTypes;
  // new task data
  private TaskCommands.Task task;
  // added comment
  private TaskCommands.TaskComment comment;
  
  // if task status change then previous status
  private TaskCommands.TaskStatus previousStatus;
  
  // if group assignment change then indicates previous roles
  private Set<String> previousAssignedRoles;
  // added groups
  private Set<String> addedAssignedRoles;
  // if user assignment change then indicates previous user
  private String previousAssignedUser;
  // if user email change then indicates previous email
  private String previousUserEmail;
  
}
