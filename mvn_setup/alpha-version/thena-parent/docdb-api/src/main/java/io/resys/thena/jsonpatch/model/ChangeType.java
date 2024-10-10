package io.resys.thena.jsonpatch.model;

import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;
import lombok.Setter;


@lombok.Data @lombok.RequiredArgsConstructor
public class ChangeType {
  
  private final jakarta.json.JsonPatch.Operation operation;
  private final JsonPatchPointer path;
  @Nullable private final Object srcValue;
  @Nullable private final JsonPatchPointer toPath;
  @Nullable private final Object value;
   
  @lombok.experimental.Accessors(chain = true, fluent = true) @Setter
  public static class ChangeTypeBuilder {
    private jakarta.json.JsonPatch.Operation operation;
    private JsonPatchPointer path;
    private Object srcValue;
    private JsonPatchPointer toPath;
    private Object value;
    
    public ChangeType build() {
      RepoAssert.notNull(operation, () -> "operation must be defined!");
      RepoAssert.notNull(path, () -> "path must be defined!");
      return new ChangeType(operation, path, srcValue, toPath, value);
    }
  }
}