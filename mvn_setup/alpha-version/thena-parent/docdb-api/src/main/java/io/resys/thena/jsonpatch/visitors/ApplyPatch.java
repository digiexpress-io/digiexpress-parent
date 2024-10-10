package io.resys.thena.jsonpatch.visitors;

import io.resys.thena.jsonpatch.model.JsonPatchPointer;
import io.resys.thena.jsonpatch.model.PatchType;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import jakarta.json.JsonPatch.Operation;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ApplyPatch {
  private JsonObject target;

  public JsonObject close() {
    return target;
  }

  public ApplyPatch start(JsonArray patch, JsonObject source) {
    this.target = source;
    patch.forEach(operation -> {
      if (operation instanceof JsonObject) {
        visitOperation((JsonObject) operation);
      }
    });
    return this;
  }

  private void visitOperation(JsonObject json) {
    final var patch = PatchType.decode(json);
    final var operation = patch.getOperation();
    final var path = patch.getPath();
    switch (operation) {
    case REMOVE: {
      remove(path);
      break;
    }
    case ADD: {
      add(path, patch.getValue());
      break;
    }
    case REPLACE: {
      replace(path, patch.getValue());
      break;
    }
    case MOVE: {
      move(patch.getFrom(), path);
      break;
    }
    case COPY: {
      copy(patch.getFrom(), path);
      break;
    }
    case TEST: {
      test(path, patch.getValue());
      break;
    }
    }
  }

  public void move(JsonPointer fromPath, JsonPointer toPath) {
    final var valueNode = fromPath.queryJson(target);
    remove(fromPath);
    set(toPath, valueNode, Operation.MOVE);
  }

  public void copy(JsonPointer fromPath, JsonPointer toPath) {
    final var valueToCopy = fromPath.queryJson(target);
    set(toPath, valueToCopy, Operation.COPY);
  }

  public void add(JsonPointer path, Object value) {
    set(path, value, Operation.ADD);
  }

  public void test(JsonPointer path, Object value) {
    final var valueNode = path.queryJson(target);
    if (!valueNode.equals(value)) {
      //throw new JsonPatchException(("Patch-Test failed for path: '%s', expected: '%s' but found: '%s'")
      //    .formatted(parseObjectValue(value), parseObjectValue(valueNode), path.toString()));
      
      log.error("Patch-Test failed for path: '{}', expected: '{}' but found: '{}'",
          path.toString(),
          parseObjectValue(value), 
          parseObjectValue(valueNode) 
          
      );
    }
  }

  @SuppressWarnings("unchecked")
  public void replace(JsonPointer path, Object value) {
    if (path.isRootPointer()) {
      target = new JsonObject(value.toString());
      return;
    }
    final var parent = path.copy().parent();
    final var parentNode = parent.queryJson(target);
    final var token = new JsonPatchPointer(path).getLastPointer();

    
    if (parentNode instanceof JsonArray) {
      final var container = (JsonArray) parentNode;
      if (token.isArrayAppend()) {
        container.add(value);
      } else {
        if (token.getIndex() > container.size()) {
          throw new JsonPatchException("Can't set path: '%s' because array index: '%s' is out of bounds".formatted(path, token.getIndex()));
        }
        container.getList().add(token.getIndex(), value);
      }
    } else if(parentNode instanceof JsonObject) {
      ((JsonObject) parentNode).put(token.getToken(), value);   
    } else {
      throw new JsonPatchException("Can't set path: '%s' because parent is not object or array".formatted(path));
    }
  }

  public void remove(JsonPointer path) {
    if (path.isRootPointer()) {
      throw new JsonPatchException("Can't remove root path: '%s'".formatted(path));
    }

    final var parent = path.copy().parent();
    final var parentNode = parent.queryJson(target); 
    final var token = new JsonPatchPointer(path).getLastPointer().getToken();

    if (parentNode instanceof JsonObject) {
      ((JsonObject) parentNode).remove(token);
    } else if (parentNode instanceof JsonArray) {
      final var pos = Integer.parseInt(token);
      if(((JsonArray) parentNode).size() > pos) {
        ((JsonArray) parentNode).remove(pos);        
      }//
    } else {
      //scalar value
      ((JsonObject) parentNode).remove(token);
    }
  }

  @SuppressWarnings("unchecked")
  private void set(JsonPointer path, Object value, Operation forOp) {
    if (path.isRootPointer()) {
      target = new JsonObject(value.toString());
      return;
    }
    final var parent = path.copy().parent();
    final var parentNode = parent.queryJson(target);
    final var patchPointer = new JsonPatchPointer(path);
    final var token = patchPointer.getLastPointer();    
    if (parentNode instanceof JsonArray) {
      final var container = (JsonArray) parentNode;
      

      if (token.isArrayAppend()) {
        container.add(value);
      } else {
        if (token.getIndex() > container.size()) {
          throw new JsonPatchException("Can't set path: '%s' because array index: '%s' is out of bounds".formatted(path, token.getIndex()));
        }
        container.getList().add(token.getIndex(), value);
      }
    } else if(parentNode instanceof JsonObject) {
      ((JsonObject) parentNode).put(token.getToken(), value);
    } else {
      throw new JsonPatchException("Can't set path: '%s' because parent is not object or array".formatted(path)); 
    }
  }

  public static class JsonPatchException extends RuntimeException {
    private static final long serialVersionUID = 7221473192009045716L;

    public JsonPatchException(String message) {
      super(message);
    }
  }

  private static String parseObjectValue(Object value) {
    if (value == null) {
      return "null";
    } else if (value instanceof JsonArray) {
      return "array";
    } else if (value instanceof JsonObject) {
      return "object";
    }
    return value.toString();
  }
}
