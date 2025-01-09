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

import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimQueryActions.MissionQuery;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FindAllUnreadTasksVisitor implements TaskStoreConfig.QueryTasksVisitor<List<String>> {
  private final String userId;
  private final List<String> roles;
  private final String viewerType;
  
  @Override
  public MissionQuery start(GrimStructuredTenant config, MissionQuery query) {
    
    if(!roles.isEmpty()) {
      query.addAssignment(TaskMapper.ASSIGNMENT_TYPE_TASK_ROLE, false, roles);
    }
    return query
        .addAssignment(TaskMapper.ASSIGNMENT_TYPE_TASK_USER, false, userId)
        .notViewed(userId, viewerType)
        .atLeastOneRemarkWithAnyType();

  }

  @Override
  public List<GrimMissionContainer> visitEnvelope(GrimStructuredTenant config, QueryEnvelopeList<GrimMissionContainer> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw TaskException.builder("FIND_ALL_UNREAD_TASKS_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(userId).addArgs(roles.toArray(new String[] {})))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw TaskException.builder("FIND_ALL_UNREAD_TASKS_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(userId).addArgs(roles.toArray(new String[] {})))
        .build();
    }
    return result;
  }

  @Override
  public Uni<List<String>> end(GrimStructuredTenant config, List<GrimMissionContainer> commit) {
    return Uni.createFrom().item(commit.stream().map(e -> e.getMission().getId()).toList());
  }
}
