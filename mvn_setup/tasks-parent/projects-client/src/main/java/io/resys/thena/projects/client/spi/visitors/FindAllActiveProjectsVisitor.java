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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.resys.thena.docdb.api.actions.BranchActions.BranchObjectsQuery;
import io.resys.thena.docdb.api.actions.ImmutableMatchCriteria;
import io.resys.thena.docdb.api.actions.PullActions.MatchCriteriaType;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.ThenaObjects.BranchObjects;
import io.resys.thena.projects.client.api.model.Document;
import io.resys.thena.projects.client.api.model.ImmutableProject;
import io.resys.thena.projects.client.api.model.Project;
import io.resys.thena.projects.client.spi.store.DocumentConfig;
import io.resys.thena.projects.client.spi.store.DocumentConfig.DocBranchVisitor;
import io.resys.thena.projects.client.spi.store.DocumentStoreException;
import io.vertx.core.json.JsonObject;

public class FindAllActiveProjectsVisitor implements DocBranchVisitor<List<Project>> {
  @Override
  public BranchObjectsQuery start(DocumentConfig config, BranchObjectsQuery builder) {
    return builder.docsIncluded()
        .matchBy(Arrays.asList(ImmutableMatchCriteria.builder()
        .key("documentType").value(Document.DocumentType.PROJECT_META.name())
        .type(MatchCriteriaType.EQUALS)
        .build()));
  }
  @Override
  public BranchObjects visitEnvelope(DocumentConfig config, QueryEnvelope<BranchObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("FIND_ALL_PROJECTS_FAIL").add(config, envelope).build();
    }
    return envelope.getObjects();
  }

  @Override
  public List<Project> end(DocumentConfig config, BranchObjects ref) {
    if(ref == null) {
      return Collections.emptyList();
    }
    return ref.accept((JsonObject json) -> json.mapTo(ImmutableProject.class));
  }
}
