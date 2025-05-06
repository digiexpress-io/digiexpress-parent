package io.digiexpress.eveli.client.spi.task.visitors;

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
import org.springframework.data.domain.Sort.Order;

import io.digiexpress.eveli.client.api.TaskClient.PaginateTasks;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.api.TaskClient.TaskPriority;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.digiexpress.eveli.client.spi.task.TaskException;
import io.digiexpress.eveli.client.spi.task.TaskMapper;
import io.digiexpress.eveli.client.spi.task.TaskStore;
import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimQueryActions.GrimArchiveQueryType;
import io.resys.thena.api.actions.GrimQueryActions.MissionOrderByType;
import io.resys.thena.api.actions.GrimQueryActions.MissionQuery;
import io.resys.thena.api.entities.ImmutablePageQuery;
import io.resys.thena.api.entities.ImmutablePageSorting;
import io.resys.thena.api.entities.ImmutablePageSortingOrder;
import io.resys.thena.api.entities.PageQuery;
import io.resys.thena.api.entities.PageQuery.PageSortDirection;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopePage;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class PaginateTasksImpl implements PaginateTasks {

  private final TaskStore ctx;
  
  private Pageable pageable;
  private String subject = ""; 
  private String clientIdentificator = "";
  private String assignedUser = "";
  private String role = "";
  
  private String dueDate;
  private String additionalInfo;
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
  public PaginateTasks additionalInfo(String additionalInfo) {
    this.additionalInfo = additionalInfo;
    return this;
  }
  @Override
  public Uni<Page<Task>> findAll() {
    final var config = ctx.getConfig();
    final var grim = config.getClient().grim(config.getTenantName());
    final var prefilled = grim.find().missionQuery();
    
    final Uni<QueryEnvelopePage<GrimMissionContainer>> query = start(grim, prefilled)
        .paginate(pageQuery());
    return query
        .onItem().transform(envelope -> visitEnvelope(grim, envelope))
        .onItem().transformToUni(ref -> end(grim, ref));
  }
  
  private PageQuery<MissionOrderByType> pageQuery() {
    final var offset = pageable.getOffset();
    final var limit = pageable.getPageSize();
    final var pageNumber = pageable.getPageNumber();
    final var orders = pageable.getSort().get().map(this::sortOrder).filter(s -> s != null).toList();
    
    return ImmutablePageQuery.<MissionOrderByType>builder()
        .offset(offset)
        .pageSize(limit)
        .pageNumber(pageNumber)
        .sort(ImmutablePageSorting.<MissionOrderByType>builder().orders(orders).build())
        .build();
  }
  
  private PageQuery.PageSortingOrder<MissionOrderByType> sortOrder(Order order) {
    final ImmutablePageSortingOrder.Builder<MissionOrderByType> type = ImmutablePageSortingOrder
        .<MissionOrderByType>builder()
        .direction(PageSortDirection.valueOf(order.getDirection().name()));
    
    switch (order.getProperty()) {
      case "priority": type.property(MissionOrderByType.MISSION_PRIORITY); break;
      case "status": type.property(MissionOrderByType.MISSION_STATUS); break;
      case "dueDate": type.property(MissionOrderByType.MISSION_DUE_DATE); break;
      case "created": type.property(MissionOrderByType.MISSION_CREATED_AT); break;
      case "clientIdentificator": type.property(MissionOrderByType.MISSION_REPORTER_ID); break;
      case "assignedUser": type.property(MissionOrderByType.MISSION_ASSIGNMENT_VALUE).propertyType(TaskMapper.ASSIGNMENT_TYPE_TASK_USER); break;
      case "subject": type.property(MissionOrderByType.MISSION_TITLE).propertyType(assignedUser); break;
      
      default: return null;
    }

    return type.build();
  }
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

    
    if(!(assignedUser == null || assignedUser.trim().isEmpty())) {
      builder.addAssignment(TaskMapper.ASSIGNMENT_TYPE_TASK_USER, false, assignedUser);
    }
    if(!(role == null || role.trim().isEmpty())) {
      builder.addAssignment(TaskMapper.ASSIGNMENT_TYPE_TASK_ROLE, false, role);
    }
    if(!(additionalInfo == null || additionalInfo.trim().isEmpty())) {    
      builder.addLink(TaskMapper.LINK_TYPE_ADDITIONAL_INFO, additionalInfo);
    }
    
    builder
      .status(statuses)
      .priority(priorities)
      .likeTitle(subject)
      .likeReporterId(clientIdentificator)
      .archived(GrimArchiveQueryType.ONLY_IN_FORCE)
      .overdue(dueDate == null ? null : !dueDate.isEmpty()); // false = mission.mission_due_date < CURRENT_DATE 
    
    if (requireAnyRoles == null) {
      return builder;
    }
    return builder.addAssignment(TaskMapper.ASSIGNMENT_TYPE_TASK_ROLE, false, requireAnyRoles);
  }

  
  public QueryEnvelopePage<GrimMissionContainer> visitEnvelope(GrimStructuredTenant config, QueryEnvelopePage<GrimMissionContainer> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw TaskException.builder("PAGINATE_TASKS_FAIL")
        .add(config, envelope)
        .build();
    }
    final var result = envelope.getCurrentPageObjects();
    if(result == null) {
      throw TaskException.builder("PAGINATE_TASKS_NOT_FOUND")   
        .add(config, envelope)
        .build();
    }
    return envelope;
  }

  @SuppressWarnings("unchecked")
  public Uni<Page<Task>> end(GrimStructuredTenant config, QueryEnvelopePage<GrimMissionContainer> commit) {
    final var tasks = commit.getCurrentPageObjects().stream()
        .map(container -> TaskMapper.map(
            container.getMission(), 
            container.getAssignments().values(), 
            container.getRemarks().values(),
            container.getLinks().values(),
            container.getMissionLabels().values()))
        .toList();
    
    final Page<Task> page = new PageImpl<Task>(tasks, pageable, commit.getTotalObjectsOnPages());
    return Uni.createFrom().item((Object) page).map(e -> (Page<Task>) e);
  }

  
}
