package io.resys.hdes.client.spi.store;

/*-
 * #%L
 * hdes-client
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

import java.util.Collection;
import java.util.stream.Collectors;

import io.resys.hdes.client.api.HdesStore.StoreEntity;
import io.resys.hdes.client.api.HdesStore.StoreExceptionMsg;
import io.resys.hdes.client.api.HdesStore.StoreState;
import io.resys.hdes.client.api.ImmutableStoreExceptionMsg;
import io.resys.hdes.client.api.ImmutableStoreState;
import io.resys.hdes.client.api.exceptions.StoreException;
import io.resys.hdes.client.spi.store.ThenaConfig.EntityState;
import io.resys.thena.api.actions.GitCommitActions.CommitResultEnvelope;
import io.resys.thena.api.actions.GitPullActions;
import io.resys.thena.api.actions.GitPullActions.PullObject;
import io.resys.thena.api.actions.GitPullActions.PullObjects;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.smallrye.mutiny.Uni;



public class PersistenceCommands implements ThenaConfig.Commands {
  protected final ThenaConfig config;

  public PersistenceCommands(ThenaConfig config) {
    super();
    this.config = config;
  }
  
  @Override
  public Uni<StoreEntity> delete(StoreEntity toBeDeleted) {
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

  @Override
  public Uni<StoreEntity> save(StoreEntity toBeSaved) {
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


  @Override
  public Uni<Collection<StoreEntity>> save(Collection<StoreEntity> entities) {
    final var commitBuilder = config.getClient().git(config.getRepoName()).commit().commitBuilder().branchName(config.getHeadName());
    final StoreEntity first = entities.iterator().next();
    
    for(final var target : entities) {
      commitBuilder.append(target.getId(), config.getSerializer().toString(target));
    }
    
    return commitBuilder
        .message("Save type: '" + first.getBodyType() + "', with id: '" + first.getId() + "'")
        .latestCommit()
        .author(config.getAuthorProvider().getAuthor())
        .build().onItem().transform(commit -> {
          if(commit.getStatus() == CommitResultStatus.OK) {
            return entities;
          }
          // TODO
          throw new StoreException("SAVE_FAIL", null, convertMessages(commit));
        });
  }

  @Override
  public Uni<StoreState> get() {
    return config.getClient().git(config.getRepoName())
        .branch().branchQuery()
        .branchName(config.getHeadName())
        .docsIncluded()
        .get()
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
          
          final var tree = state.getObjects().getTree();
          for(final var entry : tree.getValues().entrySet()) {
            final var blobId = entry.getValue().getBlob();
            final var blob = state.getObjects().getBlobs().get(blobId);
            final var entity = config.getDeserializer().fromString(blob);
            switch(entity.getBodyType()) {
             case DT: builder.putDecisions(entity.getId(), entity); break;
             case FLOW_TASK: builder.putServices(entity.getId(), entity); break;
             case FLOW: builder.putFlows(entity.getId(), entity); break;
             case TAG: builder.putTags(entity.getId(), entity);  break;
             default: throw new RuntimeException("Unknown type: " + entity.getBodyType() + "!");
            }
          }
          
          return builder.build();
        });
  }

  @Override
  public Uni<EntityState> getEntityState(String id) {
    return config.getClient().git(config.getRepoName())
        .pull().pullQuery()
        .branchNameOrCommitOrTag(config.getHeadName())
        .docId(id)
        .get().onItem()
        .transform(state -> {
          if(state.getStatus() != QueryEnvelopeStatus.OK) {
            // TODO
            throw new StoreException("GET_FAIL", null, convertMessages1(state));
          }
          StoreEntity start = (StoreEntity) config.getDeserializer().fromString(state.getObjects().getBlob());
          return ImmutableEntityState.builder().src(state).entity(start).build();
        });
  }
  
  
  protected StoreExceptionMsg convertMessages(CommitResultEnvelope commit) {
    return ImmutableStoreExceptionMsg.builder()
        .id(commit.getGid())
        .value("") //TODO
        .addAllArgs(commit.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
        .build();
  }

  protected StoreExceptionMsg convertMessages1(QueryEnvelope<GitPullActions.PullObject> state) {
    return ImmutableStoreExceptionMsg.builder()
        .id("STORE_STATE_ERROR").value("")
        .addAllArgs(state.getMessages().stream().map(message-> message.getText()).collect(Collectors.toList()))
        .build();
  }
  protected StoreExceptionMsg convertMessages2(QueryEnvelope<GitPullActions.PullObjects> state) {
    return ImmutableStoreExceptionMsg.builder()
        .id("STORE_STATE_ERROR").value("")
        .addAllArgs(state.getMessages().stream().map(message-> message.getText()).collect(Collectors.toList()))
        .build();
  }

  public ThenaConfig getConfig() {
    return config;
  }
}
