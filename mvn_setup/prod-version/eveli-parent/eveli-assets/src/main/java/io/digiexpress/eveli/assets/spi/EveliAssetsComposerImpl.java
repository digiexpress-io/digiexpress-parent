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
import io.digiexpress.eveli.assets.api.EveliAssetComposer;
import io.digiexpress.eveli.assets.api.ImmutableAnyAssetTag;
import io.digiexpress.eveli.assets.spi.builders.CreateBuilderImpl;
import io.digiexpress.eveli.assets.spi.builders.DeleteBuilderImpl;
import io.digiexpress.eveli.assets.spi.builders.DeploymentBuilderImpl;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.resys.hdes.client.api.HdesClient;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EveliAssetsComposerImpl implements EveliAssetComposer {
  private final EveliAssetClient client;
  private final StencilClient stencilClient;
  private final HdesClient hdesClient;
  private final DialobClient dialobClient;
  
  @Override
  public CreateBuilder create() {
    return new CreateBuilderImpl(client, stencilClient, hdesClient, dialobClient);
  }

  @Override
  public DeploymentBuilder deployment() {
    return new DeploymentBuilderImpl(client, stencilClient, hdesClient, dialobClient);
  }

  @Override
  public DeleteBuilder delete() {
    return new DeleteBuilderImpl(client);
  }

  @Override
  public MigrationBuilder migration() {
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
}
