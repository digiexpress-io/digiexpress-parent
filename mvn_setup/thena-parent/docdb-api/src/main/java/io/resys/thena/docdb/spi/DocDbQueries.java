package io.resys.thena.docdb.spi;

import java.util.Optional;

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
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommit;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommitLock;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocLog;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface DocDbQueries {
  
  DocQuery docs();
  DocBranchQuery branches();
  DocCommitQuery commits();
  DocLogQuery logs();  
  
  interface DocQuery {
    Multi<Doc> findAll();
    Uni<Doc> getById(String id);
  }
  
  interface DocCommitQuery {
    Uni<DocCommit> getById(String commitId);
    Uni<DocCommitLock> getLock(DocLockCriteria criteria);
    Multi<DocCommit> findAll();
  }
  
  interface DocBranchQuery {
    Multi<DocBranch> findAll();
    Uni<DocBranch> getById(String branchId);
  }

  interface DocLogQuery {
    Multi<DocLog> findAll();
    Uni<DocLog> getById(String logId);
  }

  @Value.Immutable
  interface DocLockCriteria {
    String getBranchId();
  }
}
