package io.resys.thena.docdb.spi;

import java.util.List;

import javax.annotation.Nullable;

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

import org.immutables.value.Value;

import io.resys.thena.docdb.api.actions.PullActions.MatchCriteria;
import io.resys.thena.docdb.api.models.ThenaObject.Blob;
import io.resys.thena.docdb.api.models.ThenaObject.BlobHistory;
import io.resys.thena.docdb.api.models.ThenaObject.Branch;
import io.resys.thena.docdb.api.models.ThenaObject.Commit;
import io.resys.thena.docdb.api.models.ThenaObject.CommitLock;
import io.resys.thena.docdb.api.models.ThenaObject.Tag;
import io.resys.thena.docdb.api.models.ThenaObject.Tree;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface ClientQuery {
  TagQuery tags();
  CommitQuery commits();
  RefQuery refs();
  TreeQuery trees();
  BlobQuery blobs();
  BlobHistoryQuery blobHistory();
  
  
  interface RefQuery {
    Uni<Branch> name(String name);
    Uni<Branch> nameOrCommit(String refNameOrCommit);
    Uni<Branch> get();
    Multi<Branch> findAll();
  }
  
  interface BlobHistoryQuery {
    BlobHistoryQuery latestOnly(boolean latestOnly);
    BlobHistoryQuery blobName(String name);
    BlobHistoryQuery criteria(MatchCriteria ... criteria);
    BlobHistoryQuery criteria(List<MatchCriteria> criteria);
    Multi<BlobHistory> find();
  }
  
  interface BlobQuery {
    Uni<Blob> getById(String blobId);
    
    Multi<Blob> findAll();
    Multi<Blob> findAll(String treeId, List<String> docIds, List<MatchCriteria> matchBy);
    Multi<Blob> findAll(String treeId, List<MatchCriteria> criteria);
  }
  interface CommitQuery {
    Uni<Commit> getById(String commitId);
    Uni<CommitLock> getLock(LockCriteria criteria);
    Multi<Commit> findAll();
  }
  interface TreeQuery {
    Uni<Tree> getById(String treeId);
    Multi<Tree> findAll();
  }
  interface TagQuery {
    TagQuery name(String name);
    Uni<DeleteResult> delete();
    Uni<Tag> getFirst();
    Multi<Tag> find();
  }
  
  @Value.Immutable
  interface LockCriteria {
    @Nullable String getCommitId(); 
    String getHeadName();
    List<String> getTreeValueIds();
  }
  
  @Value.Immutable
  interface DeleteResult {
    long getDeletedCount();
  }
}
