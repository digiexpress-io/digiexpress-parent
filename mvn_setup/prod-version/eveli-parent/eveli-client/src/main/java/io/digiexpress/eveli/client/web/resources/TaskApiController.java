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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
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
import io.digiexpress.eveli.client.api.AuthClient.UserType;
import io.digiexpress.eveli.client.api.TaskCommands;
import io.digiexpress.eveli.client.api.TaskCommands.TaskPriority;
import io.digiexpress.eveli.client.api.TaskCommands.TaskStatus;
import io.digiexpress.eveli.client.event.TaskNotificator;
import io.digiexpress.eveli.client.persistence.entities.TaskEntity;
import io.digiexpress.eveli.client.persistence.entities.TaskRefGenerator;
import io.digiexpress.eveli.client.persistence.repositories.TaskAccessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import io.digiexpress.eveli.client.spi.TaskCommandsImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/tasks/v1")
@Transactional
@Slf4j
/*
 * Task controller for frontdesk UI
 */
public class TaskApiController extends TaskControllerBase
{
    private final TaskRepository taskRepository;
    private final JdbcTemplate jdbcTemplate;
    private final TaskNotificator notificator;
    private final boolean adminsearch;
    private final AuthClient securityClient;
    private final TaskRefGenerator taskRefGenerator;

    public TaskApiController(
        TaskRefGenerator taskRefGenerator,
        TaskAccessRepository taskAccessRepository, 
        TaskRepository taskRepository, 
        TaskNotificator notificator, 
        JdbcTemplate jdbcTemplate,
        boolean adminsearch,
        AuthClient securityClient) 
    {
      super(taskAccessRepository);
      this.taskRepository = taskRepository;
      this.jdbcTemplate = jdbcTemplate;
      this.notificator = notificator;
      this.adminsearch = adminsearch;
      this.securityClient = securityClient;
      this.taskRefGenerator = taskRefGenerator;
    }
    
    @GetMapping("/taskSearch")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<TaskCommands.Task>> taskSearch(
        @RequestParam(name="subject", defaultValue="") String subject, 
        @RequestParam(name="clientIdentificator", defaultValue="") String clientIdentificator, 
        @RequestParam(name="assignedUser", defaultValue="") String assignedUser, 
        @RequestParam(name="status", required=false) List<TaskStatus> status,
        @RequestParam(name="priority", required=false) List<TaskPriority> priority,
        @RequestParam(name="assignedRoles", defaultValue="") String searchRole,
        @RequestParam(name="dueDate", required=false) String dueDate,
        Pageable pageable) {
      
      final var authentication = securityClient.getWorker();
      log.info("Task search: subject: {}, clientIdentificator: {}, assignedUser: {}, status: {}, priority: {}, assignedRoles: {}, dueDate: {}, by user id: {}", 
          subject, clientIdentificator, assignedUser, status, priority, searchRole, dueDate, authentication.getPrincipal().getUsername());
      
      List<String> roles = authentication.getPrincipal().getRoles();
      List<TaskStatus> statusValues = status;
      List<TaskPriority> priorityValues = priority;
      
      if (statusValues == null || statusValues.isEmpty()) {
        statusValues = new ArrayList<>();
        for (final var val: TaskStatus.values()) {
          statusValues.add(val);
        }
      }
      if (priorityValues == null || priorityValues.isEmpty()) {
        priorityValues = new ArrayList<>();
        for (final var val: TaskPriority.values()) {
          priorityValues.add(val);
        }
      }
      
      final var statuses = statusValues.stream().map(el-> el.ordinal()).collect(Collectors.toList());
      final var priorities = priorityValues.stream().map(el-> el.ordinal()).collect(Collectors.toList());
      
      final Page<TaskCommands.Task> tasks;
      if (adminsearch) {
        tasks = taskRepository.searchTasksAdmin(
            likeExpression(subject), likeExpression(clientIdentificator), likeExpression(assignedUser),
            statuses, priorities, likeExpression(searchRole), dueDate, pageable)
            .map(TaskCommandsImpl::map);
      } else {
        tasks = taskRepository.searchTasks(
            likeExpression(subject), likeExpression(clientIdentificator), likeExpression(assignedUser),
            statuses, 
            priorities, roles, likeExpression(searchRole), dueDate, pageable)
            .map(TaskCommandsImpl::map);
      }
      
      return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    private String likeExpression(String value) {
      return "%" + value.toLowerCase() + "%";
    }


    
    @GetMapping("/task/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<TaskCommands.Task> getTaskById(@PathVariable("id") Long id) 
    {
      final var authentication = securityClient.getWorker();
      log.info("Task get: id: {}, user id: {}", id, authentication.getPrincipal().getUsername());
      Optional<TaskEntity> result;
      
      if (authentication.getType() == UserType.AUTH && !adminsearch) {
        List<String> roles = authentication.getPrincipal().getRoles();
        log.info("User is authenticated with roles: {}", roles);
        result = taskRepository.findByIdAndAssignedRolesIn(id, roles);
      }
      else {
        log.info("Unauthenticated request or admin({}), no role check", adminsearch);
        result = taskRepository.findById(id);
      }
      return result
          .map(TaskCommandsImpl::map) 
          .map(ResponseEntity::ok) 
          .orElse(ResponseEntity.notFound().build());
    }

    
    @PostMapping(path = "/task/")
    @Transactional
    public ResponseEntity<TaskCommands.Task> createTask(
        @RequestBody TaskCommands.Task task) {
      
      final var authentication = securityClient.getWorker();
      String userName = authentication.getPrincipal().getUsername();
      log.info("Task post: user id: {}", userName);
      
      
      final var savedTask = taskRepository.save(TaskCommandsImpl.map(task)
          .setUpdaterId(userName)
          .setTaskRef(taskRefGenerator.generateTaskRef()));
      registerTaskAccess(savedTask.getId(), authentication.getPrincipal(), Optional.of(savedTask));
      
      final var model = TaskCommandsImpl.map(savedTask);
      
      notificator.handleTaskCreation(model, userName);
      
      
      return new ResponseEntity<>(model, HttpStatus.CREATED);
    }
    
    @PutMapping("/task/{id}")
    @Transactional
    public ResponseEntity<TaskCommands.Task> saveTask(@PathVariable("id") Long id, 
        @RequestBody TaskCommands.Task task) {
      if (id.compareTo(task.getId()) != 0) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
      }
      final var authentication = securityClient.getWorker();
      final var userName = authentication.getPrincipal().getUsername();
      final var email = authentication.getPrincipal().getEmail();

      log.info("Task put: id: {}, user id: {}", id, userName);
      
      final var savedTask = taskRepository.findById(id);
      if (savedTask.isPresent()) {
        final var t = savedTask.get();
        
        final var previousVersion = TaskCommandsImpl.map(t);
        
        t.setAssignedUser(task.getAssignedUser());
        t.setAssignedUserEmail(task.getAssignedUserEmail());
        t.setCompleted(task.getCompleted());
        t.setDescription(task.getDescription());
        t.setDueDate(task.getDueDate());
        t.setPriority(task.getPriority());
        t.setStatus(task.getStatus());
        t.setSubject(task.getSubject());
        t.setVersion(task.getVersion());
        t.setClientIdentificator(task.getClientIdentificator());
        t.setAssignedRoles(task.getAssignedRoles());
        t.setUpdaterId(userName);
        
        TaskEntity saved = taskRepository.save(t);
        registerTaskAccess(id, authentication.getPrincipal(), savedTask);
        notificator.handleTaskUpdate(task, previousVersion, email);
        
        return new ResponseEntity<>(TaskCommandsImpl.map(saved), HttpStatus.OK);
      }
      else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }
    }

