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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.digiexpress.eveli.assets.api.EveliAssetClient.AssetBatchCommand;
import io.digiexpress.eveli.assets.api.EveliAssetClient.CrudBuilder;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Entity;
import io.digiexpress.eveli.assets.api.EveliAssetClient.EntityBody;
import io.digiexpress.eveli.assets.api.EveliAssetClient.EntityState;
import io.digiexpress.eveli.assets.api.EveliAssetClient.EntityType;
import io.digiexpress.eveli.assets.api.EveliAssetClientConfig;
import io.digiexpress.eveli.assets.api.ImmutableEntityState;
import io.digiexpress.eveli.assets.spi.exceptions.DeleteException;
import io.digiexpress.eveli.assets.spi.exceptions.QueryException;
import io.digiexpress.eveli.assets.spi.exceptions.SaveException;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CrudBuilderImpl implements CrudBuilder {
  private final EveliAssetClientConfig config;

  @Override
  public <T extends EntityBody> Uni<Entity<T>> delete(Entity<T> toBeDeleted) {
    return config.getClient()
        .git(config.getRepoName())
        .commit().commitBuilder()
        .branchName(config.getHeadName())
        .message("delete type: '" + toBeDeleted.getType() + "', with id: '" + toBeDeleted.getId() + "'")
        .latestCommit()
        .author(config.getAuthorProvider().getAuthor())
        .remove(toBeDeleted.getId())
        .build().onItem().transform(commit -> {
          if(commit.getStatus() == CommitResultStatus.OK) {
            return toBeDeleted;
          }
          throw new DeleteException(toBeDeleted, commit);
        });
  }
  
  @Override
  public <T extends EntityBody> Uni<EntityState<T>> get(String blobId, EntityType type) {
    return config.getClient()
        .git(config.getRepoName())
        .pull().pullQuery()
        .branchNameOrCommitOrTag(config.getHeadName())
        .docId(blobId)
        .get().onItem()
        .transform(state -> {
          if(state.getStatus() != QueryEnvelopeStatus.OK) {
            throw new QueryException(blobId, type, state);  
          }
          Entity<T> start = config.getDeserializer()
              .fromString(type, state.getObjects().getBlob().getValue().encode());
          
          return ImmutableEntityState.<T>builder()
              .src(state)
              .entity(start)
              .build();
        });
  }
  
  @Override
  public <T extends EntityBody> Uni<Entity<T>> save(Entity<T> toBeSaved) {
    return config.getClient()
      .git(config.getRepoName())
      .commit().commitBuilder()
      .branchName(config.getHeadName())
      .message("update type: '" + toBeSaved.getType() + "', with id: '" + toBeSaved.getId() + "'")
      .latestCommit()
      .author(config.getAuthorProvider().getAuthor())
      .append(toBeSaved.getId(), config.getSerializer().toString(toBeSaved))
      .build().onItem().transform(commit -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return toBeSaved;
        }
        throw new SaveException(toBeSaved, commit);
      });
  }
  
  @Override
  public <T extends EntityBody> Uni<Entity<T>> create(Entity<T> toBeSaved) {
    return config.getClient()
      .git(config.getRepoName())
      .commit().commitBuilder()
      .branchName(config.getHeadName())
      .message("create type: '" + toBeSaved.getType() + "', with id: '" + toBeSaved.getId() + "'")
      .latestCommit()
      .author(config.getAuthorProvider().getAuthor())
      .append(toBeSaved.getId(), config.getSerializer().toString(toBeSaved))
      .build().onItem().transform(commit -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return toBeSaved;
        }
        throw new SaveException(toBeSaved, commit);
      });
  }

  @Override
  public Uni<List<Entity<?>>> saveAll(List<Entity<?>> entities) {
    final var commitBuilder = config.getClient()
        .git(config.getRepoName())
        .commit().commitBuilder()
        .branchName(config.getHeadName());
    
    final Entity<?> first = entities.iterator().next();
    
    for(final var target : entities) {
      commitBuilder.append(target.getId(), config.getSerializer().toString(target));
    }
    
    return commitBuilder
        .message("update type: '" + first.getType() + "', with id: '" + first.getId() + "'")
        .latestCommit()
        .author(config.getAuthorProvider().getAuthor())
        .build().onItem().transform(commit -> {
          if(commit.getStatus() == CommitResultStatus.OK) {
            return entities;
          }
          throw new SaveException(first, commit);
        });
  }

  @Override
  public Uni<List<Entity<?>>> batch(AssetBatchCommand batch) {
    if(batch.getToBeDeleted().isEmpty() && batch.getToBeDeleted().isEmpty() && batch.getToBeCreated().isEmpty()) {
      return Uni.createFrom().item(Collections.emptyList());
    }
    
    final List<Entity<?>> all = new ArrayList<Entity<?>>();
    final var commitBuilder = config.getClient()
        .git(config.getRepoName())
        .commit().commitBuilder()
        .branchName(config.getHeadName());

    for(final var target : batch.getToBeDeleted()) {
      commitBuilder.remove(target.getId());
      all.add((Entity<?>) target);
    }
    for(final var target : batch.getToBeSaved()) {
      commitBuilder.append(target.getId(), config.getSerializer().toString(target));
      all.add((Entity<?>) target);
    }
    
    for(final var target : batch.getToBeCreated()) {
      commitBuilder.append(target.getId(), config.getSerializer().toString(target));
      all.add((Entity<?>) target);
    }
    return commitBuilder
        .message("batch" + 
            " created: '" + batch.getToBeCreated().size() + "',"+
            " updated: '" + batch.getToBeSaved().size() + "',"+
            " deleted: '" + batch.getToBeDeleted().size() + "'")
        .latestCommit()
        .author(config.getAuthorProvider().getAuthor())
        .build().onItem().transform(commit -> {
          if(commit.getStatus() == CommitResultStatus.OK) {
            return Collections.unmodifiableList(all);
          }
          throw new SaveException(all, commit);
        });
  }
  
}
