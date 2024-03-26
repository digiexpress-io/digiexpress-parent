package io.dialob.client.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.dialob.client.api.DialobDocument.DocumentType;
import io.dialob.client.api.DialobStore;
import io.dialob.client.api.ImmutableStoreEntity;
import io.dialob.client.api.ImmutableStoreExceptionMsg;
import io.dialob.client.spi.exceptions.StoreException;
import io.dialob.client.spi.store.DialobStoreConfig;
import io.dialob.client.spi.store.DialobStoreConfig.EntityState;
import io.dialob.client.spi.store.DocumentQueryBuilder;
import io.dialob.client.spi.store.ImmutableDialobStoreConfig;
import io.dialob.client.spi.store.PersistenceCommands;
import io.dialob.client.spi.support.DialobAssert;
import io.dialob.client.spi.support.OidUtils;
import io.dialob.client.spi.support.Sha2;
import io.resys.thena.docdb.api.actions.CommitActions.CommitBuilder;
import io.resys.thena.docdb.api.actions.TenantModel.RepoStatus;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.Repo.CommitResultStatus;
import io.resys.thena.docdb.api.models.Repo.RepoType;
import io.smallrye.mutiny.Uni;

public class DialobStoreTemplate extends PersistenceCommands implements DialobStore {
  private static final Comparator<StoreCommand> COMP = (a, b) -> {
    return Sha2.blob(a.toString()).compareTo(Sha2.blob(b.toString()));
  };
  @Override
  public DialobStoreTemplate withRepo(String repoName, String headName) {
    return new DialobStoreTemplate(ImmutableDialobStoreConfig.builder().from(config).repoName(repoName).headName(headName).build());
  }
  public DialobStoreTemplate(DialobStoreConfig config) {
    super(config);
  }
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
      public Uni<DialobStore> create() {
        DialobAssert.notNull(repoName, () -> "repoName must be defined!");
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
      public DialobStore build() {
        DialobAssert.notNull(repoName, () -> "repoName must be defined!");
        return new DialobStoreTemplate(ImmutableDialobStoreConfig.builder()
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
          return Uni.createFrom().item(true);
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
        .version(StringUtils.isEmpty(newType.getVersion()) ? OidUtils.gen() : newType.getVersion())
        .body(newType.getBody())
        .bodyType(newType.getBodyType())
        .build();
    return super.save(entity);
  }

  @Override
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
      return super.save(entity);
    });
  }
  @Override
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
    
    final CommitBuilder commitBuilder = config.getClient().git(config.getRepoName()).commit()
        .commitBuilder()
        .branchName(config.getHeadName())
        .message(
            "Save batch with new: " + create.size() + 
            " , updated: " + update.size() +
            " and deleted: " + del.size() +
            " entries")
        .latestCommit()
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
        
        final var currentValue = super.getEntityFromState(currentState, toBeSaved.getId());
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
        final var currentValue = super.getEntityFromState(currentState, toBeDeleted.getId());
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
  public Uni<StoreEntity> delete(DeleteStoreEntity deleteType) {
    final Uni<EntityState> query = getEntityState(deleteType.getId());
    return query.onItem().transformToUni(state -> delete(state.getEntity()));
  }

  private String gid(DocumentType type) {
    return config.getGidProvider().getNextId(type);
  }
}
