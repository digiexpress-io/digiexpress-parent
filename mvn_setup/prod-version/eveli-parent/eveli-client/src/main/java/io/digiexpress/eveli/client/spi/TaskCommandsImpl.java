package io.digiexpress.eveli.client.spi;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import io.digiexpress.eveli.client.api.ImmutableTask;
import io.digiexpress.eveli.client.api.ImmutableTaskComment;
import io.digiexpress.eveli.client.api.ImmutableTaskLink;
import io.digiexpress.eveli.client.api.TaskCommands;
import io.digiexpress.eveli.client.event.TaskNotificator;
import io.digiexpress.eveli.client.persistence.entities.TaskRefGenerator;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert.TaskException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TaskCommandsImpl implements TaskCommands {
  private final TaskRepository repository;
  private final TaskNotificator taskNotificator;
  private final TaskRefGenerator taskRefGenerator;
  @Override
  public TaskBuilder create() {
    return new TaskBuilder() {
      final ImmutableTask.Builder task = ImmutableTask.builder();
      
      @Override
      public TaskBuilder subject(String subject) {
        this.task.subject(subject);
        return this;
      }
      @Override
      public TaskBuilder questionnaireId(String questionnaireId) {
        this.task.addTaskLinks(ImmutableTaskLink.builder().linkKey("questionnaireId").linkAddress(questionnaireId).build());
        return this;
      }
      @Override
      public TaskBuilder priority(TaskPriority priority) {
        this.task.priority(priority);
        return this;
      }
      @Override
      public TaskBuilder label(String label) {
        if(label != null) {
          this.task.addKeyWords(label);
        }
        return this;
      }
      @Override
      public TaskBuilder description(String description) {
        this.task.description(description);
        return this;
      }
      @Override
      public TaskBuilder clientIdentificator(String clientIdentificator) {
        this.task.clientIdentificator(clientIdentificator);
        return this;
      }
      @Override
      public TaskBuilder assignedId(String assignedId) {
        if (assignedId != null && !assignedId.isEmpty()) {
          String[] roles = assignedId.split(";");
          for (String role : roles) {
            this.task.addAssignedRoles(role);
          }
        }
        return this;
      }
      @Override
      public TaskBuilder dueDate(LocalDate dueDate) {
        this.task.dueDate(dueDate);
        return this;
      }

      @Override
      public TaskBuilder status(TaskStatus status) {
        this.task.status(status);
        return this;
      }
      
      @Override
      public Task build() {
        final var taskModel = task.build();
        
        io.digiexpress.eveli.client.persistence.entities.TaskEntity entity = map(taskModel).setTaskRef(taskRefGenerator.generateTaskRef());
        io.digiexpress.eveli.client.persistence.entities.TaskEntity savedTask = repository.save(entity);
        
        final var model = map(savedTask);
        log.debug("Task command: created task with id {}", model.getId());
        taskNotificator.handleTaskCreation(model, model.getUpdaterId());
        log.debug("Task command: sent task creation notification for task {}", model.getId());
        
        final var builder = ImmutableTask.builder()
            .id(model.getId())
            .taskRef(model.getTaskRef())
            .assignedId(model.getAssignedUser())
            .assignedUser(model.getAssignedUser())
            .clientIdentificator(model.getClientIdentificator())
            .description(model.getDescription())
            .dueDate(model.getDueDate())
            .priority(model.getPriority())
            .status(model.getStatus())
            .subject(model.getSubject());
        if (model.getAssignedRoles() != null) {
          builder.addAllAssignedRoles(model.getAssignedRoles());
        }
        if (model.getKeyWords() != null) {
          builder.addAllKeyWords(model.getKeyWords());
        }
        return builder.build();
      }

    };
  }

  @Override
  public Optional<Task> find(String id, List<String> roles, boolean adminsearch) {
    if (StringUtils.isEmpty(id) || roles.isEmpty()) {
      return Optional.empty();
    }
    try {
      Long taskId = Long.parseLong(id);
      final var result = adminsearch ? repository.findById(taskId) : repository.findByIdAndAssignedRolesIn(taskId, roles);
      
      return result.map(task -> ImmutableTask.builder()
          .assignedUser(task.getAssignedUser())
          .assignedUserEmail(task.getAssignedUserEmail())
          .clientIdentificator(task.getClientIdentificator())
          .completed(task.getCompleted())
          .created(task.getCreated())
          .description(task.getDescription())
          .dueDate(task.getDueDate())
          .id(task.getId())
          .priority(task.getPriority())
          .status(task.getStatus())
          .subject(task.getSubject())
          .taskRef(task.getTaskRef())
          .updated(task.getUpdated())
          .updaterId(task.getUpdaterId())
          .taskLinks(map(task.getTaskLinks()))
          .build());
    }
    catch (Exception e) {
      throw new TaskException(e.getMessage(), e);
    }
  }


  @Override
  public void complete(String id) {
    setStatus(id, TaskStatus.COMPLETED);
    
  }
  @Override
  public void reject(String id) {
    setStatus(id, TaskStatus.REJECTED);
  }
  @Override
  public void delete(String id) {
    
    if (StringUtils.isEmpty(id)) {
      return;
    }
    try {
      Long taskId = Long.parseLong(id);
      repository.deleteById(taskId);
    }
    catch (Exception e) {
      throw new TaskException(e.getMessage(), e);
    }
  }
  
  
  private Task setStatus(String id, TaskStatus status) {
    if (StringUtils.isEmpty(id)) {
      return null;
    }
    try {
      Long taskId = Long.parseLong(id);
      final var result = repository.findById(taskId);
      if (result.isPresent()) {
        final var task = result.get();
        task.setStatus(status);
        final var savedTask = repository.save(task);
        return TaskCommandsImpl.map(savedTask);
      }
      return null;
    }
    catch (Exception e) {
      throw new TaskException(e.getMessage(), e);
    }
  }
  
  public static Task map(io.digiexpress.eveli.client.persistence.entities.TaskEntity task) {
    final var builder = ImmutableTask.builder()
      .assignedUser(task.getAssignedUser())
      .assignedUserEmail(task.getAssignedUserEmail())
      .clientIdentificator(task.getClientIdentificator())
      .completed(task.getCompleted())
      .created(task.getCreated())
      .description(task.getDescription())
      .dueDate(task.getDueDate())
      .id(task.getId())
      .priority(task.getPriority())
      .status(task.getStatus())
      .subject(task.getSubject())
      .taskRef(task.getTaskRef())
      .updated(task.getUpdated())
      .updaterId(task.getUpdaterId());
    
    if (task.getTaskLinks() != null) {
      task.getTaskLinks().forEach(link -> builder.addTaskLinks(
          ImmutableTaskLink.builder()
          .id(link.getId())
          .linkAddress(link.getLinkAddress())
          .linkKey(link.getLinkKey())
          .build()
      ));
    }
    
    return builder.build();
  }
  
  public static io.digiexpress.eveli.client.persistence.entities.TaskEntity map(Task task) {
    final var builder = new io.digiexpress.eveli.client.persistence.entities.TaskEntity()
      .setAssignedUser(task.getAssignedUser())
      .setAssignedUserEmail(task.getAssignedUserEmail())
      .setClientIdentificator(task.getClientIdentificator())
      .setCompleted(task.getCompleted())
      .setCreated(task.getCreated())
      .setDescription(task.getDescription())
      .setDueDate(task.getDueDate())
      .setId(task.getId())
      .setPriority(task.getPriority())
      .setStatus(task.getStatus())
      .setSubject(task.getSubject())
      .setTaskRef(task.getTaskRef())
      .setUpdated(task.getUpdated())
      .setUpdaterId(task.getUpdaterId())
      .setTaskLinks(new ArrayList<>());
    
    if (task.getTaskLinks() != null) {
      
      
      task.getTaskLinks().forEach(link -> builder.getTaskLinks().add(
          new io.digiexpress.eveli.client.persistence.entities.TaskLinkEntity()
          .setTask(builder)
          .setLinkAddress(link.getLinkAddress())
          .setLinkKey(link.getLinkKey())
      ));
    }
    
    return builder;
  }
  
  
private Collection<TaskLink> map(Collection<io.digiexpress.eveli.client.persistence.entities.TaskLinkEntity> taskLinks) {
  List<TaskLink> result = new ArrayList<>();
  if (taskLinks != null) {
    taskLinks.forEach(link -> {
      final var model = ImmutableTaskLink.builder()
      .id(link.getId())
      .linkAddress(link.getLinkAddress())
      .linkKey(link.getLinkKey()).build();
      result.add(model);
    });
  }
  return result;
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
  
  public static io.digiexpress.eveli.client.persistence.entities.TaskCommentEntity map(TaskComment task) {
    return new io.digiexpress.eveli.client.persistence.entities.TaskCommentEntity()
        .setId(task.getId())
        .setCreated(task.getCreated())
        .setCommentText(task.getCommentText())
        .setUserName(task.getUserName())
        .setExternal(task.getExternal())
        .setSource(task.getSource());
  }
}
