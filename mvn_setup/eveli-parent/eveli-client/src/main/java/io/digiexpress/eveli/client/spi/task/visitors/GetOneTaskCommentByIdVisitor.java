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

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.spi.task.TaskException;
import io.digiexpress.eveli.client.spi.task.TaskMapper;
import io.digiexpress.eveli.client.spi.task.TaskStoreConfig;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.grim.api.GrimClient.GrimStructuredTenant;
import io.resys.thena.grim.api.GrimQueryActions.MissionRemarkQuery;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class GetOneTaskCommentByIdVisitor implements TaskStoreConfig.QueryOneTaskCommentsVisitor<TaskClient.TaskComment> {
  private final String commentId;
  
  @Override
  public Uni<QueryEnvelope<GrimMissionContainer>> start(GrimStructuredTenant config, MissionRemarkQuery query) {
    return query.getOneByRemarkId(commentId);
  }

  @Override
  public GrimMissionContainer visitEnvelope(GrimStructuredTenant config, QueryEnvelope<GrimMissionContainer> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw TaskException.builder("GET_TASK_COMMENT_BY_ID_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(commentId))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null || result.getRemarks().isEmpty()) {
      throw TaskException.builder("GET_TASK_COMMENT_BY_ID_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(commentId))
        .build();
    }
    return result;
  }

  @Override
  public Uni<TaskClient.TaskComment> end(GrimStructuredTenant config, GrimMissionContainer commit) {
    return Uni.createFrom().item(TaskMapper.map(commit.getRemarks().values().iterator().next()));
  }
}
