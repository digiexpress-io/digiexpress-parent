package io.digiexpress.eveli.client.spi.task;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;

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

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.ModifyTaskCommand;
import io.digiexpress.eveli.client.api.TaskClient.TaskPriority;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.digiexpress.eveli.client.event.TaskNotificator;
import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimCommitActions.ModifyOneMission;
import io.resys.thena.api.actions.GrimCommitActions.OneMissionEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeMission;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ModifyOneTask implements TaskStoreConfig.MergeTaskVisitor<TaskClient.Task> {
  private final String userId;
  private final String email;
  private final TaskNotificator notificator;
  private final String taskId;
  private final ModifyTaskCommand command;
  
  private TaskClient.Task previousVersion;
  
  public void modify(ModifyTaskCommand command, MergeMission merge) {
    previousVersion = TaskMapper.map(merge.getCurrentState().getMission(), merge.getCurrentState().getAssignments().values());
    
    merge
      // overwrite assignees
      .setAllAssignees(
          TaskMapper.ASSIGNMENT_TYPE_TASK_USER, 
          Arrays.asList(command.getAssignedUser()), 
          newAssignee -> (builder) -> builder
          .assignmentType(TaskMapper.ASSIGNMENT_TYPE_TASK_USER)
          .assignee(newAssignee)
          .assigneeContact(command.getAssignedUserEmail())
          .build())
      
      // overwrite roles
      .setAllAssignees(
          TaskMapper.ASSIGNMENT_TYPE_TASK_ROLE, 
          new ArrayList<>(command.getAssignedRoles()), 
          newRole -> (builder) -> builder
          .assignmentType(TaskMapper.ASSIGNMENT_TYPE_TASK_ROLE)
          .assignee(newRole)
          .build())
      
      // change is viewed by worker who created it
      .addViewer(viewer -> viewer.userId(userId).usedFor(TaskMapper.VIEWER_WORKER).build())
      
      // normal data
      .reporterId(command.getClientIdentificator())
      .description(command.getDescription())
      .dueDate(command.getDueDate())
      .priority(Optional.ofNullable(command.getPriority()).map(TaskPriority::name).orElse(null))
      .status(Optional.ofNullable(command.getStatus()).map(TaskStatus::name).orElse(null))
      .title(command.getSubject())
      .completedAt(Optional.ofNullable(command.getCompleted()).map(ZonedDateTime::toOffsetDateTime).orElse(null))
      
    .build();
  }
  
  @Override
  public ModifyOneMission start(GrimStructuredTenant config, ModifyOneMission builder) {
    builder.missionId(taskId).modifyMission(merge -> modify(command, merge));
    return builder
        .commitAuthor(userId)
        .commitMessage("Update task by: " + ModifyOneTask.class.getSimpleName());
  }

  @Override
  public OneMissionEnvelope visitEnvelope(GrimStructuredTenant config, OneMissionEnvelope envelope) {
    if(envelope.getStatus() == CommitResultStatus.OK) {
      return envelope;
    }
    throw TaskException.builder("MODIFY_ONE_TASK_FAIL").add(config, envelope).build(); 
  }

  @Override
  public Uni<TaskClient.Task> end(GrimStructuredTenant config, OneMissionEnvelope commited) {
    final var task = TaskMapper.map(commited.getMission(), commited.getAssignments());
    notificator.handleTaskUpdate(task, previousVersion, email);
    return Uni.createFrom().item(task);
  }
}
