package io.resys.thena.docdb.models.doc;

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

import io.resys.thena.docdb.api.models.Message;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaDocObject.Doc;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranchLock;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommit;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocLog;
import io.resys.thena.docdb.models.git.GitDbInserts.BatchStatus;
import io.smallrye.mutiny.Uni;

public interface DocDbInserts {
  
  @Value.Immutable
  interface DocDbBatchForOne {
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
  
  Uni<DocDbBatchForOne> batchOne(DocDbBatchForOne output);
  Uni<DocDbBatchForMany> batchMany(DocDbBatchForMany output);
  
  @Value.Immutable
  interface DocDbBatchForMany {
    BatchStatus getStatus();
    Repo getRepo();
    
    List<DocDbBatchForOne> getItems();

    Message getLog();
    List<Message> getMessages();
  }
  
  /*
  Uni<InsertResult> tag(Tag tag);
  Uni<UpsertResult> blob(Blob blob);
  Uni<UpsertResult> ref(Branch ref, Commit commit);
  Uni<UpsertResult> tree(Tree tree);
  Uni<UpsertResult> commit(Commit commit);
  Uni<Batch> batch(Batch output);
  
  enum UpsertStatus { OK, DUPLICATE, ERROR, CONFLICT }
  enum BatchStatus { OK, EMPTY, ERROR, CONFLICT }
  
  @Value.Immutable
  interface Batch {
    BatchStatus getStatus();
    Repo getRepo();
    Message getLog();
    BatchRef getRef();
    Commit getCommit();
    Tree getTree();
    Integer getDeleted();
    Collection<Blob> getBlobs();
    List<Message> getMessages();
  }
  
  @Value.Immutable
  interface BatchRef {
    Boolean getCreated(); 
    Branch getRef();
  }
  
  
  @Value.Immutable
  interface InsertResult {
    boolean getDuplicate();
  } 
  
  @Value.Immutable
  interface UpsertResult {
    String getId();
    boolean isModified();
    Message getMessage();
    Object getTarget();
    UpsertStatus getStatus();
  }
  */
}
