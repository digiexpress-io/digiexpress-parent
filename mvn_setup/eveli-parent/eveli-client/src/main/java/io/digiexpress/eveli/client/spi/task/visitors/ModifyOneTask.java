package io.digiexpress.eveli.client.spi.task.visitors;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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
import io.digiexpress.eveli.client.spi.task.TaskException;
import io.digiexpress.eveli.client.spi.task.TaskMapper;
import io.digiexpress.eveli.client.spi.task.TaskStoreConfig;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeMission;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.grim.api.GrimClient.GrimStructuredTenant;
import io.resys.thena.grim.api.GrimCommitActions.ModifyOneMission;
import io.resys.thena.grim.api.GrimCommitActions.OneMissionEnvelope;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ModifyOneTask implements TaskStoreConfig.MergeTaskVisitor<TaskClient.Task> {
  private final String userId;
  private final String email;
  private final String taskId;
  private final ModifyTaskCommand command;
  
  private TaskClient.Task previousVersion;
  
  public void modify(ModifyTaskCommand command, MergeMission merge) {
    previousVersion = TaskMapper.map(
        merge.getCurrentState().getMission(), 
        merge.getCurrentState().getAssignments().values(),
        merge.getCurrentState().getRemarks().values(),
        merge.getCurrentState().getLinks().values(),
        merge.getCurrentState().getMissionLabels().values(),
        merge.getCurrentState().getObjectives().values()
        );
    
    if(command.getVersion() != null && !previousVersion.getVersion().equals(command.getVersion())) {
      throw TaskException.builder("MODIFY_ONE_TASK_FAIL_LOCK_VERSION_MISMATCH")
      .add("locking failed", 
          "Can't modify old version, locking failed",
          JsonObject
          .of("provided", command.getVersion(),
              "expected", previousVersion.getVersion())
      )
      .build(); 
    }
    
    
    // overwrite assignees    
    if(command.getAssignedUser() == null) {
      merge.setAllAssignees(TaskMapper.ASSIGNMENT_TYPE_TASK_USER, Collections.emptyList(), null);
    } else {
      merge.setAllAssignees(
          TaskMapper.ASSIGNMENT_TYPE_TASK_USER, 
          Arrays.asList(command.getAssignedUser()), 
          newAssignee -> (builder) -> builder
          .assignmentType(TaskMapper.ASSIGNMENT_TYPE_TASK_USER)
          .assignee(newAssignee)
          .assigneeContact(command.getAssignedUserEmail())
          .build());
    }
    
    
    // overwrite additional info    
    if(command.getAdditionalInfo() == null) {
      merge.setAllLinks(TaskMapper.LINK_TYPE_ADDITIONAL_INFO, Collections.emptyList(), null);
    } else {
      merge.setAllLinks(
          TaskMapper.LINK_TYPE_ADDITIONAL_INFO, 
          Arrays.asList(command.getAdditionalInfo()), 
          newlink -> (builder) -> builder
          .linkType(TaskMapper.LINK_TYPE_ADDITIONAL_INFO)
          .linkValue(command.getAdditionalInfo())
          .build());
    }
    
    merge
      
      // overwrite roles
      .setAllAssignees(
          TaskMapper.ASSIGNMENT_TYPE_TASK_ROLE, 
          new ArrayList<>(command.getAssignedRoles()), 
          newRole -> (builder) -> builder
          .assignmentType(TaskMapper.ASSIGNMENT_TYPE_TASK_ROLE)
          .assignee(newRole)
          .build())
      
      // change is viewed by worker who created it
      .addViewer(viewer -> viewer.userId(userId).usedFor(TaskMapper.VIEWER_WORKER).currentTxCommit().build())
      
      // normal data
      .reporterId(command.getClientIdentificator())
      .description(command.getDescription())
      .dueDate(command.getDueDate())
      .priority(Optional.ofNullable(command.getPriority()).map(TaskPriority::name).orElse(previousVersion.getPriority().name()))
      .status(Optional.ofNullable(command.getStatus()).map(TaskStatus::name).orElse(previousVersion.getStatus().name()))
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
    final var task = TaskMapper.map(
        commited.getMission(), 
        commited.getAssignments(), 
        commited.getRemarks(),
        commited.getLinks(),
        commited.getLabels(),
        commited.getObjectives());
    return Uni.createFrom().item(task);
  }
}
