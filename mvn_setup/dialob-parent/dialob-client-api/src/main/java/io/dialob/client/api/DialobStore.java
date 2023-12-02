package io.dialob.client.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.client.api.DialobDocument.DocumentType;
import io.smallrye.mutiny.Uni;


public interface DialobStore {
  Uni<StoreEntity> create(CreateStoreEntity newType);
  Uni<StoreEntity> update(UpdateStoreEntity updateType);
  Uni<StoreEntity> delete(DeleteStoreEntity deleteType);
  Uni<List<StoreEntity>> batch(List<StoreCommand> batchType);
    
  QueryBuilder query();
  String getRepoName();
  String getHeadName();
  StoreRepoBuilder repo();
  
  DialobStore withRepo(String repoName, String headName);
  
  
  interface StoreRepoBuilder {
    StoreRepoBuilder repoName(String repoName);
    StoreRepoBuilder headName(String headName);
    Uni<DialobStore> create();    
    DialobStore build();
    Uni<Boolean> createIfNot();
  }
  
  interface QueryBuilder {
    Uni<StoreState> get();
    Uni<StoreEntity> get(String id);
  }

  @Value.Immutable @JsonSerialize(as = ImmutableStoreState.class) @JsonDeserialize(as = ImmutableStoreState.class)
  interface StoreState {
    Map<String, StoreEntity> getRevs();
    Map<String, StoreEntity> getForms();
    Map<String, StoreEntity> getTags();    
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableStoreEntity.class) @JsonDeserialize(as = ImmutableStoreEntity.class)
  interface StoreEntity extends Serializable {
    String getId();
    String getVersion();
    DocumentType getBodyType();
    String getBody();
  }
  
  
  interface StoreCommand extends Serializable {
    DocumentType getBodyType();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableUpdateStoreEntity.class) @JsonDeserialize(as = ImmutableUpdateStoreEntity.class)
  interface EmptyCommand extends StoreCommand {
    String getId();
    String getDescription();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableUpdateStoreEntity.class) @JsonDeserialize(as = ImmutableUpdateStoreEntity.class)
  interface UpdateStoreEntity extends StoreCommand {
    String getId();
    String getVersion();
    String getBody();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableCreateStoreEntity.class) @JsonDeserialize(as = ImmutableCreateStoreEntity.class)
  interface CreateStoreEntity extends StoreCommand {
    @Nullable String getId(); // in case not provided, auto generated
    @Nullable String getVersion(); // in case not provided, auto generated
    String getBody();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableDeleteStoreEntity.class) @JsonDeserialize(as = ImmutableDeleteStoreEntity.class)
  interface DeleteStoreEntity extends StoreCommand {
    String getId();
    String getVersion();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableStoreExceptionMsg.class)
  interface StoreExceptionMsg {
    String getId();
    String getValue();
    List<String> getArgs();
  }
  
  @FunctionalInterface
  interface DialobCredsSupplier extends Supplier<DialobCreds> {}
  
  @Value.Immutable
  interface DialobCreds {
    String getUser();
    String getEmail();
  } 
}
