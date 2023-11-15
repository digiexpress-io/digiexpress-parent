package io.resys.thena.projects.client.spi.visitors;

import java.util.ArrayList;

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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.CommitActions.CommitResultStatus;
import io.resys.thena.docdb.api.actions.DocCommitActions.ModifyManyDocBranches;
import io.resys.thena.docdb.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.ThenaDocObject.Doc;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommit;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocLog;
import io.resys.thena.docdb.api.models.ThenaDocObjects.DocObjects;
import io.resys.thena.projects.client.api.model.ImmutableTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.TenantConfigUpdateCommand;
import io.resys.thena.projects.client.spi.store.DocumentConfig;
import io.resys.thena.projects.client.spi.store.DocumentConfig.DocObjectsVisitor;
import io.resys.thena.projects.client.spi.store.DocumentStore;
import io.resys.thena.projects.client.spi.store.DocumentStoreException;
import io.resys.thena.projects.client.spi.store.MainBranch;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;


public class UpdateTenantConfigVisitor implements DocObjectsVisitor<Uni<List<TenantConfig>>> {
  private final DocumentStore ctx;
  private final List<String> tenantIds;
  private final ModifyManyDocBranches commitBuilder;
  private final Map<String, List<TenantConfigUpdateCommand>> commandsByTenantId; 
  
  
  public UpdateTenantConfigVisitor(List<TenantConfigUpdateCommand> commands, DocumentStore ctx) {
    super();
    this.ctx = ctx;
    final var config = ctx.getConfig();
    this.commandsByTenantId = commands.stream()
        .collect(Collectors.groupingBy(TenantConfigUpdateCommand::getTenantConfigId));
    this.tenantIds = new ArrayList<>(commandsByTenantId.keySet());
    this.commitBuilder = config.getClient().doc().commit().modifyManyBranches()
        .repoId(config.getRepoId())
        .message("Update Tenants: " + commandsByTenantId.size())
        .author(config.getAuthor().get());
  }

  @Override
  public DocObjectsQuery start(DocumentConfig config, DocObjectsQuery builder) {
    return builder.matchIds(tenantIds).branchName(MainBranch.HEAD_NAME);
  }

  @Override
  public DocObjects visitEnvelope(DocumentConfig config, QueryEnvelope<DocObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("GET_TENANTS_BY_IDS_FOR_UPDATE_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(tenantIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw DocumentStoreException.builder("GET_TENANTS_BY_IDS_FOR_UPDATE_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(tenantIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    if(tenantIds.size() != result.getDocs().size()) {
      throw new DocumentStoreException("TENANTS_UPDATE_FAIL_MISSING_TENANTS", JsonObject.of("failedUpdates", tenantIds));
    }
    return result;
  }

  @Override
  public Uni<List<TenantConfig>> end(DocumentConfig config, DocObjects blob) {
    final var updatedTenants = blob.accept((Doc doc, DocBranch docBranch, DocCommit commit, List<DocLog> log) -> {
      final var start = docBranch.getValue().mapTo(ImmutableTenantConfig.class);
      final var commands = commandsByTenantId.get(start.getId());
      final var updated = new TenantConfigCommandVisitor(start, ctx.getConfig()).visitTransaction(commands);
      this.commitBuilder.item()
        .branchName(updated.getId())
        .append(JsonObject.mapFrom(updated));
      return updated;
    });
    
    return commitBuilder.build().onItem().transform(commit -> {
      if(commit.getStatus() != CommitResultStatus.OK) {
        final var failedUpdates = tenantIds.stream().collect(Collectors.joining(",", "{", "}"));
        throw new DocumentStoreException("TENANTS_UPDATE_FAIL", JsonObject.of("failedUpdates", failedUpdates), DocumentStoreException.convertMessages(commit));
      }
      return updatedTenants;
    });
  }
}
