package io.digiexpress.client.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.smallrye.mutiny.Uni;

public interface ServiceStore {
  Uni<StoreEntity> create(CreateStoreEntity newType);
  Uni<StoreEntity> update(UpdateStoreEntity updateType);
  Uni<StoreEntity> delete(DeleteStoreEntity deleteType);
  Uni<List<StoreEntity>> batch(List<StoreCommand> batchType);
    
  QueryBuilder query();
  StoreRepoBuilder repo();
  
  String getRepoName();
  String getHeadName();
  
  
  interface StoreRepoBuilder {
    StoreRepoBuilder repoName(String repoName);
    StoreRepoBuilder headName(String headName);
    Uni<ServiceStore> create();    
    ServiceStore build();
    Uni<Boolean> createIfNot();
  }
  
  interface QueryBuilder {
    Uni<StoreState> get();
    Uni<StoreEntity> get(String id);
  }

  @Value.Immutable @JsonSerialize(as = ImmutableStoreState.class) @JsonDeserialize(as = ImmutableStoreState.class)
  interface StoreState {
    Map<String, StoreEntity> getRevs();
    Map<String, StoreEntity> getProcesses();
    Map<String, StoreEntity> getReleases();
    Map<String, StoreEntity> getConfigs();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableStoreEntity.class) @JsonDeserialize(as = ImmutableStoreEntity.class)
  interface StoreEntity extends Serializable {
    String getId();
    String getVersion();
    ServiceDocument.DocumentType getBodyType();
    String getBody();
  }
  
  
  interface StoreCommand extends Serializable {
    ServiceDocument.DocumentType getBodyType();
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
  interface ServiceCredsSupplier extends Supplier<ServiceCreds> {}
  
  @Value.Immutable
  interface ServiceCreds {
    String getUser();
    String getEmail();
  } 
}