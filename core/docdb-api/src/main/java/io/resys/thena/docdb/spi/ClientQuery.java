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

import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.BlobHistory;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.CommitLock;
import io.resys.thena.docdb.api.models.Objects.Ref;
import io.resys.thena.docdb.api.models.Objects.Tag;
import io.resys.thena.docdb.api.models.Objects.Tree;
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
    Uni<Ref> name(String name);
    Uni<Ref> nameOrCommit(String refNameOrCommit);
    Uni<Ref> get();
    Multi<Ref> findAll();
  }
  
  interface BlobHistoryQuery {
    BlobHistoryQuery latestOnly(boolean latestOnly);
    BlobHistoryQuery blobName(String name);
    BlobHistoryQuery criteria(BlobCriteria ... criteria);
    BlobHistoryQuery criteria(List<BlobCriteria> criteria);
    Multi<BlobHistory> find();
  }
  
  interface BlobQuery {
    Uni<Blob> getById(String blobId);
    
    Multi<Blob> findAll();
    Multi<Blob> findAll(String treeId, List<String> blobNames, List<BlobCriteria> criteria);
    Multi<Blob> findAll(String treeId, List<BlobCriteria> criteria);
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
  
  @Value.Immutable  
  interface BlobCriteria {
    CriteriaType getType();
    String getKey();
    String getValue();
  }
  
  enum CriteriaType {
    EXACT, LIKE
  }
}
