package io.resys.sysconfig.client.spi.visitors;

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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.sysconfig.client.api.model.Document;
import io.resys.sysconfig.client.api.model.ImmutableSysConfig;
import io.resys.sysconfig.client.api.model.SysConfig;
import io.resys.sysconfig.client.spi.store.DocumentConfig;
import io.resys.sysconfig.client.spi.store.DocumentConfig.DocObjectsVisitor;
import io.resys.sysconfig.client.spi.store.DocumentStoreException;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocLog;
import io.resys.thena.api.entities.doc.ThenaDocObjects.DocObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.projects.client.spi.store.MainBranch;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GetSysConfigsByIdsVisitor implements DocObjectsVisitor<List<SysConfig>> {
  private final Collection<String> projectIds;
  
  @Override
  public DocObjectsQuery start(DocumentConfig config, DocObjectsQuery builder) {
    return builder
        .docType(Document.DocumentType.SYS_CONFIG.name())
        .branchName(MainBranch.HEAD_NAME)
        .matchIds(new ArrayList<>(projectIds));
  }

  @Override
  public DocObjects visitEnvelope(DocumentConfig config, QueryEnvelope<DocObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("GET_SYS_CONFIG_BY_ID_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(projectIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw DocumentStoreException.builder("GET_SYS_CONFIG_BY_ID_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(projectIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    return result;
  }

  @Override
  public List<SysConfig> end(DocumentConfig config, DocObjects ref) {
    if(ref == null) {
      return Collections.emptyList();
    }
    return ref.accept((Doc doc, DocBranch docBranch, DocCommit commit, List<DocLog> log) -> 
      docBranch.getValue()
      .mapTo(ImmutableSysConfig.class).withVersion(commit.getId())
    );
  }
}
