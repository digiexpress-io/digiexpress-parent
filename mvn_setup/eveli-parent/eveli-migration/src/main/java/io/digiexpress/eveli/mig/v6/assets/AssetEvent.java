package io.digiexpress.eveli.mig.v6.assets;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

@Value.Immutable
public interface AssetEvent {
  ObjectType getSourceType();
  OffsetDateTime getCreatedAt();
  List<ObjectOperation> getOperations();
  
  
  interface ObjectOperation {
    ObjectOperationType getType();
  }
  
  @Value.Immutable
  interface MergeOperation extends ObjectOperation {
    String getObjectId();
    JsonObject getPrevious();
    JsonObject getNewObject();

    default ObjectOperationType getType() {
      return ObjectOperationType.MERGE_OBJECT;
    }
  }
  
  @Value.Immutable
  interface NewOperation extends ObjectOperation {
    String getObjectId();
    JsonObject getNewObject();

    default ObjectOperationType getType() {
      return ObjectOperationType.NEW_OBJECT;
    }
  }
  
  @Value.Immutable
  interface RmOperation extends ObjectOperation {
    String getObjectId();
    JsonObject getPrevious();

    default ObjectOperationType getType() {
      return ObjectOperationType.RM_OBJECT;
    }
  }
  
  @Value.Immutable
  interface TagOperation extends ObjectOperation {
    String getId();
    String getName();
    String getAuthor();
    OffsetDateTime getCreatedAt();
    OffsetDateTime getStartsAt();
    String getDescription();
    
    Optional<JsonObject> getErrors();
    JsonObject getSources();
    
    default ObjectOperationType getType() {
      return ObjectOperationType.TAG_OBJECTS;
    }
  }
  
  enum ObjectOperationType {
    NEW_OBJECT,
    RM_OBJECT,
    MERGE_OBJECT,
    TAG_OBJECTS
  }
  
  enum ObjectType {
    WRENCH, STENCIL, ENVIR
  }
  
  
  interface AssetEventMigration {
    Uni<Void> execute();
  }
}
