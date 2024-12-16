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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import jakarta.json.JsonPatch.Operation;

public class RelativePathType {
  private final JsonPatchPointer path;
  private final List<ChangeType> changes;

  private RelativePathType(JsonPatchPointer path, List<ChangeType> changes) {
    super();
    this.path = path;
    this.changes = changes;
  }

  public static RelativePathType of(JsonPatchPointer path, List<ChangeType> changes) {
    return new RelativePathType(path, changes);
  }

  public JsonPatchPointer between(int startId, int endId) {
    final var tokens = new ArrayList<>(IntStream.range(0, path.getTokens().size()).boxed().map(junk -> 0) // 0 token
                                                                                                          // list
        .toList());

    for (int changeId = startId; changeId <= endId; changeId++) {
      final var change = changes.get(changeId);
      if (change.getOperation() == Operation.ADD || change.getOperation() == Operation.REMOVE) {
        setTokens(path, change, tokens);
      }
    }

    var nextPath = path;
    for (int tokenId = 0; tokenId < tokens.size(); tokenId++) {
      final var value = tokens.get(tokenId);
      if (value != 0) {
        final var currValue = nextPath.getToken(tokenId).getIndex();
        nextPath = nextPath.withToken(tokenId, Integer.toString(currValue + value));
      }
    }
    return nextPath;
  }

  private static void setTokens(JsonPatchPointer path, ChangeType changeType, List<Integer> tokens) {

    // different path
    if (changeType.getPath().getTokens().size() > path.getTokens().size()) {
      return;
    }

    // last common token
    int found = -1;
    for (int i = 0; i < changeType.getPath().getTokens().size() - 1; i++) {
      if (changeType.getPath().getToken(i).equals(path.getToken(i))) {
        found = i;
      } else {
        break;
      }
    }
    if (changeType.getPath().getTokens().size() - 2 == found
        && changeType.getPath().getToken(changeType.getPath().getTokens().size() - 1).isArrayIndex()) {

      setTokens(changeType, changeType.getPath().getTokens().size() - 1, tokens);
    }
  }

  private static void setTokens(ChangeType changeType, int tokenId, List<Integer> tokens) {
    if (changeType.getOperation() == Operation.ADD) {
      tokens.set(tokenId, tokens.get(tokenId) - 1);
      return;
    }
    if (changeType.getOperation() == Operation.REMOVE) {
      tokens.set(tokenId, tokens.get(tokenId) + 1);
    }
  }
}
