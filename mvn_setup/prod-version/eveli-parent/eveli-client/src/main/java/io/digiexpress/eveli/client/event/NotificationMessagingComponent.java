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

import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.event.TaskEvent.TaskEventType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotificationMessagingComponent implements TaskNotificator {
  
  @Autowired(required=false)
  private TaskEventPublisher publisher;
  
  @Override
  public void sendNewCommentNotificationToClient(TaskClient.TaskComment comment, TaskClient.Task taskModel) {
    if (publisher != null) {
      TaskEvent event = TaskEvent.builder().task(taskModel).comment(comment).taskEventType(TaskEventType.TASK_COMMENT_SAVE).build();
      log.info("Sending new comment event {}", event);
      publisher.publishEvent(event);
    }
    else {
      log.debug("Event handler for new comment {}, no publisher configured", comment);
    }
    
  }

  @Override
  public void handleTaskUpdate(TaskClient.Task newTask, TaskClient.Task previous, String currentUserEmail) {
    if (publisher != null) {
      var builder = TaskEvent.builder().task(newTask).taskEventType(TaskEventType.TASK_SAVE);
      if (newTask.getStatus() != previous.getStatus()) {
        builder = builder.taskEventType(TaskEventType.TASK_STATUS_CHANGE).previousStatus(previous.getStatus());
      }
      if (!StringUtils.equals(newTask.getAssignedUser(), previous.getAssignedUser())) {
        builder = builder.taskEventType(TaskEventType.TASK_USER_ASSIGNMENT_CHANGE).
            previousAssignedUser(previous.getAssignedUser());
      }
      if (!StringUtils.isBlank(newTask.getAssignedUserEmail())) {
        if (!StringUtils.equals(newTask.getAssignedUserEmail(), previous.getAssignedUserEmail()) &&
            !StringUtils.equals(currentUserEmail, newTask.getAssignedUserEmail())) {
          builder = builder.taskEventType(TaskEventType.TASK_USER_EMAIL_CHANGE).
              previousAssignedUser(previous.getAssignedUser()).previousUserEmail(previous.getAssignedUserEmail());
        }
      }
      else if (newTask.getAssignedRoles() != null) { 
        if(previous.getAssignedRoles() == null) {
          builder = builder.taskEventType(TaskEventType.TASK_GROUP_ASSIGNMENT_CHANGE).
              addedAssignedRoles(newTask.getAssignedRoles());
        }
        else if (!newTask.getAssignedRoles().equals(previous.getAssignedRoles())) {
          var addedRoles = new HashSet<>(newTask.getAssignedRoles());
          addedRoles.removeAll(previous.getAssignedRoles());
          builder = builder.taskEventType(TaskEventType.TASK_GROUP_ASSIGNMENT_CHANGE).
              previousAssignedRoles(previous.getAssignedRoles()).
              addedAssignedRoles(addedRoles);
        }
      }
      TaskEvent event = builder.build();
      log.info("Sending task update event {}", createEventLogValue(event));
      publisher.publishEvent(event);
    }
    else {
      log.debug("Event handler for task {} update, no pubsub configured", newTask);
    }
  }
  
  @Override
  public void handleTaskCreation(TaskClient.Task createdTask, String currentUserEmail) {
    if (publisher != null) {
      var builder = TaskEvent.builder().task(createdTask).taskEventType(TaskEventType.TASK_SAVE);
      if (!StringUtils.isBlank(createdTask.getAssignedUser())) {
        if (!StringUtils.equals(createdTask.getAssignedUser(), currentUserEmail)) {
          builder = builder.taskEventType(TaskEventType.TASK_USER_ASSIGNMENT_CHANGE);
        }
      }
      if (!StringUtils.isBlank(createdTask.getAssignedUserEmail())) {
        if (!StringUtils.equals(currentUserEmail, createdTask.getAssignedUserEmail())) {
          builder = builder.taskEventType(TaskEventType.TASK_USER_EMAIL_CHANGE);
        }
      }
      else if (createdTask.getAssignedRoles() != null) { 
        builder = builder.taskEventType(TaskEventType.TASK_GROUP_ASSIGNMENT_CHANGE).
            addedAssignedRoles(createdTask.getAssignedRoles());
      }
      TaskEvent event = builder.build();
      log.info("Sending task creation event {}", createEventLogValue(event));
      publisher.publishEvent(event);
    }
    else {
      log.debug("Event handler for task {} update, no pubsub configured", createdTask);
    }
  }

  private Object createEventLogValue(TaskEvent event) {
    StringBuilder builder = new StringBuilder("TaskEvent: ");
    builder.append(", eventTypes:").append(event.getTaskEventTypes());
    if (event.getTask() != null) {
      builder.append(", taskId:").append(event.getTask().getId());
      builder.append(", version:").append(event.getTask().getVersion());
      builder.append(", status:").append(event.getTask().getStatus());
      builder.append(", previousStatus:").append(event.getPreviousStatus());
      builder.append(", assignedUserEmail:").append(event.getTask().getAssignedUserEmail());
      builder.append(", previousAssigneUserEmail:").append(event.getPreviousUserEmail());
      builder.append(", assignedRoles:").append(event.getTask().getAssignedRoles());
      builder.append(", previouslyAssignedRoles:").append(event.getPreviousAssignedRoles());
      builder.append(", addedAssignedRoles:").append(event.getAddedAssignedRoles());
    }
    return builder.toString();
  }
  
}
