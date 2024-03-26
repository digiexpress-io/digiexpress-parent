package io.resys.thena.structures.git;

import java.util.Collection;
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

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.git.ThenaGitObject.Blob;
import io.resys.thena.api.entities.git.ThenaGitObject.Branch;
import io.resys.thena.api.entities.git.ThenaGitObject.Commit;
import io.resys.thena.api.entities.git.ThenaGitObject.Tag;
import io.resys.thena.api.entities.git.ThenaGitObject.Tree;
import io.resys.thena.api.envelope.Message;
import io.smallrye.mutiny.Uni;

public interface GitInserts {
  
  Uni<InsertResult> tag(Tag tag);
  Uni<UpsertResult> blob(Blob blob);
  Uni<UpsertResult> ref(Branch ref, Commit commit);
  Uni<UpsertResult> tree(Tree tree);
  Uni<UpsertResult> commit(Commit commit);
  Uni<GitBatch> batch(GitBatch output);
  
  enum UpsertStatus { OK, DUPLICATE, ERROR, CONFLICT }
  enum BatchStatus { OK, EMPTY, ERROR, CONFLICT }
  
  @Value.Immutable
  interface GitBatch {
    BatchStatus getStatus();
    Tenant getRepo();
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
}
