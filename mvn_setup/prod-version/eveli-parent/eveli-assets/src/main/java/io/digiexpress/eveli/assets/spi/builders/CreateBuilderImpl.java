package io.digiexpress.eveli.assets.spi.builders;

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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import io.digiexpress.eveli.assets.api.EveliAssetClient;
import io.digiexpress.eveli.assets.api.EveliAssetClient.AssetState;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Entity;
import io.digiexpress.eveli.assets.api.EveliAssetClient.EntityBody;
import io.digiexpress.eveli.assets.api.EveliAssetClient.EntityType;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Publication;
import io.digiexpress.eveli.assets.api.EveliAssetComposer;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.AssetBatch;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.CreatePublication;
import io.digiexpress.eveli.assets.api.ImmutableEntity;
import io.digiexpress.eveli.assets.api.ImmutablePublication;
import io.digiexpress.eveli.assets.spi.exceptions.ConstraintException;
import io.digiexpress.eveli.assets.spi.visitors.BatchSiteCommandVisitor;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.ImmutableCreateEntity;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.ImmutableCreateRelease;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.spi.StencilComposerImpl;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateBuilderImpl implements EveliAssetComposer.CreateBuilder {
  private final EveliAssetClient client;
  private final StencilClient stencilClient;
  private final HdesClient hdesClient;
  private final DialobClient dialobClient;

  @Override
  public Uni<List<Entity<?>>> batch(AssetBatch batch) {
    final Uni<AssetState> query = client.queryBuilder().head();
    return query.onItem().transformToUni(state -> client.crudBuilder().batch(new BatchSiteCommandVisitor(state, client, dialobClient).visit(batch)));
  }
  @Override
  public Uni<Entity<Publication>> publication(CreatePublication init) {
    final Uni<AssetState> query = client.queryBuilder().head();
    return query.onItem().transformToUni(state -> client.crudBuilder().create(publication(init, state, client)))
        .onItem().transformToUni(createdTag -> {
          
          
          final var stencilRelease = init.getStencilTag() == null ? 
              new StencilComposerImpl(stencilClient).create().release(ImmutableCreateRelease.builder()
                  .name(createdTag.getBody().getStencilTagName())
                  .note("auto-created")
                  .build()):
              Uni.createFrom().nullItem();
          
          final var wrenchRelease = init.getWrenchTag() == null ? 
              new HdesComposerImpl(hdesClient).create(ImmutableCreateEntity.builder()
                  .type(AstBodyType.TAG)
                  .name(createdTag.getBody().getWrenchTagName())
                  .desc("auto-created")
                  .build()):
              Uni.createFrom().nullItem();
          

              Uni.createFrom().nullItem();
          
          return Uni.combine().all().unis(stencilRelease, wrenchRelease).with((autoCreatedTags) -> {
            return createdTag;
          });
        });
  }
  public static Entity<Publication> publication(CreatePublication init, AssetState state, EveliAssetClient client) {
    final var gid = client.getConfig().getGidProvider().getNextId();
    final var name = Optional.ofNullable(init.getName()).orElse("generic-" + gid);
    
    
    
    
    final var article = ImmutablePublication.builder()
        .created(LocalDateTime.now())
        .name(name)
        .description(Optional.ofNullable(init.getDescription()).orElse("nondescript"))
        .liveDate(init.getLiveDate() == null ? LocalDateTime.now() : init.getLiveDate())
        .user(Optional.ofNullable(init.getUser()).orElse(""))
        .wrenchTagName(Optional.ofNullable(init.getWrenchTag()).orElse(name))
        .stencilTagName(Optional.ofNullable(init.getStencilTag()).orElse(name))
        .build();
    final Entity<Publication> entity = ImmutableEntity.<Publication>builder()
        .id(gid)
        .type(EntityType.PUBLICATION)
        .body(article)
        .build();
    
    final var duplicate = state.getPublications().values().stream()
        .filter(p -> p.getBody().getName().equals(init.getName()))
        .findFirst();
    
    if(duplicate.isPresent()) {
      throw new ConstraintException(entity, "Publication: '" + init.getName() + "' already exists!");
    }
    return assertUniqueId(entity, state);
  }
  
  @Override
  public Uni<AssetState> repo() {
    return client.repoBuilder().create().onItem().transformToUni(e -> e.queryBuilder().head());
  }
  
  private static <T extends EntityBody> Entity<T> assertUniqueId(Entity<T> entity, AssetState state) {
    if( state.getPublications().containsKey(entity.getId())) {
      
      throw new ConstraintException(entity, "Entity with id: '" + entity.getId() + "' already exist!");  
    }
    
    return entity;
  }
}
