package io.digiexpress.eveli.assets.spi;

import java.util.List;
import java.util.Optional;

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

import io.digiexpress.eveli.assets.api.EveliAssetClient;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Entity;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Publication;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Workflow;
import io.digiexpress.eveli.assets.api.EveliAssetClient.WorkflowTag;
import io.digiexpress.eveli.assets.api.EveliAssetComposer;
import io.digiexpress.eveli.assets.api.ImmutableAnyAssetTag;
import io.digiexpress.eveli.assets.spi.builders.CreateBuilderImpl;
import io.digiexpress.eveli.assets.spi.builders.UpdateBuilderImpl;
import io.resys.hdes.client.api.HdesClient;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EveliAssetsComposerImpl implements EveliAssetComposer {
  private final EveliAssetClient client;
  private final StencilClient stencilClient;
  private final HdesClient hdesClient;
  
  @Override
  public CreateBuilder create() {
    return new CreateBuilderImpl(client, stencilClient, hdesClient);
  }

  @Override
  public UpdateBuilder update() {
    return new UpdateBuilderImpl(client);
  }

  @Override
  public DeleteBuilder delete() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public MigrationBuilder migration() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DeploymentBuilder deployment() {
    // TODO Auto-generated method stub
    return null;
  }

  public EveliAssetClient getClient() {
    return client;
  }

  @Override
  public AnyTagQuery anyAssetTagQuery() {
    return new AnyTagQuery() {

      @Override
      public Uni<List<AnyAssetTag>> findAllByType(AssetTagType type) {
        switch (type) {
        case STENCIL: {
          return stencilClient.getStore().query().head().onItem().transform(state -> {
            
            return state.getReleases().values().stream()
                .map(release ->  (AnyAssetTag) ImmutableAnyAssetTag.builder()
                .created(release.getBody().getCreated())
                .type(AssetTagType.STENCIL)
                .description(release.getBody().getNote())
                .id(release.getId())
                .name(release.getBody().getName())

                .user("not-available")
                .build())
                .toList();
            
          });
        }
        case WRENCH: {
          return hdesClient.store().query().get().onItem().transform(state -> {
            
            
            return state.getTags().values().stream()
                .map(release -> hdesClient.ast().commands(release.getBody()).tag())
                .map(release ->  (AnyAssetTag) ImmutableAnyAssetTag.builder()
                .created(release.getCreated())
                .type(AssetTagType.WRENCH)
                .description(release.getDescription())
                .id("no-release-id")
                .name(release.getName())

                .user("not-available")
                .build())
                .toList();
          });
        }
        case WORKFLOW: {
          return workflowTagQuery().findAll().onItem().transform(tags -> {
            return tags.stream().map(tag -> (AnyAssetTag) ImmutableAnyAssetTag.builder()
                .created(tag.getBody().getCreated())
                .type(AssetTagType.WORKFLOW)
                .description(tag.getBody().getDescription())
                .id(tag.getId())
                .name(tag.getBody().getName())

                .user(tag.getBody().getUser())
                .build()).toList();
          });
          
          
        }
        default:
          throw new IllegalArgumentException("Unexpected value: " + type);
        }
      }
    };
  
  }

  @Override
  public PublicationQuery publicationQuery() {
    return new PublicationQuery() {

      @Override
      public Uni<List<Entity<Publication>>> findAll() {
        return client.queryBuilder().findAllPublications();
      }

      @Override
      public Uni<Optional<Entity<Publication>>> findOneByName(String name) {
        return client.queryBuilder().findOnePublicationByName(name);
      }
      
    };
  }

  @Override
  public WorkflowQuery workflowQuery() {
    return new WorkflowQuery() {

      @Override
      public Uni<List<Entity<Workflow>>> findAll() {
        return client.queryBuilder().findAllWorkflows();
      }

      @Override
      public Uni<Optional<Entity<Workflow>>> findOneByName(String name) {
        return client.queryBuilder().findOneWorkflowByName(name);
      }

      @Override
      public Uni<Optional<Entity<Workflow>>> findOneById(String id) {
        return client.queryBuilder().findOneWorkflowById(id);
      }
      
    };
  }

  @Override
  public WorkflowTagQuery workflowTagQuery() {
    return new WorkflowTagQuery() {
      
      @Override
      public Uni<Optional<Entity<WorkflowTag>>> findOneByName(String name) {
        return client.queryBuilder().findOneWorkflowTagByName(name);
      }
      
      @Override
      public Uni<List<Entity<WorkflowTag>>> findAll() {
        return client.queryBuilder().findAllWorkflowTags();
      }
    };
  }
  

}
