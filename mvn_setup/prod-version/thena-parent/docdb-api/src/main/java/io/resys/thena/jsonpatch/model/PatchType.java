package io.resys.thena.jsonpatch.model;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import jakarta.annotation.Nullable;
import jakarta.json.JsonPatch.Operation;

@lombok.Data @lombok.RequiredArgsConstructor
public class PatchType {
  private final Operation operation;
  private final JsonPointer path;
  @Nullable private final JsonPointer from;
  @Nullable private final Object value;
  
  private static final String NAMES_OP = "op";
  private static final String NAMES_VALUE = "value";
  private static final String NAMES_PATH = "path";
  private static final String NAMES_FROM = "from";
  @SuppressWarnings("unused")
  private static final String NAMES_FROM_VALUE = "fromValue";
  
  public static PatchType decode(JsonObject patchOperation) {
    RepoAssert.notNull(patchOperation.containsKey(NAMES_OP), () -> "operation must be defined!");
    RepoAssert.notNull(patchOperation.containsKey(NAMES_PATH), () -> "path must be defined!");
    
    final var operation = Operation.fromOperationName(patchOperation.getString(NAMES_OP));
    final var path = JsonPointer.from(patchOperation.getString(NAMES_PATH));
    final var from = patchOperation.containsKey(NAMES_FROM) ? JsonPointer.from(patchOperation.getString(NAMES_FROM)) : null;
    final var value = patchOperation.containsKey(NAMES_VALUE) ? patchOperation.getValue(NAMES_VALUE) : null;

    return new PatchType(operation, path, from, value); 
  }
  
  public static JsonObject encode(ChangeType data) {
    final var operation = data.getOperation();
    if(operation == Operation.TEST) {
      return null;
    }
    
    final var jsonOp = JsonObject.of(NAMES_OP, operation.operationName());
    switch (operation) {
    case ADD: {
      return jsonOp
        .put(NAMES_PATH, data.getPath().toString())
        .put(NAMES_VALUE, data.getValue());
    }
    case REMOVE: {
      return jsonOp
        //.put(NAMES_VALUE, diff.getValue())
        .put(NAMES_PATH, data.getPath().toString());
    }
    case REPLACE: {
      return jsonOp
        //.put(NAMES_FROM_VALUE, diff.getSrcValue())
        .put(NAMES_PATH, data.getPath().toString())
        .put(NAMES_VALUE, data.getValue());
    }
    case MOVE:
    case COPY: {
      return jsonOp
        .put(NAMES_FROM, data.getPath().toString())
        .put(NAMES_PATH, data.getToPath().toString()); 
    }
    case TEST: {
      return jsonOp
        .put(NAMES_PATH, data.getPath().toString())
        .put(NAMES_VALUE, data.getValue());
    }
    default: RepoAssert.fail("Unknown operation type: '%s'".formatted(operation));
    }
    return null;
  }
}
