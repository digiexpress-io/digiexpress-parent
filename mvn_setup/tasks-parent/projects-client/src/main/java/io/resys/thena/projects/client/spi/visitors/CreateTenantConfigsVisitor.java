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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.DocCommitActions.CreateManyDocs;
import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.models.Repo.CommitResultStatus;
import io.resys.thena.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.projects.client.api.model.Document;
import io.resys.thena.projects.client.api.model.ImmutableTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.CreateTenantConfig;
import io.resys.thena.projects.client.spi.store.DocumentConfig;
import io.resys.thena.projects.client.spi.store.DocumentConfig.DocCreateVisitor;
import io.resys.thena.projects.client.spi.store.DocumentStoreException;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateTenantConfigsVisitor implements DocCreateVisitor<TenantConfig> {
  private final List<? extends CreateTenantConfig> commands;
  private final List<TenantConfig> createdTenants = new ArrayList<TenantConfig>();
  
  @Override
  public CreateManyDocs start(DocumentConfig config, CreateManyDocs builder) {
    builder
      .docType(Document.DocumentType.TENANT_CONFIG.name())
      .author(config.getAuthor().get())
      .message("creating tenant");
    
    for(final var command : commands) {
      final var entity = new TenantConfigCommandVisitor(config).visitTransaction(Arrays.asList(command));
      final var json = JsonObject.mapFrom(entity);
      builder.item().append(json).docId(entity.getId()).next();
      createdTenants.add(entity);
    }
    return builder;
  }

  @Override
  public List<DocBranch> visitEnvelope(DocumentConfig config, ManyDocsEnvelope envelope) {
    if(envelope.getStatus() == CommitResultStatus.OK) {
      return envelope.getBranch();
    }
    throw new DocumentStoreException("TENANT_CREATE_FAIL", DocumentStoreException.convertMessages(envelope));
  }

  @Override
  public List<TenantConfig> end(DocumentConfig config, List<DocBranch> branches) {
    final Map<String, TenantConfig> configsById = new HashMap<>(
        this.createdTenants.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    
    branches.forEach(branch -> {
      
      final var next = ImmutableTenantConfig.builder()
          .from(configsById.get(branch.getDocId()))
          .version(branch.getCommitId())
          .build();
      
      configsById.put(next.getId(), next);
    });
    
    return Collections.unmodifiableList(new ArrayList<>(configsById.values()));
  }

}
