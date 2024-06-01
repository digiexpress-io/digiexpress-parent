package io.resys.hdes.client.spi.store;


import static org.assertj.core.api.Assertions.entry;

import java.util.ArrayList;
import java.util.Collection;
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
import io.resys.hdes.client.api.HdesStore.StoreEntity;
import io.resys.hdes.client.api.ImmutableStoreEntity;
import io.resys.hdes.client.api.ImmutableStoreExceptionMsg;
import io.resys.hdes.client.api.ImmutableStoreState;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.exceptions.StoreException;
import io.resys.hdes.client.spi.util.HdesAssert;
import io.resys.thena.api.actions.GitCommitActions.CommitResultEnvelope;
import io.resys.thena.api.actions.GitPullActions;
import io.resys.thena.api.actions.TenantActions.CommitStatus;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.envelope.DocContainer.DocObject;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.DocStoreImpl;
import io.resys.thena.spi.ThenaDocConfig;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.Uni;


public class ThenaStoreTemplate extends DocStoreImpl<ThenaStoreTemplate> implements HdesStore {
  public static String DOC_TYPE_WRENCH_ASSET = "WRENCH_ASSET";
  

  public ThenaStoreTemplate(ThenaDocConfig config, DocStoreFactory<ThenaStoreTemplate> factory) {
    super(config, factory);
  }
  @Override
  public Uni<StoreEntity> create(CreateStoreEntity newType) {
    final var gid = newType.getId() == null ? gid(newType.getBodyType()) : newType.getId();
    
    final var entity = ImmutableStoreEntity.builder()
        .id(gid)
        .hash("")
        .body(newType.getBody())
        .bodyType(newType.getBodyType())
        .build();
    
    if(newType.getId() == null) {
      return save(entity);  
    }
    
    return get().onItem().transformToUni(currentState -> {
      cantHaveEntityWithId(newType.getId(), currentState);  
      return save(entity);  
    });
  }
  @Override
  public Uni<StoreEntity> update(UpdateStoreEntity updateType) {
    final Uni<StoreEntity> query = getEntityState(updateType.getId());
    return query.onItem().transformToUni(state -> {
      final StoreEntity entity = ImmutableStoreEntity.builder()
          .from(state)
          .id(updateType.getId())
          .bodyType(state.getBodyType())
          .body(updateType.getBody())
          .build();
      return save(entity);
    });
  }
  @Override
  public QueryBuilder assetQuery() {
    return new QueryBuilder() {
      @Override public Uni<StoreEntity> get(String id) { return getEntityState(id); }
      @Override public Uni<StoreState> get() { return get(); }
    };
  }
  
  @Override
  public Uni<List<StoreEntity>> batch(ImportStoreEntity batchType) {
    return get().onItem().transformToUni(currentState -> {
      final var commitBuilder = config.getClient().git(config.getRepoName()).commit().commitBuilder()
          .branchName(config.getHeadName())
          .message("Save batch with new: " + batchType.getCreate().size() + " and updated: " + batchType.getUpdate().size() + " entries")
          .latestCommit()
          .author(config.getAuthor().get());
      
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
    final Uni<StoreEntity> query = getEntityState(deleteType.getId());
    return query.onItem().transformToUni(state -> delete(state));
  }
  @Override
  public HistoryQuery history() {
    // TODO Auto-generated method stub
    return null;
  }

  private String gid(AstBodyType type) {
    return OidUtils.gen();
  }

  private Uni<StoreEntity> delete(StoreEntity toBeDeleted) {
    return config.getClient().git(config.getRepoName()).commit().commitBuilder()
        .branchName(config.getHeadName())
        .message("Delete type: '" + toBeDeleted.getBodyType() + "', with id: '" + toBeDeleted.getId() + "'")
        .latestCommit()
        .author(config.getAuthorProvider().getAuthor())
        .remove(toBeDeleted.getId())
        .build().onItem().transform(commit -> {
          if(commit.getStatus() == CommitResultStatus.OK) {
            return toBeDeleted;
          }
          // TODO
          throw new StoreException("DELETE_FAIL", toBeDeleted, convertMessages(commit));
        });
  }

  private Uni<StoreEntity> save(StoreEntity toBeSaved) {
    return config.getClient().git(config.getRepoName()).commit().commitBuilder()
      .branchName(config.getHeadName())
      .message("Save type: '" + toBeSaved.getBodyType() + "', with id: '" + toBeSaved.getId() + "'")
      .latestCommit()
      .author(config.getAuthorProvider().getAuthor())
      .append(toBeSaved.getId(), config.getSerializer().toString(toBeSaved))
      .build().onItem().transform(commit -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return toBeSaved;
        }
        // TODO
        throw new StoreException("SAVE_FAIL", toBeSaved, convertMessages(commit));
      });
  }
  
