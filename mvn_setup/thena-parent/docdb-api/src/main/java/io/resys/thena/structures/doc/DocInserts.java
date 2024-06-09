package io.resys.thena.structures.doc;

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

import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.entities.doc.DocLock.DocBranchLock;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.structures.BatchStatus;
import io.smallrye.mutiny.Uni;

public interface DocInserts {
  
  @Value.Immutable
  interface DocBatchForOne {
    DocBatchForOneType getType();
    Optional<Doc> getDoc();
    List<DocBranchLock> getDocLock();
    List<DocBranch> getDocBranch();
    List<DocCommit> getDocCommit();
    List<DocCommitTree> getDocCommitTree();
    List<DocCommands> getDocCommands();
    String getLog();
    List<Message> getMessages();
  }
  
  Uni<DocBatchForMany> batchMany(DocBatchForMany output);
  
  enum DocBatchForOneType {
    UPDATE, CREATE, DELETE
  }
  
  @Value.Immutable
  interface DocBatchForMany {
    BatchStatus getStatus();
    String getRepo();
    
    List<DocBatchForOne> getItems();
    String getLog();
    List<Message> getMessages();
  }
}
