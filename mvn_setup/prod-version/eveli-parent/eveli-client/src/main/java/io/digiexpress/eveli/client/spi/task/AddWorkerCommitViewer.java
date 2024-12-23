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

import io.digiexpress.eveli.client.api.TaskClient;
import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimCommitActions.ModifyOneMission;
import io.resys.thena.api.actions.GrimCommitActions.OneMissionEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeMission;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddWorkerCommitViewer implements TaskStoreConfig.MergeTaskVisitor<TaskClient.Task> {
  private final String userId;
  private final String taskId;
  
  public void modify(MergeMission merge) {

    merge
    // change is viewed by worker who created it
    .addViewer(viewer -> viewer.userId(userId).usedFor(TaskMapper.VIEWER_WORKER).build())
    .build();
  }
  
  @Override
  public ModifyOneMission start(GrimStructuredTenant config, ModifyOneMission builder) {
    builder.missionId(taskId).modifyMission(merge -> modify(merge));
    return builder
        .commitAuthor(userId)
        .commitMessage("Adding task viewer by: " + AddWorkerCommitViewer.class.getSimpleName());
  }

  @Override
  public OneMissionEnvelope visitEnvelope(GrimStructuredTenant config, OneMissionEnvelope envelope) {
    if(envelope.getStatus() == CommitResultStatus.OK) {
      return envelope;
    }
    throw TaskException.builder("ADD_ONE_TASK_WORKER_VIEWER_FAIL").add(config, envelope).build(); 
  }

  @Override
  public Uni<TaskClient.Task> end(GrimStructuredTenant config, OneMissionEnvelope commited) {
    final var task = TaskMapper.map(commited.getMission(), commited.getAssignments(), commited.getRemarks());
    return Uni.createFrom().item(task);
  }
}
