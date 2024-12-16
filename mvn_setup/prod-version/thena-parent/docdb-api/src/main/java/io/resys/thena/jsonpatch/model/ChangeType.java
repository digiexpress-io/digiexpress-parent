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
