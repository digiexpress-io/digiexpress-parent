package io.resys.thena.docdb.sql;

import java.util.Collection;

/*-
 * #%L
 * thena-docdb-pgsql
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

import java.util.List;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.actions.ObjectsActions.MatchCriteria;
import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.Ref;
import io.resys.thena.docdb.api.models.Objects.Tag;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.api.models.Objects.TreeValue;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientCollections;
import io.vertx.mutiny.sqlclient.Tuple;

public interface SqlBuilder {

  RepoSqlBuilder repo();
  RefSqlBuilder refs();
  TagSqlBuilder tags();
  BlobSqlBuilder blobs();
  CommitSqlBuilder commits();
  TreeSqlBuilder trees();
  TreeItemSqlBuilder treeItems();
  SqlBuilder withOptions(ClientCollections options);

  interface RepoSqlBuilder {
    SqlTuple exists();
    Sql create();
    Sql findAll();
    SqlTuple getByName(String name);
    SqlTuple getByNameOrId(String name);
    SqlTuple insertOne(Repo repo);
  }
  
  interface BlobSqlBuilder {
    SqlTuple getById(String blobId);
    SqlTuple findByIds(String treeId, Collection<String> blobId, List<MatchCriteria> criteria);
    SqlTuple findByTree(Tree tree);
    SqlTuple insertOne(Blob blob);
    SqlTupleList insertAll(Collection<Blob> blobs);
    Sql findAll();
  }
  
  interface RefSqlBuilder {
    SqlTuple getByName(String name);
    SqlTuple getByNameOrCommit(String refNameOrCommit);
    Sql getFirst();
    Sql findAll();
    SqlTuple insertOne(Ref ref);
    SqlTuple updateOne(Ref ref, Commit commit);
  }
  
  interface TagSqlBuilder {
    SqlTuple getByName(String name);
    SqlTuple deleteByName(String name);
    Sql findAll();
    Sql getFirst();
    SqlTuple insertOne(Tag tag);
  }
  
  interface TreeSqlBuilder {
    SqlTuple getById(String id);
    Sql findAll();
    SqlTuple insertOne(Tree tree);
  }
  
  
  interface CommitSqlBuilder {
    SqlTuple getById(String id);
    Sql findAll();
    SqlTuple insertOne(Commit commit);
  }
  
  interface TreeItemSqlBuilder {
    SqlTuple getByTreeId(String treeId);
    Sql findAll();
    SqlTuple insertOne(Tree tree, TreeValue item);
    SqlTupleList insertAll(Tree item);
  }
  
  @Value.Immutable
  interface Sql {
    String getValue();
  }
  @Value.Immutable
  interface SqlTuple {
    String getValue();
    Tuple getProps();
  }
  @Value.Immutable
  interface SqlTupleList {
    String getValue();
    List<Tuple> getProps();
  }
}
