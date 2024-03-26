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

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.actions.DocCommitActions.ModifyManyDocBranches;
import io.resys.thena.api.actions.DocCommitActions.ModifyManyDocs;
import io.resys.thena.api.actions.DocQueryActions;
import io.resys.thena.api.actions.DocQueryActions.DocObjects;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.projects.client.api.model.Document;
import io.resys.thena.projects.client.api.model.ImmutableArchiveTenantConfig;
import io.resys.thena.projects.client.api.model.ImmutableTenantConfig;
import io.resys.thena.projects.client.api.model.ImmutableTenantConfigTransaction;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.spi.store.DocumentConfig;
import io.resys.thena.projects.client.spi.store.DocumentConfig.DocObjectsVisitor;
import io.resys.thena.projects.client.spi.store.DocumentStoreException;
import io.resys.thena.projects.client.spi.store.MainBranch;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteAllTenantsVisitor implements DocObjectsVisitor<Uni<List<TenantConfig>>>{

  private final String userId;
  private final Instant targetDate;
  
  private ModifyManyDocBranches archiveCommand;
  private ModifyManyDocs removeCommand;
  
  @Override
  public DocObjectsQuery start(DocumentConfig config, DocObjectsQuery query) {
    // Create two commands: one for making changes by adding archive flag, the other for deleting Project from commit tree
    this.archiveCommand = config.getClient().doc(config.getRepoId()).commit().modifyManyBranches()
        .author(config.getAuthor().get())
        .message("Archive Tenants");
    this.removeCommand = config.getClient().doc(config.getRepoId()).commit().modifyManyDocs()
        .author(config.getAuthor().get())
        .message("Delete Tenants");
    
    // Build the blob criteria for finding all documents of type Project
    return query.docType(Document.DocumentType.TENANT_CONFIG.name());
  }

  @Override
  public DocQueryActions.DocObjects visitEnvelope(DocumentConfig config, QueryEnvelope<DocQueryActions.DocObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("FIND_ALL_TENANTS_FAIL_FOR_DELETE").add(config, envelope).build();
    }
    return envelope.getObjects();
  }
  
  @Override
  public Uni<List<TenantConfig>> end(DocumentConfig config, DocQueryActions.DocObjects ref) {
    if(ref == null) {
      return Uni.createFrom().item(Collections.emptyList());
    }

    final var tenantsRemoved = visitTree(ref);    
    return archiveCommand.build()
      .onItem().transform((ManyDocsEnvelope commit) -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return commit;
        }
        throw new DocumentStoreException("TENANT_ARCHIVE_FAIL", DocumentStoreException.convertMessages(commit));
      })
      .onItem().transformToUni(archived -> removeCommand.build())
      .onItem().transform((ManyDocsEnvelope commit) -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return commit;
        }
        throw new DocumentStoreException("TENANT_REMOVE_FAIL", DocumentStoreException.convertMessages(commit));
      })
      .onItem().transform((commit) -> tenantsRemoved);
  }

  
  
  
  private List<TenantConfig> visitTree(DocQueryActions.DocObjects state) {
    return state.getBranches().values().stream().flatMap(e -> e.stream())
      .map(blob -> blob.getValue().mapTo(ImmutableTenantConfig.class))
      .map(TenantConfig -> visitTenantConfig(TenantConfig))
      .collect(Collectors.toUnmodifiableList());
  }
  private TenantConfig visitTenantConfig(TenantConfig tenantConfig) {
    final var tenantId = tenantConfig.getId();
    
    final var nextVersion = ImmutableTenantConfig.builder().from(tenantConfig)
        .version(userId)
        .archived(targetDate)
        .addTransactions(ImmutableTenantConfigTransaction.builder()
            .id(String.valueOf(tenantConfig.getTransactions().size() +1))
            .addCommands(ImmutableArchiveTenantConfig.builder()
                .tenantConfigId(tenantId)
                .userId(userId)
                .targetDate(targetDate)
                .build())
            .build())
        .build();
    final var json = JsonObject.mapFrom(nextVersion);
    archiveCommand.item().docId(tenantId).branchName(MainBranch.HEAD_NAME).append(json).next();
    removeCommand.item().docId(tenantId).remove();
    return nextVersion;
  }

}
