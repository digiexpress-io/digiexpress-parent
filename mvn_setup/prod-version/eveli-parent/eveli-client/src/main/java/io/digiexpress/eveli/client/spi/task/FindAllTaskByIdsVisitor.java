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

import java.util.List;

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimQueryActions.MissionQuery;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FindAllTaskByIdsVisitor implements TaskStoreConfig.QueryTasksVisitor<List<TaskClient.Task>> {
  private final List<String> taskId;
  
  @Override
  public MissionQuery start(GrimStructuredTenant config, MissionQuery query) {
    return query
          .addMissionId(taskId)
          // we don't need following docs
          .excludeDocs(
              GrimDocType.GRIM_COMMANDS, 
              GrimDocType.GRIM_COMMIT, 
              GrimDocType.GRIM_COMMIT_VIEWER, 
              GrimDocType.GRIM_OBJECTIVE,
              GrimDocType.GRIM_OBJECTIVE_GOAL);
  }

  @Override
  public  List<GrimMissionContainer> visitEnvelope(GrimStructuredTenant config, QueryEnvelopeList<GrimMissionContainer> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw TaskException.builder("GET_TASKS_BY_IDS_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(taskId.toString()))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null || result.isEmpty()) {
      throw TaskException.builder("GET_TASKS_BY_IDS_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(taskId.toString()))
        .build();
    }
    return result;
  }

  @Override
  public Uni<List<Task>> end(GrimStructuredTenant config, List<GrimMissionContainer> commit) {
    final var tasks = commit.stream()
        .map(container -> TaskMapper.map(
            container.getMission(), 
            container.getAssignments().values(), 
            container.getRemarks().values()))
        .toList();
    
    return Uni.createFrom().item(tasks);
  }
}
