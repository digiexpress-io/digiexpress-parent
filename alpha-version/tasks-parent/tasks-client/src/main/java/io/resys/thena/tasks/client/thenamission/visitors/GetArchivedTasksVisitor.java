package io.resys.thena.tasks.client.thenamission.visitors;

/*-
 * #%L
 * thena-tasks-client
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.time.LocalDate;
import java.util.List;

import javax.annotation.Nullable;

import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimQueryActions.GrimArchiveQueryType;
import io.resys.thena.api.actions.GrimQueryActions.MissionQuery;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.tasks.client.api.actions.TaskActions.TaskAccessEvaluator;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.thenamission.TaskStoreConfig;
import io.resys.thena.tasks.client.thenamission.support.EvaluateTaskAccess;
import io.resys.thena.tasks.client.thenamission.support.TaskException;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GetArchivedTasksVisitor implements TaskStoreConfig.QueryTasksVisitor<List<Task>> {

  private final @Nullable String likeTitle;
  private final @Nullable String likeDescription;
  private final @Nullable String reporterId;
  private final LocalDate fromCreatedOrUpdated;
  private final TaskAccessEvaluator access;
  
  @Override
  public MissionQuery start(GrimStructuredTenant config, MissionQuery builder) {
     builder.archived(GrimArchiveQueryType.ONLY_ARCHIVED);
     
     if(likeTitle != null) {
       builder.likeTitle(likeTitle);
     }
     if(likeDescription != null) {
       builder.likeDescription(likeDescription);
     }
     if(reporterId != null) {
       builder.reporterId(reporterId);
     }
     return builder;
  }

  @Override
  public List<GrimMissionContainer> visitEnvelope(GrimStructuredTenant config, QueryEnvelopeList<GrimMissionContainer> envelope) {
    
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw TaskException.builder("FIND_ARCHIVED_TASKS_FAIL").add(config, envelope)
      .add(c -> c.addArgs(JsonObject.of(
          "fromCreatedOrUpdated", fromCreatedOrUpdated,
          "likeTitle", likeTitle,
          "likeDescription", likeDescription,
          "reporterId", reporterId
          ).encode()))
      .build();
    }
    return envelope.getObjects();
  }

  @Override
  public Uni<List<Task>> end(GrimStructuredTenant config, List<GrimMissionContainer> commit) {
    final var access = EvaluateTaskAccess.of(this.access);
    return Uni.createFrom().item(commit.stream()
        .map(CreateTasksVisitor::mapToTask)
        .map(access::isReadAccessGranted)
        .toList());
  }
}
