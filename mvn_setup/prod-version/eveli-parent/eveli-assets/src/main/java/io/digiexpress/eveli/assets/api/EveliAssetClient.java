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
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.docdb.api.actions.ObjectsActions.BlobObject;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsResult;
import io.smallrye.mutiny.Uni;


public interface EveliAssetClient {

  EveliAssetClientConfig getConfig();
  CrudBuilder crudBuilder();
  QueryBuilder queryBuilder();
  RepoBuilder repoBuilder();
  
  interface CrudBuilder {
    <T extends EntityBody> Uni<EntityState<T>> get(String blobId, EntityType type);
    <T extends EntityBody> Uni<Entity<T>> save(Entity<T> toBeSaved);
    <T extends EntityBody> Uni<Entity<T>> create(Entity<T> toBeSaved);
    <T extends EntityBody> Uni<Entity<T>> delete(Entity<T> toBeDeleted);
    Uni<List<Entity<?>>> saveAll(List<Entity<?>> toBeSaved);
    Uni<List<Entity<?>>> batch(AssetBatchCommand batch);
  }
  
  
  interface QueryBuilder {
    Uni<List<Entity<Workflow>>> findAllWorkflowsById(List<String> ids);
    Uni<List<Entity<Publication>>> findAllPublicationsById(List<String> ids);
    Uni<List<Entity<WorkflowTag>>> findAllWorkflowTagsById(List<String> ids);
    Uni<List<Entity<WorkflowTag>>> findAllWorkflowTags();
    Uni<List<Entity<Workflow>>> findAllWorkflows();
    Uni<Optional<Entity<Workflow>>> findOneWorkflowByName(String name);
    Uni<Optional<Entity<Workflow>>> findOneWorkflowById(String id);
    Uni<Optional<Entity<WorkflowTag>>> findOneWorkflowTagByName(String name);    
    Uni<AssetState> head();
    
    Uni<List<Entity<Publication>>> findAllPublications();    
    Uni<Optional<Entity<Publication>>> findOnePublicationByName(String name);    
  }
  
  interface RepoBuilder {
    RepoBuilder repoName(String repoName);
    RepoBuilder headName(String headName);
    Uni<EveliAssetClient> create();    
    EveliAssetClient build();
    Uni<Boolean> createIfNot();
  }
  
  
  enum EntityType {
    WORKFLOW, WORKFLOW_TAG, 
    PUBLICATION // Pointer to all other releases
  }

  enum AssetStatus {
    OK, ERRORS, NOT_CREATED, EMPTY, RELEASE
  }
  
  interface EntityBody extends Serializable { }
  
  @Value.Immutable
  @SuppressWarnings("rawtypes")
  interface AssetBatchCommand {
    List<Entity> getToBeCreated();
    List<Entity> getToBeSaved();
    List<Entity> getToBeDeleted();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableAssetState.class)
  @JsonDeserialize(as = ImmutableAssetState.class)
  interface AssetState {
    String getName();
    @Nullable
    String getCommit();
    AssetStatus getRepoStatus();
    Map<String, Entity<Workflow>> getWorkflows();
    Map<String, Entity<Publication>> getPublications();
    Map<String, Entity<WorkflowTag>> getWorkflowTags();
  }
  

  @Value.Immutable
  @JsonSerialize(as = ImmutableEntity.class)
  @JsonDeserialize(as = ImmutableEntity.class)
  interface Entity<T extends EntityBody> extends Serializable {
    String getId();
    EntityType getType();
    T getBody();
  }




  @Value.Immutable
  @JsonSerialize(as = ImmutableWorkflow.class)
  @JsonDeserialize(as = ImmutableWorkflow.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  interface Workflow extends EntityBody {
    String getName();
    String getFormName();
    String getFormTag();
    String getFlowName();
    ZonedDateTime getUpdated();
  }
  
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableWorkflowTag.class)
  @JsonDeserialize(as = ImmutableWorkflowTag.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  interface WorkflowTag extends EntityBody {
    String getParentCommit();
    List<Workflow> getEntries();
  
    String getName();
    String getDescription();
    // some models don't store user info for tag
    @Nullable
    String getUser();
    LocalDateTime getCreated();
  }
  
  

  @Value.Immutable
  @JsonSerialize(as = ImmutablePublication.class)
  @JsonDeserialize(as = ImmutablePublication.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  interface Publication extends EntityBody {
    String getName();
    String getDescription();
    LocalDateTime getCreated();
    LocalDateTime getLiveDate();

    // some models don't store user info for tag
    @Nullable
    String getUser();
    
    String getStencilTagName();
    String getWrenchTagName();
    String getWorkflowTagName();
  }
    
  @Value.Immutable
  interface EntityState<T extends EntityBody> {
    ObjectsResult<BlobObject> getSrc();
    Entity<T> getEntity();
  }

}
