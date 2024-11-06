package io.digiexpress.eveli.client.spi.task;

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

import java.util.Optional;

import io.digiexpress.eveli.client.api.ImmutableTaskComment;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.CreateTaskCommentCommand;
import io.digiexpress.eveli.client.api.TaskClient.TaskComment;
import io.digiexpress.eveli.client.event.TaskNotificator;
import io.digiexpress.eveli.client.persistence.entities.TaskCommentEntity;
import io.digiexpress.eveli.client.persistence.repositories.CommentRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskAccessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import io.digiexpress.eveli.client.web.resources.worker.TaskControllerBase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateOneTaskComment {
  private final String userId;
  private final String email;
  private final TaskRepository taskRepository;
  private final CommentRepository commentRepository;
  private final TaskNotificator notificator;
  private final TaskAccessRepository taskAccessRepository;
  
  public TaskClient.TaskComment create(CreateTaskCommentCommand command) {

    final var task = taskRepository.getOneById(command.getTaskId());
    final var replyTo = Optional.ofNullable(command.getReplyToId()).map(replyToId -> commentRepository.findById(replyToId).get());
    
    final var entity = new TaskCommentEntity()
      .setCommentText(command.getCommentText())
      .setUserName(userId)
      .setExternal(command.getExternal())
      .setSource(command.getSource())
      .setTask(task)
      .setReplyTo(replyTo.orElse(null));

    
    final var savedComment = commentRepository.save(entity);
    final var immutable = CreateOneTaskComment.map(savedComment);
    
    // TODO
    new TaskControllerBase(taskAccessRepository).registerUserTaskAccess(task.getId(), Optional.of(task), userId);
    
    // TODO
    if (savedComment.getExternal()) {
      final var taskModel = PaginateTasksImpl.map(task);
      notificator.sendNewCommentNotificationToClient(immutable, taskModel);
    }
    return immutable;
  }
  
  
  public static TaskComment map(io.digiexpress.eveli.client.persistence.entities.TaskCommentEntity task) {
    return ImmutableTaskComment.builder()
        .id(task.getId())
        .created(task.getCreated())
        .commentText(task.getCommentText())
        .userName(task.getUserName())
        .replyToId(Optional.ofNullable(task.getReplyTo()).map(r -> r.getId()).orElse(null)) // probably bad idea, lazy relations
        .taskId(task.getTask().getId()) // probably bad idea, lazy relations
        .external(task.getExternal())
        .source(task.getSource())
        .build();
  }
}
