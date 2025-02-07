package io.digiexpress.eveli.envir.api;

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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.api.form.Form;
import io.resys.hdes.client.api.HdesClient.ExecutorBuilder;
import io.resys.hdes.client.api.ast.AstTag;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;


public interface EveliEnvirClient {
  CreateOneDeployment createOneDeployment();
  ModifyOneDeployment modifyOneDeployment();
  
  
  EveliRuntimeQuery runtimeQuery();
  EveliDeploymentCompiler deploymentCompiler();
  
  DeploymentQuery deploymentQuery();
  DeploymentBuilder deploymentBuilder();
  
  
  // moves given deployment to 'DEPLOYED' and sets any other 'DEPLOYED' entries into 'READY' status
  interface DeploymentBuilder {
    DeploymentBuilder userId(String userId);
    DeploymentBuilder deploymentId(String id);
    Uni<EveliDeployment> build();
  }
  
  interface EveliDeploymentCompiler {
    EveliDeploymentCompiler userId(String userId);
    EveliDeploymentCompiler deploymentId(String id);
    Uni<EveliDeployment> compile(); // build all deployments with status "BUILDING", changes them to READY OR ERROR
  }
  
  interface DeploymentQuery {
    DeploymentQuery status(EveliDeploymentStatus ...status);
    DeploymentQuery emptyBranchBody(boolean emptyBranchBody); // don't fetch the branch contents, default is true
    Uni<EveliDeployment> getOneById(String id);
    Uni<List<EveliDeployment>> findAll(); // will not load assets
  }
  
  interface EveliRuntimeQuery {
    Uni<EveliRuntime> getOne();
    Uni<Optional<EveliRuntime>> findOne();
  }
  
  interface CreateOneDeployment {
    CreateOneDeployment userId(String userId);
    CreateOneDeployment name(String name);    
    CreateOneDeployment startsAt(OffsetDateTime startsAt);
    CreateOneDeployment stencil(SiteState stencil);
    CreateOneDeployment wrench(AstTag wrench);
    CreateOneDeployment dialob(List<Form> dialob);
    Uni<EveliDeployment> build();
  }
  
  
  interface ModifyOneDeployment {
    ModifyOneDeployment id(String idOrName);
    ModifyOneDeployment startsAt(OffsetDateTime startsAt);
    ModifyOneDeployment status(EveliDeploymentStatus status);
    Uni<EveliDeployment> build();
  }
  
  @JsonSerialize(as = ImmutableEveliDeployment.class)
  @JsonDeserialize(as = ImmutableEveliDeployment.class)
  @Value.Immutable
  interface EveliDeployment {
    String getId();
    String getName();
    @Nullable String getExternalId();

    EveliDeploymentStatus getStatus();
    @Nullable JsonObject getErrors();

    OffsetDateTime getCreatedAt();
    OffsetDateTime getStartsAt();

    // Null when user has requested sources to be not loaded on api level
    @Nullable EveliSources getSources();
  }
  
  @JsonSerialize(as = ImmutableEveliSources.class)
  @JsonDeserialize(as = ImmutableEveliSources.class)
  @Value.Immutable
  interface EveliSources {
    SiteState getStencil();
    AstTag getWrench();
    List<Form> getDialob();
  }
  
  enum EveliDeploymentStatus {
    BUILDING, READY, ERROR, DEPLOYED
  }

  interface EveliRuntime {
    String getName();
    String getDeploymentId();
    String getWrenchTagName();
    String getStencilTagName();
    
    ExecutorBuilder getWrench();
    Sites getStencil(OffsetDateTime now);
  }
}
