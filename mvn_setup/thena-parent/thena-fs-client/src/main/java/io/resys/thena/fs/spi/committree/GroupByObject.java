package io.resys.thena.fs.spi.committree;

/*-
 * #%L
 * thena-fs-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.resys.thena.fs.api.commits.CommitQuery.CommitsByObject;
import io.resys.thena.fs.entities.Entity;
import io.resys.thena.fs.spi.commit.CommitsByObjectImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GroupByObject implements CommitTreeVisitor {
  private final CommitTreeCache cache;
  private final Map<String, CommitsByObject> objects = new HashMap<>();

  
  @Override
  public void visit(CommitTree previous, CommitTree next) {
    if(previous == null) {
      init(next);
    } else {
      diff(previous, next);
    }
  }

  private void init(CommitTree next) {
    for(final var entry : next.getNodes()) {
      final var objectId = entry.getValue().getObjectId();
      getBuilder(objectId)
        .add(next.getCommit())
        .add(entry.getValue())
        .addBlobs(cache.getBlob(entry.getValue()))
        .addProps(cache.getProps(entry.getValue()));
    }
  }
      
  private void diff(CommitTree previous, CommitTree next) {
    for(final var entry : next.getNodes()) {

      final var objectId = entry.getValue().getObjectId();
      final var prev_node = previous.getNode(objectId);
      final var next_node = next.getNode(objectId);
      
      final var isBlobChanged = prev_node.getBlobId().orElse(Entity.EMPTY_UUID).equals(next_node.getBlobId().orElse(Entity.EMPTY_UUID));
      final var isPropsChanged = prev_node.getPropsId().orElse(Entity.EMPTY_UUID).equals(next_node.getPropsId().orElse(Entity.EMPTY_UUID));
      
      if(isBlobChanged || isPropsChanged) {
        getBuilder(objectId)
          .add(next.getCommit())
          .add(next_node)
          .addBlobs(cache.getBlob(next_node))
          .addProps(cache.getProps(next_node));
      }
    }
  }

  public Collection<CommitsByObject> close() {
    return objects.values();
  }
  
  
  private CommitsByObjectImpl getBuilder(String objectId) {
    CommitsByObjectImpl result = (CommitsByObjectImpl) objects.get(objectId);
    if(result == null) {
      result = new CommitsByObjectImpl(objectId);
      objects.put(objectId, result); 
    }
    return result;
  }

}
