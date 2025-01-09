package io.digiexpress.eveli.assets.spi.builders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*-
 * #%L
 * stencil-persistence
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.digiexpress.eveli.assets.api.EveliAssetClient;
import io.digiexpress.eveli.assets.api.EveliAssetClient.AssetState;
import io.digiexpress.eveli.assets.api.EveliAssetClient.AssetStatus;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Entity;
import io.digiexpress.eveli.assets.api.EveliAssetClient.EntityBody;
import io.digiexpress.eveli.assets.api.EveliAssetClient.EntityType;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Publication;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Workflow;
import io.digiexpress.eveli.assets.api.EveliAssetClient.WorkflowTag;
import io.digiexpress.eveli.assets.api.EveliAssetClientConfig;
import io.digiexpress.eveli.assets.api.ImmutableAssetState;
import io.digiexpress.eveli.assets.spi.exceptions.QueryException;
import io.resys.thena.api.actions.GitPullActions.MatchCriteria;
import io.resys.thena.api.entities.git.Blob;
import io.resys.thena.api.entities.git.Tree;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.spi.exceptions.RefException;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class QueryBuilderImpl implements EveliAssetClient.QueryBuilder {
  private final EveliAssetClientConfig config;


  @SuppressWarnings("unchecked")
  public <T extends EntityBody> Uni<List<Entity<T>>> findAnyById(List<String> ids, EntityType type) {
    return config.getClient()
      .git(config.getRepoName())
      .pull().pullQuery()
      .branchNameOrCommitOrTag(config.getHeadName())
      .docId(ids)

      .matchBy(Arrays.asList(MatchCriteria.equalsTo("type", type.name())))
      .findAll().onItem()
      .transform(state -> {
        if(state.getStatus() != QueryEnvelopeStatus.OK) {
          throw new QueryException(String.join(",", ids), type, state);  
        }
  
        return state.getObjects().getBlob().stream()
          .map(blob -> (Entity<T>) config.getDeserializer().fromString(type, blob.getValue().encode()))
          .collect(Collectors.toList());
        
      });
  }

  @Override
  public Uni<List<Entity<WorkflowTag>>> findAllWorkflowTags() {
    return findAnyById(Collections.emptyList(), EntityType.WORKFLOW_TAG);
  }
  @Override
  public Uni<List<Entity<Workflow>>> findAllWorkflows() {
    return findAnyById(Collections.emptyList(), EntityType.WORKFLOW);
  }
  @Override
  public Uni<List<Entity<Workflow>>> findAllWorkflowsById(List<String> ids) {
    final Uni<List<Entity<Workflow>>> wks = findAnyById(ids, EntityType.WORKFLOW);
    return wks;
  }
  @Override
  public Uni<List<Entity<Publication>>> findAllPublicationsById(List<String> ids) {
    final Uni<List<Entity<Publication>>> wks = findAnyById(ids, EntityType.PUBLICATION);
    return wks;
  }
  
  @Override
  public Uni<List<Entity<Publication>>> findAllPublications() {
    final Uni<List<Entity<Publication>>> wks = findAnyById(Collections.emptyList(), EntityType.PUBLICATION);
    return wks;
  }
  
  @Override
  public Uni<List<Entity<WorkflowTag>>> findAllWorkflowTagsById(List<String> ids) {
    final Uni<List<Entity<WorkflowTag>>> wks = findAnyById(ids, EntityType.WORKFLOW_TAG);
    return wks;
  }
  @Override
  public Uni<Optional<Entity<Publication>>> findOnePublicationByName(String name) {
    return config.getClient()
    .git(config.getRepoName())
    .pull().pullQuery()
    .branchNameOrCommitOrTag(config.getHeadName())
    .matchBy(Arrays.asList(MatchCriteria.equalsTo("type", EntityType.PUBLICATION.name()), MatchCriteria.equalsTo("body.name", name)))
    .findAll().onItem()
    .transform(state -> {
      if(state.getStatus() != QueryEnvelopeStatus.OK) {
        throw new QueryException("failed to find any publication", EntityType.PUBLICATION, state);  
      }

      return state.getObjects().getBlob().stream()
        .map(blob -> {
          final Entity<Publication> result = config.getDeserializer().fromString(EntityType.PUBLICATION, blob.getValue().encode());
          return result;
        })
        .findAny();
    });
  }
  @Override
  public Uni<Optional<Entity<Workflow>>> findOneWorkflowById(String id) {
    return config.getClient()
    .git(config.getRepoName())
    .pull().pullQuery()
    .branchNameOrCommitOrTag(config.getHeadName())
    .docId(id)

    .findAll().onItem()
    .transform(state -> {
      if(state.getStatus() != QueryEnvelopeStatus.OK) {
        throw new QueryException("failed to find any workflows", EntityType.WORKFLOW, state);  
      }

      return state.getObjects().getBlob().stream()
        .map(blob -> {
          final Entity<Workflow> result = config.getDeserializer().fromString(EntityType.WORKFLOW, blob.getValue().encode());
          return result;
        })
        .findAny();
    });
  }
  @Override
  public Uni<Optional<Entity<Workflow>>> findOneWorkflowByName(String name) {
    return config.getClient()
    .git(config.getRepoName())
    .pull().pullQuery()
    .branchNameOrCommitOrTag(config.getHeadName())
    .matchBy(Arrays.asList(MatchCriteria.equalsTo("type", EntityType.WORKFLOW.name()), MatchCriteria.equalsTo("body.name", name)))
    .findAll().onItem()
    .transform(state -> {
      if(state.getStatus() != QueryEnvelopeStatus.OK) {
        throw new QueryException("failed to find any workflows", EntityType.WORKFLOW, state);  
      }

      return state.getObjects().getBlob().stream()
        .map(blob -> {
          final Entity<Workflow> result = config.getDeserializer().fromString(EntityType.WORKFLOW, blob.getValue().encode());
          return result;
        })
        .findAny();
    });
  }
  @Override
  public Uni<Optional<Entity<WorkflowTag>>> findOneWorkflowTagByName(String name) {
    return config.getClient()
    .git(config.getRepoName())
    .pull().pullQuery()
    .branchNameOrCommitOrTag(config.getHeadName())
    .matchBy(Arrays.asList(MatchCriteria.equalsTo("type", EntityType.WORKFLOW_TAG.name()), MatchCriteria.equalsTo("body.name", name)))
    .findAll().onItem()
    .transform(state -> {
      if(state.getStatus() != QueryEnvelopeStatus.OK) {
        throw new QueryException("failed to find any workflow tags", EntityType.WORKFLOW_TAG, state);  
      }

      return state.getObjects().getBlob().stream()
        .map(blob -> {
          final Entity<WorkflowTag> result = config.getDeserializer().fromString(EntityType.WORKFLOW_TAG, blob.getValue().encode());
          return result;
        })
        .findAny();
    });
  }

  
  @Override
  public Uni<AssetState> head() {
    final var siteName = config.getRepoName() + ":" + config.getHeadName();
    return config.getClient()        
      .git(config.getRepoName())
      .branch().branchQuery().branchName(config.getHeadName())
      .get().onItem()
      .transformToUni(repo -> {
        if(repo == null) {
         return Uni.createFrom().item(ImmutableAssetState.builder()
              .name(siteName)
              .repoStatus(AssetStatus.NOT_CREATED)
              .build()); 
        }
      
        return config.getClient()
            
            .git(config.getRepoName())
            .branch()
            .branchQuery()
            .branchName(config.getHeadName())
            .docsIncluded()
            .get()
            
            .onItem()
            .transform(state -> {
              if(state.getStatus() == QueryEnvelopeStatus.ERROR) {
                throw new RefException(siteName, state);
              }

              // Nothing present
              if(state.getObjects() == null) {
                return ImmutableAssetState.builder()
                    .name(siteName)
                    .repoStatus(AssetStatus.EMPTY)
                    .build();
              }
              
              final var commit = state.getObjects().getCommit();
              final var blobs = state.getObjects().getBlobs();
              final var tree = state.getObjects().getTree();
              final var builder = mapTree(tree, blobs, config);
              return builder
                  .commit(commit.getId())
                  .name(siteName)
                  .repoStatus(AssetStatus.OK)
                  .build();
            });
      });
  }
  
  @SuppressWarnings("unchecked")
  public static ImmutableAssetState.Builder mapTree(Tree tree, Map<String, Blob> blobs, EveliAssetClientConfig config) {
    final var builder = ImmutableAssetState.builder();
    for(final var treeValue : tree.getValues().values()) {
      final var blob = blobs.get(treeValue.getBlob());
      final var entity = config.getDeserializer().fromString(blob.getValue().encode());
      final var id = entity.getId();
      
      switch (entity.getType()) {
      case PUBLICATION:
        builder.putPublications(id, (Entity<Publication>) entity);
        break;
      case WORKFLOW:
        builder.putWorkflows(id, (Entity<Workflow>) entity);
        break;
      case WORKFLOW_TAG:
        builder.putWorkflowTags(id, (Entity<WorkflowTag>) entity);
        break;
      default: throw new RuntimeException("Don't know how to convert entity: " + entity.toString() + "!");
      }
    }
    return builder;
  }

}
