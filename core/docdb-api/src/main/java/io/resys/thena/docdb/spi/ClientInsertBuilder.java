package io.resys.thena.docdb.spi;

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
import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.Ref;
import io.resys.thena.docdb.api.models.Objects.Tag;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.spi.commits.body.CommitInternalResponse;
import io.smallrye.mutiny.Uni;

public interface ClientInsertBuilder {
  
  Uni<InsertResult> tag(Tag tag);
  Uni<UpsertResult> blob(Blob blob);
  Uni<UpsertResult> ref(Ref ref, Commit commit);
  Uni<UpsertResult> tree(Tree tree);
  Uni<UpsertResult> commit(Commit commit);
  Uni<CommitInternalResponse> output(CommitInternalResponse output);
  
  enum UpsertStatus { OK, DUPLICATE, ERROR, CONFLICT }
  
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
