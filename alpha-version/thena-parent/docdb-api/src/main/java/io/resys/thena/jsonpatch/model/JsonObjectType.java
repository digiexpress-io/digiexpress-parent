package io.resys.thena.jsonpatch.model;

import io.resys.thena.jsonpatch.model.JsonType.JsonTypeVisitor;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class JsonObjectType {
  private final Object previousState;
  private final Object nextState;
  private final JsonPatchPointer pointer;
  private JsonTypeVisitor<?> visitor;
  
  public JsonObjectType(Object previousState, Object nextState) {
    super();
    this.previousState = previousState;
    this.nextState = nextState;
    this.pointer = JsonPatchPointer.create();
  }
  
  public void accept(JsonTypeVisitor<?> visitor) {
    this.visitor = visitor;
    
    if (java.util.Objects.equals(previousState, nextState)) {
      return;
    }
    if (previousState == null) {
      visitor.visitAdd(pointer, nextState);
      return;
    } else if (nextState == null) {
      visitor.visitRemove(pointer, previousState);
      return;
    }

    if (previousState instanceof JsonObject && nextState instanceof JsonObject) {
      acceptJsonObject(pointer, (JsonObject) previousState, (JsonObject) nextState);
      return;
    }

    if (previousState instanceof JsonArray && nextState instanceof JsonArray) {
      new JsonArrayType((JsonArray) previousState, (JsonArray) nextState) {
        @Override protected void handleRemove(JsonPatchPointer currentPath, Object stateRemoved) {
          acceptAnyObjects(currentPath, stateRemoved, null);
        } 
        @Override protected void handleConflict(JsonPatchPointer currentPath, Object previousState, Object nextState) {
          acceptAnyObjects(currentPath, previousState, nextState);
        }
        @Override protected void handleAdd(JsonPatchPointer currentPath, Object newState) {
          acceptAnyObjects(currentPath, null, newState);
        }
      }
      .accept(pointer);
      return;
    }
    visitor.visitTest(pointer, previousState);
    visitor.visitReplace(pointer, previousState, nextState);
  }
  
  

  /**
   * Add changes from any two objects irrelevant if it is object/array/scalar
   */
  private void acceptAnyObjects(JsonPatchPointer pointer, Object previousState, Object nextState) {
    if (java.util.Objects.equals(previousState, nextState)) {
      return;
    }
    if (previousState == null) {
      visitor.visitAdd(pointer, nextState);
      return;
    } else if (nextState == null) {
      visitor.visitRemove(pointer, previousState);
      return;
    }

    if (previousState instanceof JsonObject && nextState instanceof JsonObject) {
      acceptJsonObject(pointer, (JsonObject) previousState, (JsonObject) nextState);
      return;
    }

    if (previousState instanceof JsonArray && nextState instanceof JsonArray) {
      
      new JsonArrayType((JsonArray) previousState, (JsonArray) nextState) {
        @Override protected void handleRemove(JsonPatchPointer currentPath, Object stateRemoved) {
          acceptAnyObjects(currentPath, stateRemoved, null);
        } 
        @Override protected void handleConflict(JsonPatchPointer currentPath, Object previousState, Object nextState) {
          acceptAnyObjects(currentPath, previousState, nextState);
        }
        @Override protected void handleAdd(JsonPatchPointer currentPath, Object newState) {
          acceptAnyObjects(currentPath, null, newState);
        }
      }
      .accept(pointer);
      
      return;
    }
    visitor.visitTest(pointer, previousState);
    visitor.visitReplace(pointer, previousState, nextState);
  }
  
  
  /**
   * Add changes between TWO JsonObjects
   */
  private void acceptJsonObject(JsonPatchPointer pointer, JsonObject previousState, JsonObject nextState) {
    for (final var key : previousState.fieldNames()) {
      final JsonPatchPointer currentPath = pointer.append(key);
      if (nextState.containsKey(key)) {
        // add changes between both states 
        acceptAnyObjects(currentPath, previousState.getValue(key), nextState.getValue(key));        
      } else {
        // add removed fields
        visitor.visitTest(currentPath, previousState.getValue(key));
        visitor.visitRemove(currentPath, previousState.getValue(key));
      }
    }
    
    // add new fields
    for (final var key : nextState.fieldNames()) {
      if (!previousState.containsKey(key)) {
        final JsonPatchPointer currentPath = pointer.append(key);
        visitor.visitAdd(currentPath, nextState.getValue(key));
      }
    }
  }
}
