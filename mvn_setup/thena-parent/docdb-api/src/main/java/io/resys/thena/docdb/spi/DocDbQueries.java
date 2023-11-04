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
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommit;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommitLock;
import io.resys.thena.docdb.api.models.ThenaGitObject.Blob;
import io.resys.thena.docdb.api.models.ThenaGitObject.Commit;
import io.resys.thena.docdb.api.models.ThenaGitObject.Tag;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface DocDbQueries {
  TagQuery tags();
  DocCommitQuery commits();
  BlobQuery blobs();
  

  interface BlobQuery {
    Uni<Blob> getById(String blobId);
    
    Multi<Blob> findAll();
    Multi<Blob> findAll(String treeId, List<String> docIds, List<MatchCriteria> matchBy);
    Multi<Blob> findAll(String treeId, List<MatchCriteria> criteria);
  }
  interface DocCommitQuery {
    Uni<DocCommit> getById(String commitId);
    Uni<DocCommitLock> getLock(DocLockCriteria criteria);
    Multi<DocCommit> findAll();
  }

  interface TagQuery {
    TagQuery name(String name);
    Uni<DocDeleteResult> delete();
    Uni<Tag> getFirst();
    Multi<Tag> find();
  }
  
  @Value.Immutable
  interface DocLockCriteria {
    @Nullable String getDocId(); 
    @Nullable String getVersionId();
    @Nullable String getBranchName();
  }
  
  @Value.Immutable
  interface DocDeleteResult {
    long getDeletedCount();
  }
}
