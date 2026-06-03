package io.digiexpress.eveli.client.spi.task.visitors;

import java.util.Arrays;
import java.util.Collections;

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
import io.digiexpress.eveli.client.api.TaskClient.ChangeDocPropertiesCommand;
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
public class ChangeDocProperties implements TaskStoreConfig.MergeTaskVisitor<TaskClient.Task> {
  private final String userId;
  private final String taskId;
  private final ChangeDocPropertiesCommand command;
  
  private TaskClient.Task previousVersion;
  
  public void modify(MergeMission merge) {
    previousVersion = TaskMapper.map(merge.getCurrentState());
    
    if (command.getDocumentProperties().isEmpty()) {
      merge.setAllLinks(TaskMapper.LINK_TYPE_DOC_PROPS, Collections.emptyList(), null);
    } else {
      merge.setAllLinks(
          TaskMapper.LINK_TYPE_DOC_PROPS, 
          Arrays.asList(command.getDocumentProperties()), 
          newlink -> (builder) -> builder
          .linkType(TaskMapper.LINK_TYPE_DOC_PROPS)
          .linkBody(JsonObject.mapFrom(command.getDocumentProperties()))
          .build());
    }
    
    merge.build();
  }
  
  @Override
  public ModifyOneMission start(GrimStructuredTenant config, ModifyOneMission builder) {
    builder.missionId(taskId).modifyMission(merge -> modify(merge));
    return builder
        .commitAuthor(userId)
        .commitMessage("Update task by: " + ChangeDocProperties.class.getSimpleName());
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
    final var task = TaskMapper.map(commited);
    return Uni.createFrom().item(task);
  }
}
