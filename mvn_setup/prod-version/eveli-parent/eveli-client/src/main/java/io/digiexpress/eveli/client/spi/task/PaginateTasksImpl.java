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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.digiexpress.eveli.client.api.ImmutableTask;
import io.digiexpress.eveli.client.api.TaskClient.PaginateTasks;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.api.TaskClient.TaskPriority;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.digiexpress.eveli.client.persistence.entities.TaskEntity;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class PaginateTasksImpl implements PaginateTasks {

  private final TaskRepository taskRepository;
  
  private Pageable pageable;
  private String subject = ""; 
  private String clientIdentificator = "";
  private String assignedUser = "";
  private String role = "";
  
  private String dueDate;
  private List<TaskStatus> status = new ArrayList<>();
  private List<TaskPriority> priority = new ArrayList<>();
  private List<String> requireAnyRoles;
  

  @Override
  public PaginateTasks page(Pageable pageable) {
    this.pageable = pageable;
    return this;
  }
  @Override
  public PaginateTasks subject(String subject) {
    this.subject = subject;
    return this;
  }
  @Override
  public PaginateTasks clientIdentificator(String clientIdentificator) {
    this.clientIdentificator = clientIdentificator;
    return this;
  }
  @Override
  public PaginateTasks assignedUser(String assignedUser) {
    this.assignedUser = assignedUser;
    return this;
  }
  @Override
  public PaginateTasks role(String role) {
    this.role = role;
    return this;
  }

  @Override
  public PaginateTasks status(List<TaskStatus> status) {
    if(status != null) {
      this.status.addAll(status);
    }
    return this;
  }
  @Override
  public PaginateTasks priority(List<TaskPriority> priority) {
    if(priority != null) {
      this.priority.addAll(priority);
    }
    return this;
  }
  @Override
  public PaginateTasks dueDate(String dueDate) {
    this.dueDate = dueDate;
    return this;
  }
  @Override
  public PaginateTasks requireAnyRoles(List<String> requireAnyRoles) {
    this.requireAnyRoles = requireAnyRoles;
    return this;
  }

  @Override
  public Page<Task> findAll() {
    TaskAssert.notEmpty("pageable", () -> "pageable can't be null!");
    if (this.status.isEmpty()) {
      this.status.addAll(Arrays.asList(TaskStatus.values()));
    }
    if (priority.isEmpty()) {
      this.priority.addAll(Arrays.asList(TaskPriority.values()));
    }
    final var statuses = this.status.stream().map(el-> el.ordinal()).collect(Collectors.toList());
    final var priorities = this.priority.stream().map(el-> el.ordinal()).collect(Collectors.toList());

    if (requireAnyRoles == null) {
      return taskRepository.searchTasksAdmin(
          likeExpression(subject), likeExpression(clientIdentificator), likeExpression(assignedUser),
          statuses, priorities, likeExpression(role), dueDate, pageable)
          .map(PaginateTasksImpl::map);
    } else {
      return taskRepository.searchTasks(
          likeExpression(subject), likeExpression(clientIdentificator), likeExpression(assignedUser),
          statuses, 
          priorities, requireAnyRoles, likeExpression(role), dueDate, pageable)
          .map(PaginateTasksImpl::map);
    }
  }
  
  private String likeExpression(String value) {
    return "%" + value.toLowerCase() + "%";
  }
  
  
  public static Task map(TaskEntity task) {
    return ImmutableTask.builder()
      .version(task.getVersion())
      .assignedUser(task.getAssignedUser())
      .assignedUserEmail(task.getAssignedUserEmail())
      .clientIdentificator(task.getClientIdentificator())
      .completed(task.getCompleted())
      .created(task.getCreated())
      .description(task.getDescription())
      .dueDate(task.getDueDate())
      .id(task.getId())
      .questionnaireId(task.getQuestionnanireId())
      .priority(task.getPriority())
      .status(task.getStatus())
      .subject(task.getSubject())
      .taskRef(task.getTaskRef())
      .updated(task.getUpdated())
      .updaterId(task.getUpdaterId())
      .build();
  }
  
}