    @DeleteMapping("/task/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable("id") Long id) {
      final var authentication = securityClient.getWorker();
      log.info("Task delete: id: {}, user id: {}", id, authentication.getPrincipal().getUsername());
      taskRepository.deleteById(id);
    }
    
    @GetMapping(value="/tasksUnread")
    @Transactional(readOnly = true)
    public ResponseEntity<Collection<Long>> getUnreadTasks() 
    {
      final var authentication = securityClient.getWorker();
      log.info("Task unread request: user id: {}", authentication.getPrincipal().getUsername());
      List<Long> taskIds = new ArrayList<>();
      
      if (adminsearch) {
        Iterable<Long> accesses = taskRepository.findUnreadTasks(authentication.getPrincipal().getUsername());
        accesses.forEach(access->taskIds.add(access));
      } else {
        List<String> roles = authentication.getPrincipal().getRoles();
        Iterable<Long> accesses = taskRepository.findUnreadTasksByRole(authentication.getPrincipal().getUsername(), roles);
        accesses.forEach(access->taskIds.add(access));
      }
    
      return new ResponseEntity<>(taskIds,HttpStatus.OK);
    }
    
    
    @Data
    @AllArgsConstructor
    private static class KeyWordsResponse {
      List<String> keyWords;
    }
    
    @GetMapping("/task-keywords")
    @Transactional(readOnly = true)
    public ResponseEntity<KeyWordsResponse> getKeyWords() 
    {
      final var authentication = securityClient.getWorker();
      log.info("Task keyword request: user id: {}", authentication.getPrincipal().getUsername());
      try {
        List<String> result = new ArrayList<>();
        jdbcTemplate.query(
            "SELECT distinct key_words from task_keywords order by 1",
            (rs, rowNum) -> rs.getString(1)
        ).forEach(keyword -> result.add(keyword));
        return ResponseEntity.status(HttpStatus.OK).body(new KeyWordsResponse(result));
      }
      catch (Exception e) {
        log.error("Error in keyword reading", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
      }
    }

 
}
