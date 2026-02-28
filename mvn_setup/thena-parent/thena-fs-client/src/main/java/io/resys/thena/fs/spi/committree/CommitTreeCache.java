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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.support.RepoAssert;

public class CommitTreeCache {
  
  // master ref
  private final Map<String, CommitTree> all_commitTrees = new HashMap<>();
  private final Set<String> all_objectIds = new HashSet<>();
  private final Map<String, Blob> blobs = new HashMap<>();
  private final Map<String, Props> props = new HashMap<>();
  
  private final List<String> blobIds = new ArrayList<>();
  private final List<String> propsIds = new ArrayList<>();
  
  
  public void add(CommitTree commitTree) {
    this.all_commitTrees.put(commitTree.getCommit().getId(), commitTree);
  }
  
  public void add(Node node) {
    node.getBlobId().ifPresent(this.blobIds::add);
    node.getPropsId().ifPresent(this.propsIds::add);
    this.all_objectIds.add(node.getObjectId());
  }
  
  public CommitTree getTreeByCommitId(String commitId) {
    return RepoAssert.notNull(all_commitTrees.get(commitId), () -> "Can't find commit tree by commit id: " + commitId);
  }

  public Set<String> getAllObjectIds() {
    return all_objectIds;
  }
  
  public void addAllBlobs(List<Blob> blobs) {
    blobs.forEach(blob -> this.blobs.put(blob.getId(), blob));
  }
  
  public void addAllProps(List<Props> props) {
    props.forEach(prop -> this.props.put(prop.getId(), prop));
  }
  
  public CommitTreeBlobsAndProps getDeps() {
    return new CommitTreeBlobsAndProps(blobIds, propsIds);
  }
  
  public Optional<Blob> getBlob(Node node) {
    return node.getBlobId().map(blobs::get);
  }
  public Optional<Props> getProps(Node node) {
    return node.getPropsId().map(props::get); 
  }
}
