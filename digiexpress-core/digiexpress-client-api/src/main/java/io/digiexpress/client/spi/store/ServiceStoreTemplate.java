package io.digiexpress.client.spi.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.dialob.client.spi.support.OidUtils;
import io.dialob.client.spi.support.Sha2;
import io.digiexpress.client.api.ImmutableStoreEntity;
import io.digiexpress.client.api.ImmutableStoreExceptionMsg;
import io.digiexpress.client.api.ImmutableStoreState;
import io.digiexpress.client.api.ServiceDocument.DocumentType;
import io.digiexpress.client.api.ServiceStore.CreateStoreEntity;
import io.digiexpress.client.api.ServiceStore.DeleteStoreEntity;
import io.digiexpress.client.api.ServiceStore.StoreCommand;
import io.digiexpress.client.api.ServiceStore.StoreEntity;
import io.digiexpress.client.api.ServiceStore.StoreExceptionMsg;
import io.digiexpress.client.api.ServiceStore.StoreState;
import io.digiexpress.client.api.ServiceStore.UpdateStoreEntity;
import io.digiexpress.client.spi.store.ServiceStoreConfig.EntityState;
import io.resys.thena.docdb.api.actions.CommitActions.CommitResult;
import io.resys.thena.docdb.api.actions.CommitActions.CommitStatus;
import io.resys.thena.docdb.api.actions.ObjectsActions.BlobObject;
import io.resys.thena.docdb.api.actions.ObjectsActions.BlobObjects;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsResult;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsStatus;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServiceStoreTemplate implements ServiceStoreConfig.Commands {
  private static final Comparator<StoreCommand> COMP = (a, b) -> {
    return Sha2.blob(a.toString()).compareTo(Sha2.blob(b.toString()));
  };
  protected final ServiceStoreConfig config;

  public String getRepoName() {
    return config.getRepoName();
  }

  public String getHeadName() {
    return config.getHeadName();
  }
  
  public Uni<StoreEntity> delete(DeleteStoreEntity deleteType) {
    final Uni<EntityState> query = getEntityState(deleteType.getId());
    return query.onItem().transformToUni(state -> delete(state.getEntity()));
  }


  public Uni<StoreEntity> create(CreateStoreEntity newType) {
    final var gid = newType.getId() == null ? gid(newType.getBodyType()) : newType.getId();
    final var entity = (StoreEntity) ImmutableStoreEntity.builder()
        .id(gid)
        .version(StringUtils.isEmpty(newType.getVersion()) ? OidUtils.gen() : newType.getVersion())
        .body(newType.getBody())
        .bodyType(newType.getBodyType())
        .build();
    return this.save(entity);
  }

  public Uni<StoreEntity> update(UpdateStoreEntity updateType) {
    final Uni<EntityState> query = getEntityState(updateType.getId());
    return query.onItem().transformToUni(state -> {
      
      if(!state.getEntity().getVersion().equals(updateType.getVersion())) {
        throw new StoreException("VERSION_LOCKING_ERROR", null, ImmutableStoreExceptionMsg.builder()
            .id("VERSION_LOCKING_ERROR_ON_UPDATE")
            .value("Version check mismatch on update, you provided: " + updateType.getVersion() + ", store expecting: " + state.getEntity().getVersion() + "!")
            .args(Arrays.asList(state.getEntity().getVersion(), updateType.getVersion()))
            .build()); 
      }
      
      final StoreEntity entity = ImmutableStoreEntity.builder()
          .from(state.getEntity())
          .version(OidUtils.gen())
          .id(updateType.getId())
          .bodyType(state.getEntity().getBodyType())
          .body(updateType.getBody())
          .build();
      return this.save(entity);
    });
  }

  public Uni<List<StoreEntity>> batch(List<StoreCommand> batchType) {

    final var create = batchType.stream()
        .filter(e -> e instanceof CreateStoreEntity).map(e -> (CreateStoreEntity) e)
        .sorted(COMP)
        .collect(Collectors.toList());
    final var update = batchType.stream()
        .filter(e -> e instanceof UpdateStoreEntity).map(e -> (UpdateStoreEntity) e)
        .sorted(COMP)
        .collect(Collectors.toList());
    final var del = batchType.stream()
        .filter(e -> e instanceof DeleteStoreEntity).map(e -> (DeleteStoreEntity) e)
        .sorted(COMP)
        .collect(Collectors.toList());
    
    final var commitBuilder = config.getClient().commit().head()
        .head(config.getRepoName(), config.getHeadName())
        .message(
            "Save batch with new: " + create.size() + 
            " , updated: " + update.size() +
            " and deleted: " + del.size() +
            " entries")
        .parentIsLatest()
        .author(config.getAuthorProvider().getAuthor());
    
    
    return get().onItem().transformToUni(currentState -> {
      
      final List<String> ids = new ArrayList<>();
      for(final var toBeSaved : create) {
        final var gid = toBeSaved.getId() == null ? gid(toBeSaved.getBodyType()) : toBeSaved.getId();
        final var entity = (StoreEntity) ImmutableStoreEntity.builder()
            .id(gid)
            .version(StringUtils.isEmpty(toBeSaved.getVersion()) ? OidUtils.gen() : toBeSaved.getVersion())
            .body(toBeSaved.getBody())
            .bodyType(toBeSaved.getBodyType())
            .build();
        commitBuilder.append(entity.getId(), config.getSerializer().toString(entity));
        ids.add(gid);
      }
      for(final var toBeSaved : update) {
        final var entity = (StoreEntity) ImmutableStoreEntity.builder()
            .id(toBeSaved.getId())
            .version(OidUtils.gen())
            .body(toBeSaved.getBody())
            .bodyType(toBeSaved.getBodyType())
            .build();
        
        final var currentValue = this.getEntityFromState(currentState, toBeSaved.getId());
        if(!currentValue.getVersion().equals(toBeSaved.getVersion())) {
          throw new StoreException("VERSION_LOCKING_ERROR", null, ImmutableStoreExceptionMsg.builder()
              .id("VERSION_LOCKING_ERROR_ON_UPDATE_IN_BATCH")
              .value("Version check mismatch on update in batch, you provided: " + toBeSaved.getVersion() + ", store expecting: " + currentValue.getVersion() + "!")
              .args(Arrays.asList(currentValue.getVersion(), toBeSaved.getVersion()))
              .build()); 
        }
        commitBuilder.append(entity.getId(), config.getSerializer().toString(entity));
        ids.add(entity.getId());
      }    
      
      for(final var toBeDeleted : del) {
        final var currentValue = this.getEntityFromState(currentState, toBeDeleted.getId());
        if(!currentValue.getVersion().equals(toBeDeleted.getVersion())) {
          throw new StoreException("VERSION_LOCKING_ERROR", null, ImmutableStoreExceptionMsg.builder()
              .id("VERSION_LOCKING_ERROR_ON_DELETE_IN_BATCH")
              .value("Version check mismatch on delete in batch, you provided: " + toBeDeleted.getVersion() + ", store expecting: " + currentValue.getVersion() + "!")
              .args(Arrays.asList(currentValue.getVersion(), toBeDeleted.getVersion()))
              .build()); 
        }
        commitBuilder.remove(toBeDeleted.getId());
      }
      
      return commitBuilder.build().onItem().transformToUni(commit -> {
        if(commit.getStatus() == CommitStatus.OK) {
          return config.getClient()
              .objects().blobState()
              .repo(config.getRepoName())
              .anyId(config.getHeadName())
              .blobNames(ids)
              .list().onItem()
              .transform(states -> {
                if(states.getStatus() != ObjectsStatus.OK) {
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
  public Uni<StoreEntity> save(StoreEntity toBeSaved) {
    return config.getClient().commit().head()
      .head(config.getRepoName(), config.getHeadName())
      .message("Save type: '" + toBeSaved.getBodyType() + "', with id: '" + toBeSaved.getId() + "'")
      .parentIsLatest()
      .author(config.getAuthorProvider().getAuthor())
      .append(toBeSaved.getId(), config.getSerializer().toString(toBeSaved))
      .build().onItem().transform(commit -> {
        if(commit.getStatus() == CommitStatus.OK) {
          return toBeSaved;
        }
        // TODO
        throw new StoreException("SAVE_FAIL", toBeSaved, convertMessages(commit));
      });
  }


  @Override
  public Uni<Collection<StoreEntity>> save(Collection<StoreEntity> entities) {
    final var commitBuilder = config.getClient().commit().head().head(config.getRepoName(), config.getHeadName());
    final StoreEntity first = entities.iterator().next();
    
    for(final var target : entities) {
      commitBuilder.append(target.getId(), config.getSerializer().toString(target));
    }
    
    return commitBuilder
        .message("Save type: '" + first.getBodyType() + "', with id: '" + first.getId() + "'")
        .parentIsLatest()
        .author(config.getAuthorProvider().getAuthor())
        .build().onItem().transform(commit -> {
          if(commit.getStatus() == CommitStatus.OK) {
            return entities;
          }
          // TODO
          throw new StoreException("SAVE_FAIL", null, convertMessages(commit));
        });
  }
  
  public StoreEntity getEntityFromState(StoreState state, String id) {
    var entity = state.getProcesses().get(id);
    if(entity == null) {
      entity = state.getRevs().get(id);
    }
    if(entity == null) {
      entity = state.getReleases().get(id);
    }
    if(entity == null) {
      entity = state.getConfigs().get(id);
    }    
    return null;
  }

  @Override
  public Uni<StoreState> get() {
    return config.getClient()
        .objects().refState()
        .repo(config.getRepoName())
        .ref(config.getHeadName())
        .blobs()
        .build()
        .onItem().transform(state -> {
          if(state.getStatus() != ObjectsStatus.OK) {
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
             case SERVICE_DEF: builder.putRevs(entity.getId(), entity); break;
             case SERVICE_REV: builder.putProcesses(entity.getId(), entity);  break;
             case SERVICE_RELEASE: builder.putReleases(entity.getId(), entity);  break;
             case SERVICE_CONFIG: builder.putConfigs(entity.getId(), entity);  break;
             default: throw new RuntimeException("Unknown type: " + entity.getBodyType() + "!");
            }
          }
          
          return builder.build();
        });
  }

  @Override
  public Uni<EntityState> getEntityState(String id) {
    return config.getClient()
        .objects().blobState()
        .repo(config.getRepoName())
        .anyId(config.getHeadName())
        .blobName(id)
        .get().onItem()
        .transform(state -> {
          if(state.getStatus() != ObjectsStatus.OK) {
            // TODO
            throw new StoreException("GET_FAIL", null, convertMessages1(state));
          }
          StoreEntity start = (StoreEntity) config.getDeserializer().fromString(state.getObjects().getBlob());
          return ImmutableEntityState.builder().src(state).entity(start).build();
        });
  }
  @Override
  public Uni<StoreEntity> delete(StoreEntity toBeDeleted) {
    return config.getClient().commit().head()
        .head(config.getRepoName(), config.getHeadName())
        .message("Delete type: '" + toBeDeleted.getBodyType() + "', with id: '" + toBeDeleted.getId() + "'")
        .parentIsLatest()
        .author(config.getAuthorProvider().getAuthor())
        .remove(toBeDeleted.getId())
        .build().onItem().transform(commit -> {
          if(commit.getStatus() == CommitStatus.OK) {
            return toBeDeleted;
          }
          // TODO
          throw new StoreException("DELETE_FAIL", toBeDeleted, convertMessages(commit));
        });
  }
  
  
  protected StoreExceptionMsg convertMessages(CommitResult commit) {
    return ImmutableStoreExceptionMsg.builder()
        .id(commit.getGid())
        .value("") //TODO
        .addAllArgs(commit.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
        .build();
  }

  protected StoreExceptionMsg convertMessages1(ObjectsResult<BlobObject> state) {
    return ImmutableStoreExceptionMsg.builder()
        .id("STATE_FAIL")
        .value("")
        .addAllArgs(state.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
        .build();
  }
  protected StoreExceptionMsg convertMessages2(ObjectsResult<BlobObjects> state) {
    return ImmutableStoreExceptionMsg.builder()
        .addAllArgs(state.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
        .build();
  }

  protected String gid(DocumentType type) {
    return config.getGidProvider().getNextId(type);
  }
  public ServiceStoreConfig getConfig() {
    return config;
  }
}
