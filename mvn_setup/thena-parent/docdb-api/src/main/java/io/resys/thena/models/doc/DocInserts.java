package io.resys.thena.models.doc;

import java.util.List;
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

import io.resys.thena.api.models.Message;
import io.resys.thena.api.models.Repo;
import io.resys.thena.api.models.ThenaDocObject.Doc;
import io.resys.thena.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.api.models.ThenaDocObject.DocBranchLock;
import io.resys.thena.api.models.ThenaDocObject.DocCommit;
import io.resys.thena.api.models.ThenaDocObject.DocLog;
import io.resys.thena.models.git.GitInserts.BatchStatus;
import io.smallrye.mutiny.Uni;

public interface DocInserts {
  
  @Value.Immutable
  interface DocBatchForOne {
    BatchStatus getStatus();
    String getRepoId();
    
    Optional<Doc> getDoc();
    List<DocBranchLock> getDocLock();
    List<DocBranch> getDocBranch();
    List<DocCommit> getDocCommit();
    List<DocLog> getDocLogs();

    Message getLog();
    List<Message> getMessages();
  }
  
  Uni<DocBatchForOne> batchOne(DocBatchForOne output);
  Uni<DocBatchForMany> batchMany(DocBatchForMany output);
  
  @Value.Immutable
  interface DocBatchForMany {
    BatchStatus getStatus();
    Repo getRepo();
    
    List<DocBatchForOne> getItems();

    Message getLog();
    List<Message> getMessages();
  }
}