package io.digiexpress.eveli.client.spi.task;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

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

import io.digiexpress.eveli.client.api.ImmutableTask;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.CreateTaskCommand;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.api.TaskClient.TaskPriority;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.digiexpress.eveli.client.event.TaskNotificator;
import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimCommitActions.CreateOneMission;
import io.resys.thena.api.actions.GrimCommitActions.OneMissionEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewMission;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateOneTask implements TaskStoreConfig.CreateOneTaskVisitor<TaskClient.Task> {
  private final String userId;
  private final TaskNotificator notificator;
  private final CreateTaskCommand command;
  
  public static final String ASSIGNMENT_TYPE_TASK_USER = "task_user";
  public static final String ASSIGNMENT_TYPE_TASK_ROLE = "task_role";
  public static final String LABEL_TYPE_KEYWORD = "keyword";
  
  private void createTask(CreateTaskCommand commmand, NewMission mission) {
    final var status = commmand.getStatus() == null ? TaskStatus.NEW: commmand.getStatus();
    
    mission
      .reporterId(commmand.getClientIdentificator())
      .title(commmand.getSubject())
      .description(commmand.getDescription())
      .dueDate(commmand.getDueDate())
      .status(status.name())
      .priority(Optional.ofNullable(commmand.getPriority()).map(TaskPriority::name).orElse(null))
      .questionnaireId(commmand.getQuestionnaireId())
      .addViewer(newViewer -> newViewer.userId(userId).build());
    
    // add roles
    for(final var role : commmand.getAssignedRoles()) {
      mission
      .addAssignees(newAss -> newAss
        .assignee(role)
        .assignmentType(ASSIGNMENT_TYPE_TASK_ROLE)
        .build()
      );
    }

    // assign to given user    
    if(commmand.getAssignedUser() != null) {
      mission
      .addAssignees(newAss -> newAss
        .assignee(commmand.getAssignedUser())
        .assigneeContact(commmand.getAssignedUserEmail())
        .assignmentType(ASSIGNMENT_TYPE_TASK_USER)
        .build()
      );
    }
    
    for(final var keyword : commmand.getKeyWords()) {
      mission.addLabels(newLabel -> newLabel
          .labelType(LABEL_TYPE_KEYWORD)
          .labelValue(keyword)
          .build());
    }

    mission.build();
  }

  @Override
  public CreateOneMission start(GrimStructuredTenant config, CreateOneMission builder) {
    builder.mission(newMission -> createTask(command, newMission));
    return builder
        .commitAuthor(userId)
        .commitMessage("Creating tasks by: " + CreateOneTask.class.getSimpleName());
  }

  @Override
  public GrimMission visitEnvelope(GrimStructuredTenant config, OneMissionEnvelope envelope) {
    if(envelope.getStatus() == CommitResultStatus.OK) {
      return envelope.getMission();
    }
    throw TaskException.builder("CREATE_TASKS_SAVE_FAIL").add(config, envelope).build(); 
  }

  @Override
  public Uni<Task> end(GrimStructuredTenant config, GrimMission commited) {

    final var assignee = commited.getTransitives().getAssignments().stream()
    .filter(e -> ASSIGNMENT_TYPE_TASK_USER.equals(e.getAssignmentType()))
    .findFirst();
    
    
    final var task = ImmutableTask.builder()
        .version(commited.getCommitId())
        .clientIdentificator(commited.getReporterId())
        .description(commited.getDescription())
        .dueDate(commited.getDueDate())
        .id(commited.getId())

        .completed(toZoned(commited.getCompletedAt()))
        .created(toZoned(commited.getTransitives().getCreatedAt()))
        .questionnaireId(commited.getQuestionnaireId())
        .priority(TaskPriority.valueOf(commited.getMissionPriority()))
        .status(TaskStatus.valueOf(commited.getMissionStatus()))
        .subject(commited.getTitle())
        .taskRef(commited.getRefId())
        
        .updated(toZoned(commited.getTransitives().getTreeUpdatedAt()))
        .updaterId(commited.getTransitives().getTreeUpdatedBy())
        
        .assignedUser(assignee.map(e -> e.getAssignee()).orElse(null))
        .assignedUserEmail(assignee.map(e -> e.getAssigneeContact()).orElse(null))

        .build();
    
    notificator.handleTaskCreation(task, userId); 
    return Uni.createFrom().item(task);
  }
  
  
  public static ZonedDateTime toZoned(OffsetDateTime input) {
    if(input == null) {
      return null;
    }
    return input.toZonedDateTime();
  } 
}
