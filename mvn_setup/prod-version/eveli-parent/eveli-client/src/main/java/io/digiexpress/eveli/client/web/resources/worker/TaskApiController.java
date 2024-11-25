package io.digiexpress.eveli.client.web.resources.worker;

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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.api.TaskClient.TaskPriority;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.digiexpress.eveli.client.persistence.repositories.TaskAccessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/*
 * Task controller for frontdesk UI
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/worker/rest/api/tasks")
@Slf4j
public class TaskApiController {    
  private final AuthClient securityClient;
  private final TaskClient taskClient;
  
  private final TaskAccessRepository taskAccessRepository;
  private final TaskRepository taskRepository;
  
  
  @GetMapping
  @Transactional(readOnly = true)
  public ResponseEntity<Page<Task>> taskSearch(
      @RequestParam(name="subject", defaultValue = "") String subject, 
      @RequestParam(name="clientIdentificator", defaultValue = "") String clientIdentificator, 
      @RequestParam(name="assignedUser", defaultValue = "") String assignedUser, 
      @RequestParam(name="assignedRoles", defaultValue = "") String searchRole,
      @RequestParam(name="dueDate", required = false) String dueDate,
      @RequestParam(name="status", required = false) List<TaskStatus> status,
      @RequestParam(name="priority", required = false) List<TaskPriority> priority,
      Pageable pageable) {
    
    final var worker = securityClient.getUser();
    
    final var query = taskClient.paginateTasks()
        .subject(subject)
        .clientIdentificator(clientIdentificator)
        .assignedUser(assignedUser)
        .role(searchRole)
        .dueDate(dueDate)
        .status(status)
        .priority(priority)
        .page(pageable);
    
    if (worker.getPrincipal().isAdmin()) {
      return ResponseEntity.ok(query.findAll());
    }
    return ResponseEntity.ok(query.requireAnyRoles(worker.getPrincipal().getRoles()).findAll());
  }

  @GetMapping("/{id}")
  @Transactional(readOnly = true)
  public ResponseEntity<Task> getTaskById(@PathVariable("id") Long id) {
    
    final var worker = securityClient.getUser();
    final var task = taskClient.queryTasks().getOneById(id);
    
    
    if (worker.getPrincipal().isAdmin()) {
      return ResponseEntity.ok(task);
    }
  
    final var isWorkerInAssignedRoles = worker.getPrincipal().isAccessGranted(task.getAssignedRoles());
    if(isWorkerInAssignedRoles) {
      return ResponseEntity.ok(task);
    }
    
    // alarm clocks
    return ResponseEntity.status(403).build();
  }

  @PostMapping
  @Transactional
  public ResponseEntity<TaskClient.Task> createTask(@RequestBody TaskClient.CreateTaskCommand command) {
    final var worker = securityClient.getUser().getPrincipal();
    final var newTask = taskClient.commandTaskBuilder()
        .userId(worker.getUsername(), worker.getEmail())
        .createTask(command);
    return new ResponseEntity<>(newTask, HttpStatus.CREATED);
  }
  
  @PutMapping("/{id}")
  @Transactional
  public ResponseEntity<TaskClient.Task> saveTask(@PathVariable("id") Long id, @RequestBody TaskClient.ModifyTaskCommand command) {
    final var worker = securityClient.getUser().getPrincipal();
    final var modifiedTask = taskClient.commandTaskBuilder()
        .userId(worker.getUsername(), worker.getEmail())
        .modifyTask(id, command);
    return new ResponseEntity<>(modifiedTask, HttpStatus.OK);

  }

  @DeleteMapping("/{id}")
  @Transactional
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteTask(@PathVariable("id") Long id) {
    final var worker = securityClient.getUser().getPrincipal();
    taskClient.commandTaskBuilder()
        .userId(worker.getUsername(), worker.getEmail())
        .deleteTask(id);
  }
  
  @GetMapping(value="/unread")
  public ResponseEntity<Collection<Long>> getUnreadTasks() {
    final var worker = securityClient.getUser().getPrincipal();
    
    if (worker.isAdmin()) {
      return ResponseEntity.ok(taskClient.queryUnreadUserTasks()
          .userId(worker.getUsername())
          .findAll());
    } 
    return ResponseEntity.ok(taskClient.queryUnreadUserTasks()
        .userId(worker.getUsername())
        .requireAnyRoles(worker.getRoles())
        .findAll());
  }
  
  @GetMapping(value="/{id}/comments")
  public ResponseEntity<List<TaskClient.TaskComment>> getTaskComments(@PathVariable("id") Long id)
  {
    final var authentication = securityClient.getUser();
    new TaskControllerBase(taskAccessRepository).registerUserTaskAccess(id, Optional.of(taskRepository.getOneById(id)), authentication.getPrincipal().getUsername());
    final var comments = taskClient.queryComments().findAllByTaskId(id);
 
    return new ResponseEntity<>(comments, HttpStatus.OK);
  }
  
  @Data
  @AllArgsConstructor
  private static class KeyWordsResponse { List<String> keyWords; }
  
  @GetMapping("/keywords")
  @Transactional(readOnly = true)
  public ResponseEntity<KeyWordsResponse> getKeyWords() {
    return ResponseEntity.ok(new KeyWordsResponse(taskClient.queryKeywords().findAllKeywords()));
  }
}
