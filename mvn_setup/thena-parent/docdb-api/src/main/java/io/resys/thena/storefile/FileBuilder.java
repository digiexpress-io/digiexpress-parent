package io.resys.thena.storefile;

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

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.git.Blob;
import io.resys.thena.api.entities.git.Branch;
import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.entities.git.Tag;
import io.resys.thena.api.entities.git.Tree;
import io.resys.thena.api.entities.git.TreeValue;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.storefile.tables.Table.FileStatement;
import io.resys.thena.storefile.tables.Table.FileTuple;
import io.resys.thena.storefile.tables.Table.FileTupleList;

public interface FileBuilder extends TenantTableNames.WithTenant<FileBuilder>{

  RepoFileBuilder repo();
  RefFileBuilder refs();
  TagFileBuilder tags();
  BlobFileBuilder blobs();
  CommitFileBuilder commits();
  TreeFileBuilder trees();
  TreeItemFileBuilder treeItems();
  FileBuilder withTenant(TenantTableNames options);

  interface RepoFileBuilder {
    FileTuple exists();
    FileStatement create();
    FileStatement findAll();
    FileTuple getByName(String name);
    FileTuple getByNameOrId(String name);
    FileTuple insertOne(Tenant repo);
  }
  
  interface BlobFileBuilder {
    FileStatement create();
    FileTuple getById(String blobId);
    FileTuple findByIds(Collection<String> blobId);
    FileTuple findByTreeId(String treeId);
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
    FileTuple insertOne(Branch ref);
    FileTuple updateOne(Branch ref, Commit commit);
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
