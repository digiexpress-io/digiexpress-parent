package io.resys.thena.tasks.client.spi.visitors;

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

import java.util.Arrays;
import java.util.List;

import io.resys.thena.api.actions.GitHistoryActions;
import io.resys.thena.api.actions.GitHistoryActions.BlobHistoryQuery;
import io.resys.thena.api.actions.GitPullActions.MatchCriteria;
import io.resys.thena.api.entities.git.BlobHistory;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.tasks.client.api.model.Document;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.ImmutableTaskHistory;
import io.resys.thena.tasks.client.api.model.Task.TaskHistory;
import io.resys.thena.tasks.client.spi.store.DocumentConfig;
import io.resys.thena.tasks.client.spi.store.DocumentConfig.DocHistoryVisitor;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetTaskHistoryVisitor implements DocHistoryVisitor<TaskHistory> {
  private final String taskId;
  
  @Override
  public BlobHistoryQuery start(DocumentConfig config, BlobHistoryQuery builder) {
     builder.latestOnly(false).matchBy(
      MatchCriteria.equalsTo("documentType", Document.DocumentType.TASK.name()),
      MatchCriteria.equalsTo("id", taskId)
    );
     
     return builder;
  }

  @Override
  public GitHistoryActions.HistoryObjects visitEnvelope(DocumentConfig config, QueryEnvelope<GitHistoryActions.HistoryObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("FIND_TASK_HISTORY_FAIL").add(config, envelope)
      .add(c -> c.addArgs(JsonObject.of(
          "taskId", taskId
          ).encode()))
      .build();
    }
    if(envelope.getObjects().getValues().isEmpty()) {
      throw DocumentStoreException.builder("NO_TASK_HISTORY_TO_FIND").add(config, envelope)
      .add(c -> c.addArgs(JsonObject.of(
          "taskId", taskId
          ).encode()))
      .build();
    }
    return envelope.getObjects();
  }

  @Override
  public List<TaskHistory> end(DocumentConfig config, GitHistoryActions.HistoryObjects values) {
    BlobHistory previous = null;
    ImmutableTaskHistory.Builder historyBuilder = ImmutableTaskHistory.builder();
    for(final var history : values.getValues()) {
      if(previous != null && previous.getBlob().equals(history.getBlob())) {
        previous = history;
        continue;
      }

      historyBuilder.addVersions(history.getBlob().getValue().mapTo(ImmutableTask.class));
      previous = history;
    }

    return Arrays.asList(historyBuilder.id("not implemented yet").build());
  }
}
  
