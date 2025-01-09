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
