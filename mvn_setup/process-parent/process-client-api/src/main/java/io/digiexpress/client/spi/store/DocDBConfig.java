package io.digiexpress.client.spi.store;

import java.util.Collection;

import org.immutables.value.Value;

import io.digiexpress.client.api.ClientStore.StoreEntity;
import io.digiexpress.client.api.ClientStore.StoreGid;
import io.digiexpress.client.api.ClientStore.StoreState;
import io.digiexpress.client.api.Parser;
import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.ThenaGitObject.Blob;
import io.resys.thena.docdb.api.models.ThenaObjects.PullObject;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;


@Value.Immutable
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
    JsonObject toString(StoreEntity entity);
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
    QueryEnvelope<PullObject> getBlob();
    StoreEntity getEntity();
  } 
}