package io.resys.thena.jsonpatch.visitors;


import java.util.ArrayList;
import java.util.List;

import io.resys.thena.jsonpatch.model.ChangeType;
import io.resys.thena.jsonpatch.model.JsonPatchPointer;
import io.resys.thena.jsonpatch.model.JsonType.JsonTypeVisitor;
import io.resys.thena.jsonpatch.model.PatchType;
import io.resys.thena.jsonpatch.model.RelativePathType;
import io.vertx.core.json.JsonArray;
import jakarta.json.JsonPatch.Operation;


public class CreatePatch implements JsonTypeVisitor<JsonArray> {
  private final List<ChangeType> collected = new ArrayList<ChangeType>();
  private final List<ChangeType> moved = new ArrayList<ChangeType>();
  
  @Override
  public void visitAdd(JsonPatchPointer currentPath, Object newState) {
    append().path(currentPath).value(newState).operation(Operation.ADD).build();
  }
  @Override
  public void visitRemove(JsonPatchPointer currentPath, Object stateRemoved) {
    append().path(currentPath).value(stateRemoved).operation(Operation.REMOVE).build();
  }
  @Override
  public void visitTest(JsonPatchPointer currentPath, Object previousState) {
    //append().path(currentPath).value(previousState).operation(Operation.TEST).build();    
  }
  @Override
  public void visitReplace(JsonPatchPointer currentPath, Object previousState, Object nextState) {
    append().path(currentPath).srcValue(previousState).value(nextState).operation(Operation.REPLACE).build();
  }
  private ChangeType.ChangeTypeBuilder append() {
    return new ChangeType.ChangeTypeBuilder() {
      @Override
      public ChangeType build() {
        final var op = super.build();
        collected.add(op);
        return op;
      }
    };
  }
  
  @Override
  public JsonArray end() {
    moved.clear();
    moved.addAll(collected);
    
    for (int id = 0; id < moved.size(); id++) {
      final var change = moved.get(id);
      final var applyMove = change.getOperation() == Operation.REMOVE || change.getOperation() == Operation.ADD;
      if (!applyMove) {
        continue;
      }
      
      for (int nextId = id + 1; nextId < moved.size(); nextId++) {
        final var nextChange = moved.get(nextId);
        final var identicalValues = change.getValue().equals(nextChange.getValue());
        if (!identicalValues) {
          continue;
        }
        final var move = visitMove(id, change, nextId, nextChange);
        if (move != null) {
          moved.remove(nextId);
          moved.set(id, move);
          break;
        }
      }
    }
    
    final var generatedPatch = new JsonArray();
    moved.forEach(change -> {
      final var patch = PatchType.encode(change);
      if(patch != null) {
        generatedPatch.add(patch);
      }
    });
    return generatedPatch;
  }  

  private ChangeType visitMove(int prevId, ChangeType prev, int nextId, ChangeType next) {
    if (prev.getOperation() == Operation.REMOVE  && next.getOperation() == Operation.ADD) {
      final var fromPath = prev.getPath();
      final var toPath = RelativePathType.of(next.getPath(), moved).between(prevId + 1, nextId - 1);
      return new ChangeType.ChangeTypeBuilder()
          .operation(Operation.MOVE)
          .path(fromPath)
          .toPath(toPath)
          .build();
      
    }
    
    if (prev.getOperation() == Operation.ADD && next.getOperation() == Operation.REMOVE) {
      final var fromPath = RelativePathType.of(next.getPath(), moved).between(prevId, nextId - 1);
      final var toPath = prev.getPath(); 
      return new ChangeType.ChangeTypeBuilder()
          .operation(Operation.MOVE)
          .path(fromPath)
          .toPath(toPath)
          .build();
    }
    
    return null;
  }
}
