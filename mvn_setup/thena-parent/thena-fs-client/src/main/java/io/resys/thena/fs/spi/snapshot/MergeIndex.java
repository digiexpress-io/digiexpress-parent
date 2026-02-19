package io.resys.thena.fs.spi.snapshot;

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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.ImmutableIndex;
import io.resys.thena.fs.entities.Index;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.support.RepoAssert;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class MergeIndex {
  private final Optional<Ref> ref;
  private final OffsetDateTime now;
  
  private final List<ImmutableIndex.Builder> updates = new ArrayList<>();
  private final List<ImmutableIndex.Builder> inserts = new ArrayList<>();
  
  public void rm(List<Node> nodes) {
    for(final var node : nodes) {
      merge(node, node);
    }
  }
  
  public void merge(Node prev, Node next) {
    RepoAssert.isTrue(ref.isPresent(), () ->  "Ref lock is missing, no previous data, merge requires previous change into what to apply changes!");
    RepoAssert.isTrue(prev.getTransitives() != null, () ->  "previous 'node.transitives' must be loaded!");
    RepoAssert.isTrue(prev.getTransitives().getObjectIndex() != null, () ->  "previous node 'node.transitives.objectIndex' must be loaded!");
    
    updates.add(ImmutableIndex.builder()
        .from(prev.getTransitives().getObjectIndex())
        .updatedAt(now));
  }
  
  public void create(Node next) {
    inserts.add(ImmutableIndex.builder()
        .objectId(next.getObjectId())
        .createdAt(now)
        .updatedAt(now));
  }
  
  public MergeIndexResult close(Commit commit) {
    return new MergeIndexResult(
        updates.stream().map(b -> b.updatedBy(commit.getId()).build()).toList(), 
        inserts.stream().map(b ->  b.createdBy(commit.getId()).updatedBy(commit.getId()).build()).toList()
    );
  }
  
  @Value
  public static class MergeIndexResult {
    List<? extends Index> updates;
    List<? extends Index> inserts;
  }
}
