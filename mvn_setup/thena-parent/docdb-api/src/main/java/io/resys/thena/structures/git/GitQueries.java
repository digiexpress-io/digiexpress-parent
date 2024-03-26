package io.resys.thena.structures.git;

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

import io.resys.thena.api.actions.PullActions.MatchCriteria;
import io.resys.thena.api.entities.git.Blob;
import io.resys.thena.api.entities.git.BlobHistory;
import io.resys.thena.api.entities.git.Branch;
import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.entities.git.CommitLock;
import io.resys.thena.api.entities.git.Tag;
import io.resys.thena.api.entities.git.Tree;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface GitQueries {
  GitTagQuery tags();
  GitCommitQuery commits();
  GitRefQuery refs();
  GitTreeQuery trees();
  GitBlobQuery blobs();
  GitBlobHistoryQuery blobHistory();
  
  
  interface GitRefQuery {
    Uni<Branch> name(String name);
    Uni<Branch> nameOrCommit(String refNameOrCommit);
    Uni<Branch> get();
    Multi<Branch> findAll();
  }
  
  interface GitBlobHistoryQuery {
    GitBlobHistoryQuery latestOnly(boolean latestOnly);
    GitBlobHistoryQuery blobName(String name);
    GitBlobHistoryQuery criteria(MatchCriteria ... criteria);
    GitBlobHistoryQuery criteria(List<MatchCriteria> criteria);
    Multi<BlobHistory> find();
  }
  
  interface GitBlobQuery {
    Uni<Blob> getById(String blobId);
    
    Multi<Blob> findAll();
    Multi<Blob> findAll(String treeId, List<String> docIds, List<MatchCriteria> matchBy);
    Multi<Blob> findAll(String treeId, List<MatchCriteria> criteria);
  }
  interface GitCommitQuery {
    Uni<Commit> getById(String commitId);
    Uni<CommitLock> getLock(LockCriteria criteria);
    Multi<Commit> findAll();
  }
  interface GitTreeQuery {
    Uni<Tree> getById(String treeId);
    Multi<Tree> findAll();
  }
  interface GitTagQuery {
    GitTagQuery name(String name);
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
