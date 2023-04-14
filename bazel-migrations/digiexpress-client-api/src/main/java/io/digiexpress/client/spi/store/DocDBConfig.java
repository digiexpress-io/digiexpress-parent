package io.digiexpress.client.spi.store;

import java.util.Collection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.digiexpress.client.api.ImmutableProcessState;
import org.immutables.value.Value;

import io.digiexpress.client.api.ClientStore.StoreEntity;
import io.digiexpress.client.api.ClientStore.StoreGid;
import io.digiexpress.client.api.ClientStore.StoreState;
import io.digiexpress.client.api.Parser;
import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.api.actions.ObjectsActions.BlobObject;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsResult;
import io.resys.thena.docdb.api.models.Objects.Blob;
import io.smallrye.mutiny.Uni;


@Value.Immutable @JsonSerialize(as = ImmutableDocDBConfig.class) @JsonDeserialize(as = ImmutableDocDBConfig.class)
public interface DocDBConfig {
  DocDB getClient();
  DocDBAuthorProvider getAuthorProvider();
  DocDBSerializer getSerializer();
  DocDBDeserializer getDeserializer();
  
  String getRepoName();
  String getHeadName();
  StoreGid getGid();
  Parser getParser();


  @FunctionalInterface
  interface DocDBSerializer {
    String toString(StoreEntity entity);
  }
  @FunctionalInterface  
  interface DocDBDeserializer {
    StoreEntity fromString(Blob value);
  }
  @FunctionalInterface
  interface DocDBAuthorProvider {
    String getAuthor();
  }
  interface DocDBCommands {
    Uni<StoreEntity> delete(StoreEntity toBeDeleted);
    Uni<StoreState> get();
    Uni<StoreEntityState> getState(String id);
    Uni<StoreEntity> save(StoreEntity toBeSaved);
    Uni<Collection<StoreEntity>> save(Collection<StoreEntity> toBeSaved);
  }
  
  @Value.Immutable
  interface StoreEntityState {
    ObjectsResult<BlobObject> getBlob();
    StoreEntity getEntity();
  }
}