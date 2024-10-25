package io.digiexpress.eveli.assets.api;

/*-
 * #%L
 * eveli-assets
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.api.form.Form;
import io.digiexpress.eveli.assets.api.EveliAssetClient.AssetState;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Entity;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Publication;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Workflow;
import io.digiexpress.eveli.assets.api.EveliAssetClient.WorkflowTag;
import io.resys.hdes.client.api.ast.AstTag;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.ImmutableWorkflowMutator;
import io.thestencil.client.api.StencilClient;



public interface EveliAssetComposer {
  CreateBuilder create();
  UpdateBuilder update();
  DeleteBuilder delete();
  MigrationBuilder migration();
  DeploymentBuilder deployment();
  
  AnyTagQuery anyAssetTagQuery();
  PublicationQuery publicationQuery();
  WorkflowQuery workflowQuery();
  WorkflowTagQuery workflowTagQuery();
  
  

  // transient, downloaded entity for production
  @Value.Immutable
  @JsonSerialize(as = ImmutableDeployment.class)
  @JsonDeserialize(as = ImmutableDeployment.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  interface Deployment {
    Publication getSource();
    LocalDateTime getCreated(); // download time
    
    WorkflowTag getWorkflowTag();
    StencilClient.Release getStencilTag();
    AstTag getWrenchTag();
    List<Form> getDialobTag();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableAssetBatch.class)
  @JsonDeserialize(as = ImmutableAssetBatch.class)
  interface AssetBatch extends CreateBuilder.CreateCommand {
    List<CreateWorkflow> getWorkflows();
    List<CreateWorkflowTag> getWorkflowTags();
    List<CreatePublication> getPublications();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableCreateWorkflowTag.class)
  @JsonDeserialize(as = ImmutableCreateWorkflowTag.class)
  interface CreateWorkflowTag extends CreateBuilder.CreateCommand {
    String getName();
    String getDescription();
    @Nullable
    String getUser();
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableCreatePublication.class)
  @JsonDeserialize(as = ImmutableCreatePublication.class)
  interface CreatePublication extends CreateBuilder.CreateCommand {
    String getStencilTag();
    String getWrenchTag();
    String getWorkflowTag();
    
    @Nullable String getUser();
    @Nullable String getName();
    @Nullable String getDescription();
    @Nullable LocalDateTime getLiveDate();
    
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableCreateWorkflow.class)
  @JsonDeserialize(as = ImmutableCreateWorkflow.class)
  interface CreateWorkflow extends CreateBuilder.CreateCommand {
    String getName();
    String getFormName();
    String getFormTag();
    String getFlowName();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableWorkflowMutator.class)
  @JsonDeserialize(as = ImmutableWorkflowMutator.class)
  interface WorkflowMutator {
    String getId();
    @Nullable String getName();
    @Nullable String getFormName();
    @Nullable String getFormTag();
    @Nullable String getFlowName();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableAnyAssetTag.class)
  @JsonDeserialize(as = ImmutableAnyAssetTag.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  interface AnyAssetTag {
    String getId();
    String getName();
    String getDescription();
    LocalDateTime getCreated();
    AssetTagType getType();
    
    // some models don't store user info for tag
    @Nullable String getUser();
  }
  
  enum AssetTagType {
    WRENCH, STENCIL, WORKFLOW
  }
  

  interface WorkflowQuery {
    Uni<List<Entity<Workflow>>> findAll();
    Uni<Optional<Entity<Workflow>>> findOneByName(String name);
  }
  
  interface AnyTagQuery {
    Uni<List<AnyAssetTag>> findAllByType(AssetTagType type);
  }
  
  interface PublicationQuery {
    Uni<List<Entity<Publication>>> findAll();
    Uni<Optional<Entity<Publication>>> findOneByName(String name);
  }
  
  interface WorkflowTagQuery {
    Uni<List<Entity<WorkflowTag>>> findAll();
    Uni<Optional<Entity<WorkflowTag>>> findOneByName(String name);
  }
  
  interface DeploymentBuilder {
    DeploymentBuilder id(String idOrName);
    Uni<Optional<Deployment>> build();
  }  
  
  interface CreateBuilder {
    Uni<AssetState> repo();
    Uni<Entity<Workflow>> workflow(CreateWorkflow init);
    Uni<Entity<WorkflowTag>> workflowTag(CreateWorkflowTag init);
    Uni<Entity<Publication>> publication(CreatePublication init);
    Uni<List<Entity<?>>> batch(AssetBatch batch);
    
    interface CreateCommand extends Serializable {}
  }
  
  interface UpdateBuilder {
    Uni<Entity<Workflow>> workflow(WorkflowMutator changes);
  }

  interface DeleteBuilder {
    Uni<Entity<Publication>> publication(String publicationId);
    Uni<Entity<Workflow>> workflow(String workflowId);
    Uni<Entity<WorkflowTag>> workflowTag(String workflowTagId);
  }

  interface MigrationBuilder {
    Uni<AssetState> importData(Deployment sites);
    Uni<AssetState> importData(AssetState sites);  
  }
}
