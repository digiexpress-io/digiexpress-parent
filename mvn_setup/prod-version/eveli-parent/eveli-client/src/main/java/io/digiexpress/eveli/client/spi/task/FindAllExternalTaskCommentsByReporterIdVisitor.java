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
import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimQueryActions.MissionRemarkQuery;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FindAllExternalTaskCommentsByReporterIdVisitor implements TaskStoreConfig.QueryOneTaskCommentsVisitor<List<TaskClient.TaskComment>> {
  private final String reporterId;
  
  @Override
  public Uni<QueryEnvelope<GrimMissionContainer>> start(GrimStructuredTenant config, MissionRemarkQuery query) {
    return query.findAllByReporterId(reporterId);
  }

  @Override
  public GrimMissionContainer visitEnvelope(GrimStructuredTenant config, QueryEnvelope<GrimMissionContainer> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw TaskException.builder("FIND_ALL_TASK_COMMENTS_BY_TASK_REPORTER_ID_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(reporterId))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw TaskException.builder("FIND_ALL_TASK_COMMENTS_BY_REPORTER_ID_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(reporterId))
        .build();
    }
    return result;
  }

  @Override
  public Uni<List<TaskClient.TaskComment>> end(GrimStructuredTenant config, GrimMissionContainer commit) {
    return Uni.createFrom().item(
      commit.getRemarks().values().stream()
      .map(TaskMapper::map)
      .filter(e -> Boolean.TRUE.equals(e.getExternal()))
      .toList()
    );
  }
}
