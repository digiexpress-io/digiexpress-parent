package io.resys.thena.docdb.spi;

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

import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.Ref;
import io.resys.thena.docdb.api.models.Objects.Tag;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface ClientQuery {
  TagQuery tags();
  CommitQuery commits();
  RefQuery refs();
  TreeQuery trees();
  BlobQuery blobs();
  
  interface RefQuery {
    Uni<Ref> name(String name);
    Uni<Ref> nameOrCommit(String refNameOrCommit);
    Uni<Ref> get();
    Multi<Ref> find();
  }
  
  interface BlobQuery {
    Uni<Blob> id(String blobId);
    Uni<List<Blob>> id(List<String> blobId);
    
    Multi<Blob> find();
    Multi<Blob> find(Tree tree);
  }
  interface CommitQuery {
    Uni<Commit> id(String commitId);
    Multi<Commit> find();
  }
  
  interface TreeQuery {
    Uni<Tree> id(String treeId);
    Multi<Tree> find();
  }  
  
  interface TagQuery {
    TagQuery name(String name);
    Uni<DeleteResult> delete();
    Uni<Tag> get();
    Multi<Tag> find();
  }
  
  @Value.Immutable
  interface DeleteResult {
    long getDeletedCount();
  }
}
