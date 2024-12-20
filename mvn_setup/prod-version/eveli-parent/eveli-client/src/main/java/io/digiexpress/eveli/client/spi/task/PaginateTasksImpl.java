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
import org.springframework.data.domain.PageImpl;
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
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
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
  
  /**

  @Query(value=
      "select distinct t from TaskEntity t join t.assignedRoles r left join t.assignedRoles r2 where " +
      " (lower(subject) like :subject or lower(taskRef) like :subject)" +
      " and lower(coalesce(clientIdentificator, '')) like :clientIdentificator" +
      " and lower(coalesce(assignedUser, '')) like :assignedUser" +
      " and priority in :priority" +
      " and status in :status" +
      " and lower(coalesce(r2, '')) like :searchRole" +
      " and r in :roles" +
      " and (:dueDate is null or t.dueDate < CURRENT_DATE)")
  Page<TaskEntity> searchTasks(
      @Param("subject") String subject, 
      @Param("clientIdentificator") String clientIdentificator, 
      @Param("assignedUser") String assignedUser, 
      @Param("status") List<Integer> status,
      @Param("priority") List<Integer> priority,
      @Param("roles") List<String> roles,
      @Param("searchRole") String searchRole,
      @Param("dueDate") String dueDate,
      Pageable page);
  
  @Query(value=
      "select distinct t from TaskEntity t left join t.assignedRoles r2 where " +
      " (lower(subject) like :subject or lower(taskRef) like :subject)" +
      " and lower(coalesce(clientIdentificator, '')) like :clientIdentificator" +
      " and lower(coalesce(assignedUser, '')) like :assignedUser" +
      " and priority in :priority" +
      " and status in :status" +
      " and lower(coalesce(r2, '')) like :searchRole" +
      " and (:dueDate is null or t.dueDate < CURRENT_DATE)")
  Page<TaskEntity> searchTasksAdmin(
      @Param("subject") String subject,  
      @Param("clientIdentificator") String clientIdentificator,
      @Param("assignedUser") String assignedUser,
      @Param("status") List<Integer> status,
      @Param("priority") List<Integer> priority,
      @Param("searchRole") String searchRole,
      @Param("dueDate") String dueDate,
      Pageable page);

   */
  @Override
  public MissionQuery start(GrimStructuredTenant config, MissionQuery builder) {
    TaskAssert.notEmpty("pageable", () -> "pageable can't be null!");
    if (this.status.isEmpty()) {
      this.status.addAll(Arrays.asList(TaskStatus.values()));
    }
    if (priority.isEmpty()) {
      this.priority.addAll(Arrays.asList(TaskPriority.values()));
    }
    final var statuses = this.status.stream().map(el-> el.name()).collect(Collectors.toList());
    final var priorities = this.priority.stream().map(el-> el.name()).collect(Collectors.toList());

    if (requireAnyRoles == null) {
      return builder
      .addAssignment(TaskMapper.ASSIGNMENT_TYPE_TASK_USER, false, assignedUser)
      .addAssignment(TaskMapper.ASSIGNMENT_TYPE_TASK_ROLE, false, role)
      .likeReporterId(clientIdentificator)
      .likeTitle(subject)
      .likeDescription("the bEst")
      .status(statuses)
      .priority(priorities)
      .overdue(dueDate == null ? !dueDate.isEmpty() : false) // do not return overdue tasks
      ;
    }
    return builder
      .addAssignment(TaskMapper.ASSIGNMENT_TYPE_TASK_USER, false, assignedUser)
      .addAssignment(TaskMapper.ASSIGNMENT_TYPE_TASK_ROLE, false, role)
      .addAssignment(TaskMapper.ASSIGNMENT_TYPE_TASK_ROLE, false, requireAnyRoles)
      .likeReporterId(clientIdentificator)
      .likeTitle(subject)
      .likeDescription("the bEst")
      .status(statuses)
      .priority(priorities)
      .overdue(dueDate == null ? !dueDate.isEmpty() : false) // do not return overdue tasks
      ;
  }
  @Override
  public List<GrimMissionContainer> visitEnvelope(GrimStructuredTenant config, QueryEnvelopeList<GrimMissionContainer> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw TaskException.builder("PAGINATE_TASKS_FAIL")
        .add(config, envelope)

        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw TaskException.builder("PAGINATE_TASKS_NOT_FOUND")   
        .add(config, envelope)
        .build();
    }
    return result;
  }
  @Override
  public Uni<Page<Task>> end(GrimStructuredTenant config, List<GrimMissionContainer> commit) {
    final var tasks = commit.stream()
        .map(container -> TaskMapper.map(
            container.getMission(), 
            container.getAssignments().values(), 
            container.getRemarks().values()))
        .toList();
    
    final Page<Task> page = new PageImpl<Task>(tasks);
    
    return Uni.createFrom().item((Object) page)
        .map(e -> (Page<Task>) e);
  }
  
}
