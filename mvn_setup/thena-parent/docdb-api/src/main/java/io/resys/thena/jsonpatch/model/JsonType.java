package io.resys.thena.jsonpatch.model;

import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;



public class JsonType {
  private final JsonObjectType objectType;
  private JsonType(JsonObjectType objectType) {
    this.objectType = objectType;
  }
  
  public static JsonType of(@Nullable JsonObject previousState, @Nullable JsonObject nextState) {
    RepoAssert.isTrue(previousState != null || nextState != null, () -> "Both json states can't be null");
    return new JsonType(new JsonObjectType(previousState, nextState));
  }
  
  public interface JsonTypeVisitor<T> {
    void visitAdd(JsonPatchPointer currentPath, Object newState); //patchBuilder.add().path(pointer).value(nextState).operation(Operation.ADD).build();
    void visitRemove(JsonPatchPointer currentPath, Object stateRemoved); //patchBuilder.add().path(pointer).value(previousState).operation(Operation.REMOVE).build(); 
    
    //patchBuilder.add().path(pointer).value(previousState).operation(Operation.TEST).build();
    void visitTest(JsonPatchPointer currentPath, Object previousState);
    //patchBuilder.add().path(pointer).srcValue(previousState).value(nextState).operation(Operation.REPLACE).build();
    void visitReplace(JsonPatchPointer currentPath, Object previousState, Object nextState);
    
    T end();
  }
  
  public <T> T accept(JsonTypeVisitor<T> visitor) {
    objectType.accept(visitor);
    return visitor.end();
  }
  

}
