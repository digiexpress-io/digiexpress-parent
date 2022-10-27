package io.digiexpress.client.spi.store;

import java.util.Collection;

import org.immutables.value.Value;

import io.digiexpress.client.api.ServiceDocument.DocumentType;
import io.digiexpress.client.api.ServiceStore.StoreEntity;
import io.digiexpress.client.api.ServiceStore.StoreState;
import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.api.actions.ObjectsActions.BlobObject;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsResult;
import io.resys.thena.docdb.api.models.Objects.Blob;
import io.smallrye.mutiny.Uni;


@Value.Immutable
public interface ServiceStoreConfig {
  DocDB getClient();
  String getRepoName();
  String getHeadName();
  AuthorProvider getAuthorProvider();
  
  @FunctionalInterface
  interface GidProvider {
    String getNextId(DocumentType entity);
  }
  
  GidProvider getGidProvider();
  
  @FunctionalInterface
  interface Serializer {
    String toString(StoreEntity entity);
  }
  
  interface Deserializer {
    StoreEntity fromString(Blob value);
  }
  Serializer getSerializer();
  Deserializer getDeserializer();
  
  @FunctionalInterface
  interface AuthorProvider {
    String getAuthor();
  }
  
  @Value.Immutable
  interface EntityState {
    ObjectsResult<BlobObject> getSrc();
    StoreEntity getEntity();
  }
  
  interface Commands {
    Uni<StoreEntity> delete(StoreEntity toBeDeleted);
    Uni<StoreState> get();
    Uni<EntityState> getEntityState(String id);
    Uni<StoreEntity> save(StoreEntity toBeSaved);
    Uni<Collection<StoreEntity>> save(Collection<StoreEntity> toBeSaved);
  }
}