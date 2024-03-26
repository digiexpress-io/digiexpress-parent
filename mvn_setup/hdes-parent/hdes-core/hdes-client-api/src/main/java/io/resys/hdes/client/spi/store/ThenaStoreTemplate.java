package io.resys.hdes.client.spi.store;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.api.ImmutableStoreEntity;
import io.resys.hdes.client.api.ImmutableStoreExceptionMsg;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.exceptions.StoreException;
import io.resys.hdes.client.spi.store.ThenaConfig.EntityState;
import io.resys.hdes.client.spi.util.HdesAssert;
import io.resys.thena.api.actions.TenantActions.RepoStatus;
import io.resys.thena.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.models.Repo.CommitResultStatus;
import io.resys.thena.api.models.Repo.RepoType;
import io.smallrye.mutiny.Uni;

public abstract class ThenaStoreTemplate extends PersistenceCommands implements HdesStore {
  public ThenaStoreTemplate(ThenaConfig config) {
    super(config);
  }
  
  protected abstract HdesStore createWithNewConfig(ThenaConfig config);
  
  @Override
  public String getRepoName() {
    return config.getRepoName();
  }
  @Override
  public String getHeadName() {
    return config.getHeadName();
  }
  @Override
  public StoreRepoBuilder repo() {
    return new StoreRepoBuilder() {
      private String repoName;
      private String headName;
      @Override
      public StoreRepoBuilder repoName(String repoName) {
        this.repoName = repoName;
        return this;
      }
      @Override
      public StoreRepoBuilder headName(String headName) {
        this.headName = headName;
        return this;
      }
      @Override
      public Uni<HdesStore> create() {
        HdesAssert.notNull(repoName, () -> "repoName must be defined!");
        final var client = config.getClient();
        final var newRepo = client.tenants().commit().name(repoName, RepoType.git).build();
        return newRepo.onItem().transform((repoResult) -> {
          if(repoResult.getStatus() != RepoStatus.OK) {
            throw new StoreException("REPO_CREATE_FAIL", null, 
                ImmutableStoreExceptionMsg.builder()
                .id(repoResult.getStatus().toString())
                .value(repoName)
                .addAllArgs(repoResult.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
                .build()); 
          }
          
          return build();
        });
      }
      @Override
      public HdesStore build() {
        HdesAssert.notNull(repoName, () -> "repoName must be defined!");
        return createWithNewConfig(ImmutableThenaConfig.builder()
            .from(config)
            .repoName(repoName)
            .headName(headName == null ? config.getHeadName() : headName)
            .build());
      }
      @Override
      public Uni<Boolean> createIfNot() {
        final var client = config.getClient();
        
        return client.git(config.getRepoName()).project().get().onItem().transformToUni(repo -> {
          if(repo == null) {
            return client.tenants().commit().name(config.getRepoName(), RepoType.git).build().onItem().transform(newRepo -> true); 
          }
          return Uni.createFrom().item(false);
        });
      }
    };
  }
  
  @Override
  public QueryBuilder query() {
    return new DocumentQueryBuilder(config);
  }
  @Override
  public Uni<StoreEntity> create(CreateStoreEntity newType) {
    final var gid = newType.getId() == null ? gid(newType.getBodyType()) : newType.getId();
    
    final var entity = (StoreEntity) ImmutableStoreEntity.builder()
        .id(gid)
        .hash("")
        .body(newType.getBody())
        .bodyType(newType.getBodyType())
        .build();
    
    if(newType.getId() == null) {
      return super.save(entity);  
    }
    
    return get().onItem().transformToUni(currentState -> {
      cantHaveEntityWithId(newType.getId(), currentState);  
      return super.save(entity);  
    });
  }

  @Override
  public Uni<StoreEntity> update(UpdateStoreEntity updateType) {
    final Uni<EntityState> query = getEntityState(updateType.getId());
    return query.onItem().transformToUni(state -> {
      final StoreEntity entity = ImmutableStoreEntity.builder()
          .from(state.getEntity())
          .id(updateType.getId())
          .bodyType(state.getEntity().getBodyType())
          .body(updateType.getBody())
          .build();
      return super.save(entity);
    });
  }
  @Override
  public Uni<List<StoreEntity>> batch(ImportStoreEntity batchType) {
    return get().onItem().transformToUni(currentState -> {
      final var commitBuilder = config.getClient().git(config.getRepoName()).commit().commitBuilder()
          .branchName(config.getHeadName())
          .message("Save batch with new: " + batchType.getCreate().size() + " and updated: " + batchType.getUpdate().size() + " entries")
          .latestCommit()
          .author(config.getAuthorProvider().getAuthor());
      
      final List<String> ids = new ArrayList<>();
      for(final var toBeSaved : batchType.getCreate()) {
        final var id = toBeSaved.getId();
        if(id != null) {
          cantHaveEntityWithId(id, currentState);
        }
        
        final var gid = toBeSaved.getId() == null ? gid(toBeSaved.getBodyType()) : toBeSaved.getId();
        final var entity = (StoreEntity) ImmutableStoreEntity.builder()
            .id(gid)
            .hash("")
            .body(toBeSaved.getBody())
            .bodyType(toBeSaved.getBodyType())
            .build();
        commitBuilder.append(entity.getId(), config.getSerializer().toString(entity));
        ids.add(gid);
      }
      for(final var toBeSaved : batchType.getUpdate()) {
        final var id = toBeSaved.getId();
        HdesAssert.isTrue(
            currentState.getDecisions().containsKey(id) ||
            currentState.getFlows().containsKey(id) ||
            currentState.getServices().containsKey(id) ||
            currentState.getTags().containsKey(id), 
            () -> "Entity not found with id: '" + id + "'!");
        
        final var entity = (StoreEntity) ImmutableStoreEntity.builder()
            .id(id)
            .hash("")
            .body(toBeSaved.getBody())
            .bodyType(toBeSaved.getBodyType())
            .build();
        commitBuilder.append(entity.getId(), config.getSerializer().toString(entity));
        ids.add(entity.getId());
      }    
      
      return commitBuilder.build().onItem().transformToUni(commit -> {
            if(commit.getStatus() == CommitResultStatus.OK) {
              return config.getClient().git(config.getRepoName())
                  .pull().pullQuery()
                  .branchNameOrCommitOrTag(config.getHeadName())
                  .docId(ids)
                  .findAll().onItem()
                  .transform(states -> {
                    if(states.getStatus() != QueryEnvelopeStatus.OK) {
                      // TODO
                      throw new StoreException("LIST_FAIL", null, convertMessages2(states));
                    }
                    List<StoreEntity> entities = new ArrayList<>(); 
                    for(final var state : states.getObjects().getBlob()) {
                      StoreEntity start = (StoreEntity) config.getDeserializer().fromString(state);
                      entities.add(start);
                    }                  
                    return entities;
                  });
            }
            // TODO
            throw new StoreException("SAVE_FAIL", null, convertMessages(commit));
          });
      
    });
    
  }
  @Override
  public Uni<StoreEntity> delete(DeleteAstType deleteType) {
    final Uni<EntityState> query = getEntityState(deleteType.getId());
    return query.onItem().transformToUni(state -> delete(state.getEntity()));
  }

  private String gid(AstBodyType type) {
    return config.getGidProvider().getNextId(type);
  }

  @Override
  public HistoryQuery history() {
    // TODO Auto-generated method stub
    return null;
  }
  
  private void cantHaveEntityWithId(String id, StoreState currentState) {
    HdesAssert.isTrue(!currentState.getDecisions().containsKey(id), () -> "Entity of type 'decision' already exists with id: '" + id + "'!");
    HdesAssert.isTrue(!currentState.getFlows().containsKey(id), () -> "Entity of type 'flow' already exists with id: '" + id + "'!");
    HdesAssert.isTrue(!currentState.getServices().containsKey(id), () -> "Entity of type 'service' already exists with id: '" + id + "'!");
    HdesAssert.isTrue(!currentState.getTags().containsKey(id), () -> "Entity of type 'tag' already exists with id: '" + id + "'!");
  }
}
