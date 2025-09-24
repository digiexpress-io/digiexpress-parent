package io.digiexpress.eveli.client.spi.task.visitors;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.CompleteCustomerAssignmentCommand;
import io.digiexpress.eveli.client.spi.task.TaskException;
import io.digiexpress.eveli.client.spi.task.TaskMapper;
import io.digiexpress.eveli.client.spi.task.TaskStoreConfig;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeMission;
import io.resys.thena.grim.api.GrimClient.GrimStructuredTenant;
import io.resys.thena.grim.api.GrimCommitActions.ModifyOneMission;
import io.resys.thena.grim.api.GrimCommitActions.OneMissionEnvelope;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CompleteCustomerAssignment implements TaskStoreConfig.MergeTaskVisitor<TaskClient.Task> {
  private final String userId;
  private final String taskId;
  private final CompleteCustomerAssignmentCommand command;
  
  private TaskClient.Task previousVersion;
  
  public void modify(MergeMission merge) {
    previousVersion = TaskMapper.map(
        merge.getCurrentState().getMission(), 
        merge.getCurrentState().getAssignments().values(),
        merge.getCurrentState().getRemarks().values(),
        merge.getCurrentState().getLinks().values(),
        merge.getCurrentState().getMissionLabels().values(),
        merge.getCurrentState().getObjectives().values()
        );
    
    if(command.getTaskVersion() != null && !previousVersion.getVersion().equals(command.getTaskVersion())) {
      throw TaskException.builder("MODIFY_ONE_TASK_ASSIGNMENT_FAIL_LOCK_VERSION_MISMATCH")
      .add("locking failed", 
          "Can't modify old version, locking failed",
          JsonObject
          .of("provided", command.getTaskVersion(),
              "expected", previousVersion.getVersion())
      )
      .build(); 
    }
    
    
    merge.modifyObjective(command.getAssignmentId(), objective -> {
      objective.status(TaskClient.TaskAssignmentStatus.COMPLETED.name());
    }).build();
  }
  
  @Override
  public ModifyOneMission start(GrimStructuredTenant config, ModifyOneMission builder) {
    builder.missionId(taskId).modifyMission(merge -> modify(merge));
    return builder
        .commitAuthor(userId)
        .commitMessage("Update task by: " + CompleteCustomerAssignment.class.getSimpleName());
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
