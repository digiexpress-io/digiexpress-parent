package io.resys.thena.projects.client.spi.visitors;

/*-
 * #%L
 * thena-Projects-client
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.PullActions.PullObjectsQuery;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.ThenaGitObjects.PullObjects;
import io.resys.thena.projects.client.api.model.ImmutableProject;
import io.resys.thena.projects.client.api.model.Project;
import io.resys.thena.projects.client.spi.store.DocumentConfig;
import io.resys.thena.projects.client.spi.store.DocumentConfig.DocPullObjectsVisitor;
import io.resys.thena.projects.client.spi.store.DocumentStoreException;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GetActiveProjectsByIdsVisitor implements DocPullObjectsVisitor<Project> {
  private final Collection<String> ProjectIds;
  
  @Override
  public PullObjectsQuery start(DocumentConfig config, PullObjectsQuery builder) {
    return builder.docId(new ArrayList<>(ProjectIds));
  }

  @Override
  public PullObjects visitEnvelope(DocumentConfig config, QueryEnvelope<PullObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("GET_PROJECTS_BY_IDS_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(ProjectIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw DocumentStoreException.builder("GET_PROJECTS_BY_IDS_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(ProjectIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    return result;
  }

  @Override
  public List<Project> end(DocumentConfig config, PullObjects blob) {
    return blob.accept((JsonObject json) -> json.mapTo(ImmutableProject.class));
  }
}
