package io.resys.thena.structures.doc;

import java.util.List;

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

import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocLock;
import io.resys.thena.api.entities.doc.DocLock.DocBranchLock;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface DocQueries {
  DocBranchQuery branches();
  
  interface DocBranchQuery {
    Multi<DocBranch> findAll();
    
    Uni<DocBranchLock> getBranchLock(DocBranchLockCriteria criteria);
    Uni<List<DocBranchLock>> getBranchLocks(List<DocBranchLockCriteria> criteria);
    
    Uni<DocLock> getDocLock(DocLockCriteria criteria);
    Uni<List<DocLock>> getDocLocks(List<DocLockCriteria> criteria);
    Uni<DocBranch> getById(String branchId);
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
