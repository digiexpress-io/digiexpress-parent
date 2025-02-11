package io.digiexpress.eveli.envir.spi.actions;

/*-
 * #%L
 * eveli-envir
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import io.digiexpress.eveli.envir.api.EveliEnvirClient.DeploymentQuery;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeploymentStatus;
import io.digiexpress.eveli.envir.spi.EveliEnvirStore;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.DocStoreException;
import io.resys.thena.spi.ThenaDocConfig;
import io.resys.thena.spi.ThenaDocConfig.DocObjectsVisitor;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class DeploymentQueryImpl implements DeploymentQuery, DocObjectsVisitor<List<EveliDeployment>>{
  private final EveliEnvirStore ctx;
  private final List<String> ids = new ArrayList<>();
  private final List<EveliDeploymentStatus> status = new ArrayList<>();
  private boolean emptyBranchBody = true;
  private boolean excludeExternal = false;

  @Override
  public DeploymentQuery status(EveliDeploymentStatus ...status) {
    this.status.addAll(Arrays.asList(status));
    return this;
  }
  
  private Uni<Optional<EveliDeployment>> getExternal() {
    if(excludeExternal) {
      return Uni.createFrom().item(Optional.empty());
    }
    return ctx.getExternalProvider().getDeployment();
  }
  
  @Override
  public Uni<EveliDeployment> getOneById(String id) {
    ids.add(id);    
    return getExternal().onItem()
      .transformToUni(predefined -> {
        
        if(predefined.isPresent() && (predefined.get().getId().equals(id) || predefined.get().getName().equals(id))) {
          return Uni.createFrom().item(predefined.get());
        }
        
        final var config = ctx.getConfig();
        return config.accept(this).onItem().transform(e -> {
          if(e.size() != 1) {
            throw DocStoreException.builder("GET_ONE_DEPLOYMENT_BY_ID_FAIL")
              .add(config)
              .add((m) -> m.addArgs(JsonObject.of("deploymentId", id).encode()))
              .build();
          }
          
          return e.iterator().next();
        });
      });
  }  
  
  @Override
  public Uni<List<EveliDeployment>> findAll() {
    return getExternal().onItem()
        .transformToUni(predefined -> {
          
          final var isIncludeExternal = predefined.isPresent() && (this.ids.isEmpty() || this.ids.contains(predefined.get().getId()));
          
          final var config = ctx.getConfig();
          return config.accept(this)
              .onItem().transform(e -> {
                
                final var builder = ImmutableList.<EveliDeployment>builder();
                if(isIncludeExternal) {
                  builder.add(predefined.get());
                }
                return builder.addAll(e).build();
              });
        });
  }  
  @Override
  public Uni<QueryEnvelope<DocTenantObjects>> start(ThenaDocConfig config, DocObjectsQuery builder) {
    if(status != null) {
      builder.docSubStatus(status.stream().map(n -> n.name()).toList());
    }

    if(emptyBranchBody) {
      builder.emptyBranchBody();
    }
    
    builder.docType(EveliEnvirStore.DOC_TYPE_DEPLOYMENT);
    if(!ids.isEmpty()) {
      return builder.findAll(ids);
    }
    return builder.findAll();
  }
  @Override
  public DocTenantObjects visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocTenantObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocStoreException.builder("FIND_ALL_DEPLOYMENTS_FAIL")
        .add(config, envelope)
        .add((m) -> m.addArgs(JsonObject.of("deploymentId-s-if-present", ids).encode()))
        .build();
    }
    return envelope.getObjects();
  }
  @Override
  public List<EveliDeployment> end(ThenaDocConfig config, DocTenantObjects ref) {
    if(ref == null) {
      return Collections.emptyList();
    }
    return ref.accept((
        Doc doc, 
        DocBranch docBranch, 
        Map<String, DocCommit> commit, 
        List<DocCommands> commands,
        List<DocCommitTree> trees) -> EveliEnvirStore.map(doc, Optional.ofNullable(docBranch))
    );
  }
}
