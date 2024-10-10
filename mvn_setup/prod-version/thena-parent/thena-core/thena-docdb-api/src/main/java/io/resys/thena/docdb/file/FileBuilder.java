package io.resys.thena.docdb.file;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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

import java.util.Collection;

import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.Ref;
import io.resys.thena.docdb.api.models.Objects.Tag;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.api.models.Objects.TreeValue;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.file.tables.Table.FileStatement;
import io.resys.thena.docdb.file.tables.Table.FileTuple;
import io.resys.thena.docdb.file.tables.Table.FileTupleList;
import io.resys.thena.docdb.spi.ClientCollections;

public interface FileBuilder {

  RepoFileBuilder repo();
  RefFileBuilder refs();
  TagFileBuilder tags();
  BlobFileBuilder blobs();
  CommitFileBuilder commits();
  TreeFileBuilder trees();
  TreeItemFileBuilder treeItems();
  FileBuilder withOptions(ClientCollections options);

  interface RepoFileBuilder {
    FileTuple exists();
    FileStatement create();
    FileStatement findAll();
    FileTuple getByName(String name);
    FileTuple getByNameOrId(String name);
    FileTuple insertOne(Repo repo);
  }
  
  interface BlobFileBuilder {
    FileStatement create();
    FileTuple getById(String blobId);
    FileTuple findByIds(Collection<String> blobId);
    FileTuple findByTree(Tree tree);
    FileTuple insertOne(Blob blob);
    FileTupleList insertAll(Collection<Blob> blobs);
    FileStatement findAll();
  }
  
  interface RefFileBuilder {
    FileStatement create();
    FileStatement constraints();
    FileTuple getByName(String name);
    FileTuple getByNameOrCommit(String refNameOrCommit);
    FileStatement getFirst();
    FileStatement findAll();
    FileTuple insertOne(Ref ref);
    FileTuple updateOne(Ref ref, Commit commit);
  }
  
  interface TagFileBuilder {
    FileStatement create();
    FileStatement constraints();
    FileTuple getByName(String name);
    FileTuple deleteByName(String name);
    FileStatement findAll();
    FileStatement getFirst();
    FileTuple insertOne(Tag tag);
  }
  
  interface TreeFileBuilder {
    FileStatement create();
    FileTuple getById(String id);
    FileStatement findAll();
    FileTuple insertOne(Tree tree);
  }
  
  
  interface CommitFileBuilder {
    FileStatement create();
    FileStatement constraints();
    FileTuple getById(String id);
    FileStatement findAll();
    FileTuple insertOne(Commit commit);
  }
  
  interface TreeItemFileBuilder {
    FileStatement create();
    FileStatement constraints();
    FileTuple getByTreeId(String treeId);
    FileStatement findAll();
    FileTuple insertOne(Tree tree, TreeValue item);
    FileTupleList insertAll(Tree item);
  }
  

}
