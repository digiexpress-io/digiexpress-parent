package io.digiexpress.eveli.client.web.resources;

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
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.digiexpress.eveli.client.api.TaskCommands;
import io.digiexpress.eveli.client.event.TaskNotificator;
import io.digiexpress.eveli.client.persistence.repositories.CommentRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskAccessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import io.digiexpress.eveli.client.spi.TaskCommandsImpl;
import lombok.extern.slf4j.Slf4j;



@RestController
@Transactional
@RequestMapping("/api/tasks/v1")
@Slf4j
public class CommentApiController extends TaskControllerBase
{
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final TaskNotificator notificator;
    
    public CommentApiController(
        TaskRepository taskRepository, CommentRepository commentRepository, 
        TaskNotificator notificator, TaskAccessRepository taskAccessRepository) 
    {
      super(taskAccessRepository);
      this.taskRepository = taskRepository;
      this.commentRepository = commentRepository;
      this.notificator = notificator;
    }
    
    @GetMapping(value="/task/{id}/comments")
    public ResponseEntity<List<TaskCommands.TaskComment>> getTaskComments(@PathVariable("id") Long id) 
    {
      final var authentication = SecurityContextHolder.getContext().getAuthentication();
      log.info("Task comments get: id: {}, user id: {}", id, authentication.getName());
      final var task = taskRepository.findById(id);
      registerTaskAccess(id, authentication, task);
      final var comments = commentRepository.findByTaskId(id);
   
      return new ResponseEntity<>(comments.stream().map(TaskCommandsImpl::map).toList(), HttpStatus.OK);
    }

    
    @GetMapping("/comment/{id}")
    public ResponseEntity<TaskCommands.TaskComment> getCommentById(@PathVariable("id") Long id) 
    {
      return commentRepository.findById(id) 
          .map(TaskCommandsImpl::map) 
          .map(ResponseEntity::ok) 
          .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/comment")
    public ResponseEntity<TaskCommands.TaskComment> createComment(
        @RequestBody TaskCommands.TaskComment comment,
        @AuthenticationPrincipal Jwt principal) 
    {
      final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      final var entity = TaskCommandsImpl.map(comment);
      final var task = getCommentTask(comment);
      entity.setTask(task);

      if (comment.getReplyToId() != null) {
        final var replyComment = getReplyToComment(comment);
        entity.setReplyTo(replyComment);
      }
      String userName = getUserName(principal);
      entity.setUserName(userName);

      final var savedComment = commentRepository.save(entity);
      registerTaskAccess(task.getId(), authentication, Optional.of(task));
      
      final var commentModel = TaskCommandsImpl.map(savedComment);
      if (savedComment.getExternal()) {
        final var taskModel = TaskCommandsImpl.map(task);
        notificator.sendNewCommentNotificationToClient(commentModel, taskModel);
      }
      return new ResponseEntity<>(commentModel, HttpStatus.CREATED);
    }


    private io.digiexpress.eveli.client.persistence.entities.TaskEntity getCommentTask(TaskCommands.TaskComment comment) {
      final var commentTask = taskRepository.findById(comment.getTaskId());
      if (!commentTask.isPresent()) {
        throw new ResponseStatusException(
            HttpStatus.UNPROCESSABLE_ENTITY, "Task id incorrect");
      }
      return commentTask.get();
    }

    private io.digiexpress.eveli.client.persistence.entities.TaskCommentEntity getReplyToComment(TaskCommands.TaskComment comment) {
      final var replyComment = commentRepository.findById(comment.getReplyToId());
      if (!replyComment.isPresent()) {
        throw new ResponseStatusException(
            HttpStatus.UNPROCESSABLE_ENTITY, "Reply to id incorrect");
      }
      return replyComment.get();
    }
    
    @GetMapping("/comment/{id}/replyTo")
    public ResponseEntity<TaskCommands.TaskComment> getCommentReplyTo(@PathVariable("id") Long id) 
    {
      return commentRepository.findOneByReplyTo(id)
          .map(TaskCommandsImpl::map) 
          .map(ResponseEntity::ok) 
          .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/comment/{id}/task")
    public ResponseEntity<TaskCommands.Task> getCommentTask(@PathVariable("id") Long id) 
    {
      return commentRepository.findById(id)
          .map(comment-> TaskCommandsImpl.map(comment.getTask())) 
          .map(ResponseEntity::ok) 
          .orElse(ResponseEntity.notFound().build());
    }

}
