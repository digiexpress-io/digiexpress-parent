package io.resys.thena.api.entities.git;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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
import java.util.Map;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.ThenaObjects;
import io.resys.thena.api.entities.git.GitEntity.IsGitObject;
import io.resys.thena.api.envelope.BlobContainer;
import io.resys.thena.api.envelope.BlobContainer.BlobVisitor;

public interface ThenaGitObjects extends ThenaObjects { 
  @Value.Immutable
  public interface GitRepoObjects extends ThenaGitObjects {
    Map<String, Branch> getBranches();
    Map<String, Tag> getTags();
    Map<String, IsGitObject> getValues();   
  }

  
  @Value.Immutable
  interface CommitObjects extends ThenaGitObjects, BlobContainer  {
    Tenant getRepo();
    Commit getCommit();
    Tree getTree();
    Map<String, Blob> getBlobs(); //only if loaded
    
    default <T> List<T> accept(BlobVisitor<T> visitor) {
      return getTree().getValues().values().stream()
          .map(treeValue -> getBlobs().get(treeValue.getBlob()))
          .map(blob -> visitor.visit(blob.getValue()))
          .collect(Collectors.toUnmodifiableList());
    }
  }
  
  @Value.Immutable
  interface PullObject extends ThenaGitObjects {
    Tenant getRepo();
    Commit getCommit();
    //Tree getTree();
    Blob getBlob();
    
    default <T> T accept(BlobVisitor<T> visitor) {
      return visitor.visit(getBlob().getValue());
    }
  }
  

  @Value.Immutable
  interface PullObjects extends BlobContainer, ThenaGitObjects {
    Tenant getRepo();
    Commit getCommit();
    //Tree getTree();
    List<Blob> getBlob();
    
    default <T> List<T> accept(BlobVisitor<T> visitor) {
      return getBlob().stream()
          .map(blob -> visitor.visit(blob.getValue()))
          .collect(Collectors.toUnmodifiableList());
    }
  }
  @Value.Immutable
  interface BranchObjects extends BlobContainer, ThenaGitObjects {
    Tenant getRepo();
    Branch getRef();
    Commit getCommit();
    Tree getTree();
    Map<String, Blob> getBlobs(); //only if loaded
    
    default <T> List<T> accept(BlobVisitor<T> visitor) {
      return getTree().getValues().values().stream()
          .map(treeValue -> getBlobs().get(treeValue.getBlob()))
          .map(blob -> visitor.visit(blob.getValue()))
          .collect(Collectors.toUnmodifiableList());
    }
  }
  
  @Value.Immutable
  interface HistoryObjects extends BlobContainer, ThenaGitObjects {
    List<BlobHistory> getValues();
    
    default <T> List<T> accept(BlobVisitor<T> visitor) {
      return getValues().stream()
          .map(value -> value.getBlob())
          .map(blob -> visitor.visit(blob.getValue()))
          .collect(Collectors.toUnmodifiableList());
    }
  }
  
}
