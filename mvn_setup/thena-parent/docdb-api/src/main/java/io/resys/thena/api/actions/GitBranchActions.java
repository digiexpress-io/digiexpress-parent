package io.resys.thena.api.actions;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import io.resys.thena.api.actions.GitPullActions.MatchCriteria;
import io.resys.thena.api.entities.GitContainer;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.git.Blob;
import io.resys.thena.api.entities.git.Branch;
import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.entities.git.Tree;
import io.resys.thena.api.envelope.BlobContainer;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.smallrye.mutiny.Uni;

public interface GitBranchActions {

  BranchObjectsQuery branchQuery();
  
  // build REF world state, no blobs by default
  interface BranchObjectsQuery {
    BranchObjectsQuery branchName(String ref);
    BranchObjectsQuery docsIncluded();
    BranchObjectsQuery docsIncluded(boolean docsIncluded);
    BranchObjectsQuery matchBy(List<MatchCriteria> blobCriteria);
    Uni<QueryEnvelope<GitBranchActions.BranchObjects>> get();
  }

  @Value.Immutable
  interface BranchObjects extends BlobContainer, GitContainer {
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


}
