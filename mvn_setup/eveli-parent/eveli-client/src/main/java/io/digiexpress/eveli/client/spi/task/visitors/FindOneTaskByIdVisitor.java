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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.spi.task.TaskException;
import io.digiexpress.eveli.client.spi.task.TaskMapper;
import io.digiexpress.eveli.client.spi.task.TaskStoreConfig;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.grim.api.GrimClient.GrimStructuredTenant;
import io.resys.thena.grim.api.GrimQueryActions.MissionQuery;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FindOneTaskByIdVisitor implements TaskStoreConfig.QueryTasksVisitor<Optional<TaskClient.Task>> {
  private final String taskId;
  
  @Override
  public MissionQuery start(GrimStructuredTenant config, MissionQuery query) {
    return query
          .addMissionId(Arrays.asList(taskId))
          // we don't need following docs
          .excludeDocs(
              GrimDocType.GRIM_COMMANDS, 
              GrimDocType.GRIM_COMMIT, 
              GrimDocType.GRIM_COMMIT_VIEWER, 
              GrimDocType.GRIM_OBJECTIVE_GOAL);
  }

  @Override
  public  List<GrimMissionContainer> visitEnvelope(GrimStructuredTenant config, QueryEnvelopeList<GrimMissionContainer> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw TaskException.builder("FIND_ONE_TASK_BY_ID_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(taskId))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null || result.isEmpty()) {
      return Collections.emptyList();
    }
    return result;
  }

  @Override
  public Uni<Optional<Task>> end(GrimStructuredTenant config, List<GrimMissionContainer> commit) {
    if(commit.isEmpty()) {
      return Uni.createFrom().item(Optional.empty());
    }
    
    
    final var container = commit.iterator().next();
    final var task = TaskMapper.map(
        container.getMission(), 
        container.getAssignments().values(), 
        container.getRemarks().values(),
        container.getLinks().values(),
        container.getMissionLabels().values(),
        container.getObjectives().values());
    
    return Uni.createFrom().item(Optional.of(task));
  }
}