  private Uni<Collection<StoreEntity>> save(Collection<StoreEntity> entities) {
    final var commitBuilder = config.getClient().doc(config.getRepoId()).commit();
    final StoreEntity first = entities.iterator().next();
    
    for(final var target : entities) {
      commitBuilder.append(target.getId(), config.getSerializer().toString(target));
    }
    
    return commitBuilder
        .
        
        .latestCommit()
        .author(config.getAuthor().get())
        .build().onItem().transform(commit -> {
          if(commit.getStatus() == CommitResultStatus.OK) {
            return entities;
          }
          // TODO
          throw new StoreException("SAVE_FAIL", null, convertMessages(commit));
        });
  }

  private Uni<StoreState> get() {
    return config.getClient().doc(config.getRepoId())
        .find().docQuery()
        .docType(DOC_TYPE_WRENCH_ASSET)
        .branchName(config.getHeadId())
        .findAll()
        .onItem().transform(state -> {
          if(state.getStatus() != QueryEnvelopeStatus.OK) {
            throw new StoreException("GET_REPO_STATE_FAIL", null, ImmutableStoreExceptionMsg.builder()
                .id(state.getRepo().getName())
                .value(state.getRepo().getId())
                .addAllArgs(state.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
                .build()); 
          }
          

          final var builder = ImmutableStoreState.builder();
          if(state.getObjects() == null) {
            return builder.build(); 
          }
          
          state.getObjects().accept((doc, docBranch, commit, commands, tree) -> {
            final var entity = docBranch.getValue().mapTo(ImmutableStoreEntity.class);
            switch(entity.getBodyType()) {
             case DT: builder.putDecisions(entity.getId(), entity); break;
             case FLOW_TASK: builder.putServices(entity.getId(), entity); break;
             case FLOW: builder.putFlows(entity.getId(), entity); break;
             case TAG: builder.putTags(entity.getId(), entity);  break;
             default: throw new RuntimeException("Unknown type: " + entity.getBodyType() + "!");
            }
            return null;
          });
          return builder.build();
        });
  }

  private Uni<StoreEntity> getEntityState(String id) {
    return config.getClient().doc(config.getRepoId())
        .find().docQuery()
        .branchName(config.getHeadId())
        .get(id).onItem()
        .transform(state -> {
          if(state.getStatus() != QueryEnvelopeStatus.OK) {
            // TODO
            throw new StoreException("GET_FAIL", null, convertMessages1(state));
          }
          return  state.getObjects()
              .accept((doc, docBranch, commit, commands, tree) -> docBranch.getValue().mapTo(ImmutableStoreEntity.class))
              .iterator().next();
        });
  }
  
  
  private StoreExceptionMsg convertMessages(CommitResultEnvelope commit) {
    return ImmutableStoreExceptionMsg.builder()
        .id(commit.getGid())
        .value("") //TODO
        .addAllArgs(commit.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
        .build();
  }

  private StoreExceptionMsg convertMessages1(QueryEnvelope<DocObject> state) {
    return ImmutableStoreExceptionMsg.builder()
        .id("STORE_STATE_ERROR").value("")
        .addAllArgs(state.getMessages().stream().map(message-> message.getText()).collect(Collectors.toList()))
        .build();
  }
  private StoreExceptionMsg convertMessages2(QueryEnvelope<GitPullActions.PullObjects> state) {
    return ImmutableStoreExceptionMsg.builder()
        .id("STORE_STATE_ERROR").value("")
        .addAllArgs(state.getMessages().stream().map(message-> message.getText()).collect(Collectors.toList()))
        .build();
  }
  private void cantHaveEntityWithId(String id, StoreState currentState) {
    HdesAssert.isTrue(!currentState.getDecisions().containsKey(id), () -> "Entity of type 'decision' already exists with id: '" + id + "'!");
    HdesAssert.isTrue(!currentState.getFlows().containsKey(id), () -> "Entity of type 'flow' already exists with id: '" + id + "'!");
    HdesAssert.isTrue(!currentState.getServices().containsKey(id), () -> "Entity of type 'service' already exists with id: '" + id + "'!");
    HdesAssert.isTrue(!currentState.getTags().containsKey(id), () -> "Entity of type 'tag' already exists with id: '" + id + "'!");
  }
}
