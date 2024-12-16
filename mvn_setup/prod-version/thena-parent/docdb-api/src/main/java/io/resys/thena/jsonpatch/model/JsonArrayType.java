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

import java.util.List;
import java.util.stream.StreamSupport;

import org.apache.commons.collections4.ListUtils;

import io.vertx.core.json.JsonArray;


public abstract class JsonArrayType {
  private final List<Object> nodes_lcs;
  private final JsonArray nodes_previous; 
  private final JsonArray nodex_next;
  
  private int index_lcs = 0;
  private int index_previous = 0;
  private int index_next = 0;
  private int index_path = 0;
  
  public JsonArrayType(JsonArray previousState, JsonArray nextState) {
    this.nodes_lcs = ListUtils.longestCommonSubsequence(
        StreamSupport.stream(previousState.spliterator(), false).toList(), 
        StreamSupport.stream(nextState.spliterator(), false).toList());
    this.nodes_previous = previousState;
    this.nodex_next = nextState;   
  }
  
  protected abstract void handleAdd(JsonPatchPointer currentPath, Object newState); 
  protected abstract void handleRemove(JsonPatchPointer currentPath, Object stateRemoved); 
  protected abstract void handleConflict(JsonPatchPointer currentPath, Object previousState, Object nextState);
  
  public void accept(JsonPatchPointer currentPath) {
    while (index_lcs < nodes_lcs.size()) {
      this.visitLongestCommonSubsequence(currentPath);
    }
    
    while ((index_previous < nodes_previous.size()) && (index_next < nodex_next.size())) {
      final var srcNode = nodes_previous.getValue(index_previous++);
      final var targetNode = nodex_next.getValue(index_next++);
      handleConflict(currentPath.append(index_path++), srcNode, targetNode);
    }
    
    while (index_next < nodex_next.size()) {      
      this.handleAdd(
          currentPath.append(index_path++), 
          nodex_next.getValue(index_next++));
    }
    
    while (index_previous < nodes_previous.size()) {
      this.handleRemove(
          currentPath.append(index_path),
          nodes_previous.getValue(index_previous++)
      );
    }
  }
  
  private void visitLongestCommonSubsequence(JsonPatchPointer path) {
    final var lcsNode = nodes_lcs.get(index_lcs);
    final var srcNode = nodes_previous.getValue(index_previous);
    final var targetNode = nodex_next.getValue(index_next);
    
    // all is equal, not diff to generate
    if (lcsNode.equals(srcNode) && lcsNode.equals(targetNode)) {
      index_previous++;
      index_next++;
      index_path++;
      index_lcs++;
      return;
    }
    
    // lcs = start => add target
    if (lcsNode.equals(srcNode)) {
      this.handleAdd(path.append(index_path), targetNode);
      index_path++;
      index_next++;
      return;
    } 
    
    // lcs = end => remove start
    if (lcsNode.equals(targetNode)) { //targetNode node is same as lcs, but not src
      this.handleRemove(path.append(index_path), srcNode);
      index_previous++;
      return;
    }
    
    // conflict, both positions different
    this.handleConflict(path.append(index_path), srcNode, targetNode);
    index_path++;
    index_previous++;
    index_next++;
    
  }
}
