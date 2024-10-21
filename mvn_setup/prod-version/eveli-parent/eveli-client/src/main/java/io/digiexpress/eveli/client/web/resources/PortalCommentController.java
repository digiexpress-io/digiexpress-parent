package io.digiexpress.eveli.client.web.resources;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.digiexpress.eveli.client.api.PortalClient;
import io.digiexpress.eveli.client.api.TaskCommands;
import io.digiexpress.eveli.client.iam.PortalAccessValidator;
import io.digiexpress.eveli.client.persistence.entities.TaskEntity;
import io.digiexpress.eveli.client.persistence.repositories.CommentRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskAccessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import io.digiexpress.eveli.client.spi.TaskCommandsImpl;
import lombok.extern.slf4j.Slf4j;

@RestController
@Transactional
@Slf4j
public class PortalCommentController extends TaskControllerBase
{
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final PortalAccessValidator validator;
    
    public PortalCommentController(
        TaskRepository taskRepository, CommentRepository commentRepository, 
        TaskAccessRepository taskAccessRepository, PortalClient client, PortalAccessValidator validator) {
      
      super(taskAccessRepository);
      this.taskRepository = taskRepository;
      this.commentRepository = commentRepository;
      this.validator = validator;
    }
    
    @GetMapping(value="/task/{id}/comments")
    public ResponseEntity<List<TaskCommands.TaskComment>> getTaskComments(@PathVariable("id") Long id,
        @AuthenticationPrincipal Jwt principal) 
    {
      final var authentication = SecurityContextHolder.getContext().getAuthentication();
      log.debug("Task comments get: id: {}, user id: {}", id, authentication.getName());
      validator.validateTaskAccess(id, principal);
      final var task = taskRepository.findById(id);
      registerTaskAccess(id, authentication, task);
      
      final var comments = commentRepository.findByTaskId(id).stream().map(TaskCommandsImpl::map).toList();
       
      return new ResponseEntity<>(comments, HttpStatus.OK);
    }
    
    @GetMapping(value="/task/{id}/externalComments")
    public ResponseEntity<List<TaskCommands.TaskComment>> getTaskExternalComments(
        @PathVariable("id") Long id,
        @RequestParam("userId") String userId,
        @AuthenticationPrincipal Jwt principal) 
    {
      validator.validateTaskAccess(id, principal);
      final var task = taskRepository.findById(id);
      registerUserTaskAccess(id, task, userId);
      
      final var comments = commentRepository.findByTaskIdAndExternalTrue(id).stream().map(TaskCommandsImpl::map).toList();

      return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    
    @GetMapping("/comment/{id}")
    public ResponseEntity<TaskCommands.TaskComment> getCommentById(@PathVariable("id") Long id,
        @AuthenticationPrincipal Jwt principal) 
    {
      validator.validateTaskAccess(id, principal);
      return commentRepository.findById(id) 
          .map(TaskCommandsImpl::map) 
          .map(ResponseEntity::ok) 
          .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/externalComment")
    public ResponseEntity<TaskCommands.TaskComment> createExternalComment(
        @RequestBody TaskCommands.TaskComment comment,
        @RequestParam("userId") String userId,
        @AuthenticationPrincipal Jwt principal) 
    {
      validator.validateTaskAccess(comment.getTaskId(), principal);
      
      final var task = getCommentTask(comment);
      final var entity = TaskCommandsImpl.map(comment).setTask(task);
      
      String userName = getUserName(principal);
      if (!StringUtils.isBlank(userName)) {
        // use name from principal if given to ensure that no incorrect name can be given. 
        entity.setUserName(userName);
      }
      else if (StringUtils.isBlank(userId)) {
        // portal access, user name in comment data, id should be present
        log.warn("Task external comment without provided user id, saving with user data from comment: {}", entity.getUserName());
      }
      
      final var savedComment = commentRepository.save(entity);
      registerUserTaskAccess(task.getId(), Optional.of(task), userId);

      return new ResponseEntity<>(TaskCommandsImpl.map(savedComment), HttpStatus.CREATED);
    }

    
    private TaskEntity getCommentTask(TaskCommands.TaskComment comment) {
      final var commentTask = taskRepository.findById(comment.getTaskId());
      if (!commentTask.isPresent()) {
        throw new ResponseStatusException(
            HttpStatus.UNPROCESSABLE_ENTITY, "Task id incorrect");
      }
      return commentTask.get();
    }
    
    @GetMapping("/comment/{id}/replyTo")
    public ResponseEntity<TaskCommands.TaskComment> getCommentReplyTo(@PathVariable("id") Long id,
        @AuthenticationPrincipal Jwt principal) 
    {
      final var model = commentRepository.findByReplyTo(id).map(TaskCommandsImpl::map);
      
      validator.validateTaskAccess(model.map(comment-> comment.getTaskId()).orElse(null), principal);
      return model 
          .map(ResponseEntity::ok) 
          .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/comment/{id}/task")
    public ResponseEntity<TaskCommands.Task> getCommentTask(@PathVariable("id") Long id,
        @AuthenticationPrincipal Jwt principal) 
    {
      final var commentEntity = commentRepository.findById(id);
      validator.validateTaskAccess(commentEntity.map(comment->comment.getTask().getId()).orElse(null), principal);
      return commentEntity
          .map(comment-> comment.getTask())
          .map(TaskCommandsImpl::map)
          .map(ResponseEntity::ok) 
          .orElse(ResponseEntity.notFound().build());
    }
}