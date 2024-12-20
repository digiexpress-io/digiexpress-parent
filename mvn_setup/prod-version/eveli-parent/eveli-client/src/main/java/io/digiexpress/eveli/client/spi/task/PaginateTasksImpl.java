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

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.PaginateTasks;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.api.TaskClient.TaskPriority;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimQueryActions.MissionQuery;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class PaginateTasksImpl implements PaginateTasks, TaskStoreConfig.QueryTasksVisitor<Page<TaskClient.Task>> {

  private final TaskStore ctx;
  
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
  public Uni<Page<Task>> findAll() {
    return ctx.getConfig().accept(this);
  }
  
  @Override
  public MissionQuery start(GrimStructuredTenant config, MissionQuery builder) {
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
      return
          /*
          .addAssignment(TaskMapper.ASSIGNMENT_TYPE_TASK_USER, false, assignedUser)
          .likeReporterId(clientIdentificator)
          .likeTitle(subject)
          .likeDescription("the bEst")
          .status(status)
          .priority(priority)
          .overdue(dueDate == null ? dueDate.isEmpty() : false) // do not return overdue tasks
          */
          
          
          
          taskRepository.searchTasks(
          likeExpression(subject), likeExpression(clientIdentificator), likeExpression(assignedUser),
          statuses, 
          priorities, requireAnyRoles, likeExpression(role), dueDate, pageable)
          .map(PaginateTasksImpl::map);
    }
    return null;
  }
  @Override
  public List<GrimMissionContainer> visitEnvelope(GrimStructuredTenant config, QueryEnvelopeList<GrimMissionContainer> envelope) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public Uni<List<Task>> end(GrimStructuredTenant config, List<GrimMissionContainer> commit) {
    // TODO Auto-generated method stub
    return null;
  }
  
}
