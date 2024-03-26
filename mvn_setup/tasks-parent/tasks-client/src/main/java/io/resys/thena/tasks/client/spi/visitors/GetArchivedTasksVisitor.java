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

import java.time.LocalDate;
import java.util.List;

import javax.annotation.Nullable;

import io.resys.thena.api.actions.HistoryActions.BlobHistoryQuery;
import io.resys.thena.api.actions.PullActions.MatchCriteria;
import io.resys.thena.api.models.QueryEnvelope;
import io.resys.thena.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.models.ThenaGitObjects.HistoryObjects;
import io.resys.thena.tasks.client.api.model.Document;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.spi.store.DocumentConfig;
import io.resys.thena.tasks.client.spi.store.DocumentConfig.DocHistoryVisitor;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GetArchivedTasksVisitor implements DocHistoryVisitor<Task> {

  private final @Nullable String likeTitle;
  private final @Nullable String likeDescription;
  private final @Nullable String reporterId;
  private final LocalDate fromCreatedOrUpdated;
  
  @Override
  public BlobHistoryQuery start(DocumentConfig config, BlobHistoryQuery builder) {
     builder.latestOnly(true).matchBy(
      MatchCriteria.equalsTo("documentType", Document.DocumentType.TASK.name()),
      MatchCriteria.notNull("archived")
      //TODO MatchCriteria.greaterThanOrEqualTo("updated", fromCreatedOrUpdated.atStartOfDay())
    );
     
     if(likeTitle != null) {
       builder.matchBy(MatchCriteria.like("title", likeTitle));
     }
     if(likeDescription != null) {
       builder.matchBy(MatchCriteria.like("description", likeDescription));
     }
     if(reporterId != null) {
       builder.matchBy(MatchCriteria.equalsTo("reporterId", reporterId));
     }
     
     return builder;
  }

  @Override
  public HistoryObjects visitEnvelope(DocumentConfig config, QueryEnvelope<HistoryObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("FIND_ARCHIVED_TASKS_FAIL").add(config, envelope)
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
  public List<Task> end(DocumentConfig config, HistoryObjects values) {
    return values.accept(blob -> blob.mapTo(ImmutableTask.class));
  }
}
