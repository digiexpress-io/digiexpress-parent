package io.digiexpress.client.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.client.api.ClientEntity.ClientEntityType;
import io.smallrye.mutiny.Uni;

public interface ClientStore {
  Uni<StoreEntity> create(CreateStoreEntity newType);
  Uni<StoreEntity> update(UpdateStoreEntity updateType);
  Uni<StoreEntity> delete(DeleteStoreEntity deleteType);
  Uni<List<StoreEntity>> batch(List<ClientStoreCommand> batchType);
    
  StoreQuery query();
  StoreRepo repo();
  
  String getRepoName();
  String getHeadName();

  StoreGid getGid();

  @FunctionalInterface
  interface StoreGid {
    String getNextId(ClientEntityType entity);
  }
  
  @FunctionalInterface
  interface StoreCredsSupplier extends Supplier<StoreCreds> {
  }
  
  interface ClientStoreCommand extends Serializable {
    ClientEntityType getBodyType();
  }
  
  interface StoreRepo {
    StoreRepo repoName(String repoName);
    StoreRepo headName(String headName);
    Uni<ClientStore> create();    
    ClientStore build();
    Uni<Boolean> createIfNot();
  }
  
  interface StoreQuery {
    Uni<StoreState> get();
    Uni<StoreEntity> get(String id);
  }

  @Value.Immutable @JsonSerialize(as = ImmutableStoreState.class) @JsonDeserialize(as = ImmutableStoreState.class)
  interface StoreState {
    String getCommit();
    @Nullable String getCommitMsg();
    Map<String, StoreEntity> getProjects();
    Map<String, StoreEntity> getDefinitions();
    Map<String, StoreEntity> getReleases();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableStoreEntity.class) @JsonDeserialize(as = ImmutableStoreEntity.class)
  interface StoreEntity extends Serializable {
    String getId();
    String getVersion();
    ClientEntityType getBodyType();
    String getBody();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableEmptyStoreCommand.class) @JsonDeserialize(as = ImmutableEmptyStoreCommand.class)
  interface EmptyStoreCommand extends ClientStoreCommand {
    String getId();
    String getDescription();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableUpdateStoreEntity.class) @JsonDeserialize(as = ImmutableUpdateStoreEntity.class)
  interface UpdateStoreEntity extends ClientStoreCommand {
    String getId();
    String getVersion();
    String getBody();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableCreateStoreEntity.class) @JsonDeserialize(as = ImmutableCreateStoreEntity.class)
  interface CreateStoreEntity extends ClientStoreCommand {
    @Nullable String getId(); // in case not provided, auto generated
    @Nullable String getVersion(); // in case not provided, auto generated
    String getBody();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableDeleteStoreEntity.class) @JsonDeserialize(as = ImmutableDeleteStoreEntity.class)
  interface DeleteStoreEntity extends ClientStoreCommand {
    String getId();
    String getVersion();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableStoreExceptionMsg.class)
  interface StoreExceptionMsg {
    String getId();
    String getValue();
    List<String> getArgs();
  }
  
  @Value.Immutable
  interface StoreCreds {
    String getUser();
    String getEmail();
  } 
}