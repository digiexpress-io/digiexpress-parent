package io.resys.thena.docdb.models.doc;

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

import io.resys.thena.docdb.api.models.ThenaDocObject.Doc;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranchLock;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommit;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocFlatted;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocLock;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocLog;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface DocQueries {
  
  DocQuery docs();
  DocBranchQuery branches();
  DocCommitQuery commits();
  DocLogQuery logs();  
  
  interface DocQuery {
    Multi<DocFlatted> findAllFlatted(FlattedCriteria criteria);
    Multi<DocFlatted> findAllFlatted();
    Multi<Doc> findAll();
    Uni<Doc> getById(String id);
  }
  
  interface DocCommitQuery {
    Uni<DocCommit> getById(String commitId);
    Multi<DocCommit> findAll();
  }
  
  interface DocBranchQuery {
    Multi<DocBranch> findAll();
    
    Uni<DocBranchLock> getBranchLock(DocBranchLockCriteria criteria);
    Uni<List<DocBranchLock>> getBranchLocks(List<DocBranchLockCriteria> criteria);
    
    Uni<DocLock> getDocLock(DocLockCriteria criteria);
    Uni<List<DocLock>> getDocLocks(List<DocLockCriteria> criteria);
    
    Uni<DocBranch> getById(String branchId);
  }

  interface DocLogQuery {
    Multi<DocLog> findAll();
    Uni<DocLog> getById(String logId);
  }

  @Value.Immutable
  interface FlattedCriteria {
    List<String> getMatchId();
    @Nullable String getBranchName();
    @Nullable String getDocType();
    
    boolean getChildren();
    boolean getOnlyActiveDocs();
  }

  @Value.Immutable
  interface DocBranchLockCriteria {
    String getBranchName();
    String getDocId();
  }
  @Value.Immutable
  interface DocLockCriteria {
    String getDocId();
  }
  
  
}
