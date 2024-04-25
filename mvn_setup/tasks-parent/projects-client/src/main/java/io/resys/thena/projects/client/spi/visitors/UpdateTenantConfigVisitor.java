package io.resys.thena.projects.client.spi.visitors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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

import io.resys.thena.api.actions.DocCommitActions.ModifyManyDocBranches;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.projects.client.api.model.ImmutableTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.TenantConfigUpdateCommand;
import io.resys.thena.projects.client.spi.ProjectStore;
import io.resys.thena.spi.DocStoreException;
import io.resys.thena.spi.ThenaDocConfig;
import io.resys.thena.spi.ThenaDocConfig.DocObjectsVisitor;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;


public class UpdateTenantConfigVisitor implements DocObjectsVisitor<Uni<List<TenantConfig>>> {
  private final ProjectStore ctx;
  private final List<String> tenantIds;
  private final ModifyManyDocBranches commitBuilder;
  private final Map<String, List<TenantConfigUpdateCommand>> commandsByTenantId; 
  
  
  public UpdateTenantConfigVisitor(List<TenantConfigUpdateCommand> commands, ProjectStore ctx) {
    super();
    this.ctx = ctx;
    final var config = ctx.getConfig();
    this.commandsByTenantId = commands.stream()
        .collect(Collectors.groupingBy(TenantConfigUpdateCommand::getTenantConfigId));
    this.tenantIds = new ArrayList<>(commandsByTenantId.keySet());
    this.commitBuilder = config.getClient().doc(config.getRepoId()).commit().modifyManyBranches()
        .commitMessage("Update Tenants: " + commandsByTenantId.size())
        .commitAuthor(config.getAuthor().get());
  }

  @Override
  public Uni<QueryEnvelope<DocTenantObjects>> start(ThenaDocConfig config, DocObjectsQuery builder) {
    return builder.docType(TenantConfig.TENANT_CONFIG).findAll(new ArrayList<>(tenantIds));
  }

  @Override
  public DocTenantObjects visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocTenantObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocStoreException.builder("GET_TENANTS_BY_IDS_FOR_UPDATE_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(tenantIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw DocStoreException.builder("GET_TENANTS_BY_IDS_FOR_UPDATE_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(tenantIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    if(tenantIds.size() != result.getDocs().size()) {
      throw new DocStoreException("TENANTS_UPDATE_FAIL_MISSING_TENANTS", JsonObject.of("failedUpdates", tenantIds));
    }
    return result;
  }

  @Override
  public Uni<List<TenantConfig>> end(ThenaDocConfig config, DocTenantObjects blob) {
    final var updatedTenants = blob
      .accept((Doc doc, 
          DocBranch docBranch, 
          Map<String, DocCommit> commit, 
          List<DocCommands> _commands,
          List<DocCommitTree> trees) -> {
        final var start = docBranch.getValue().mapTo(ImmutableTenantConfig.class);
        final var commands = commandsByTenantId.get(start.getId());
        final var updated = new TenantConfigCommandVisitor(start, ctx.getConfig()).visitTransaction(commands);
        this.commitBuilder.item()
          .branchName(updated.getItem1().getId())
          .replace(JsonObject.mapFrom(updated.getItem1()))
          .commands(updated.getItem2());
        return updated.getItem1();
      });
    
    return commitBuilder.build().onItem().transform(response -> {
      if(response.getStatus() != CommitResultStatus.OK) {
        final var failedUpdates = tenantIds.stream().collect(Collectors.joining(",", "{", "}"));
        throw new DocStoreException("TENANTS_UPDATE_FAIL", JsonObject.of("failedUpdates", failedUpdates), DocStoreException.convertMessages(response));
      }
      
      final Map<String, TenantConfig> configsById = new HashMap<>(updatedTenants.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
      
      response.getBranch().forEach(branch -> {
        final var next = FindAllTenantsVisitor.mapToUserProfile(branch);
        configsById.put(next.getId(), next);
      });
      return Collections.unmodifiableList(new ArrayList<>(configsById.values()));
    });
  }
}
