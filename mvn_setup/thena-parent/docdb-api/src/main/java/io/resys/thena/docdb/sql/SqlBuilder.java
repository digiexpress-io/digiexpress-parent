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

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.actions.PullActions.MatchCriteria;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaGitObject.Blob;
import io.resys.thena.docdb.api.models.ThenaGitObject.Branch;
import io.resys.thena.docdb.api.models.ThenaGitObject.Commit;
import io.resys.thena.docdb.api.models.ThenaGitObject.Tag;
import io.resys.thena.docdb.api.models.ThenaGitObject.Tree;
import io.resys.thena.docdb.api.models.ThenaGitObject.TreeValue;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.spi.GitDbQueries.LockCriteria;
import io.vertx.mutiny.sqlclient.Tuple;

public interface SqlBuilder extends DbCollections.WithOptions<SqlBuilder> {

  RepoSqlBuilder repo();
  
  GitRefSqlBuilder docs();
  GitRefSqlBuilder docLogs();
  GitRefSqlBuilder docCommits();
  GitRefSqlBuilder docBranches();
  
  GitRefSqlBuilder refs();
  GitTagSqlBuilder tags();
  GitBlobSqlBuilder blobs();
  GitCommitSqlBuilder commits();
  GitTreeSqlBuilder trees();
  GitTreeItemSqlBuilder treeItems();
  SqlBuilder withOptions(DbCollections options);

  
  interface RepoSqlBuilder {
    SqlTuple exists();
    Sql findAll();
    SqlTuple getByName(String name);
    SqlTuple getByNameOrId(String name);
    SqlTuple insertOne(Repo repo);
    SqlTuple deleteOne(Repo repo);
  }
  
  
  interface DocSqlBuilder {
    SqlTuple getById(String blobId);
    
    SqlTuple insertOne(Doc blob);
    SqlTupleList insertAll(Collection<Blob> blobs);
    
    SqlTuple find(@Nullable String name, boolean latestOnly, List<MatchCriteria> criteria);
    SqlTuple findByTree(String treeId, List<MatchCriteria> criteria);
    SqlTuple findByTree(String treeId, List<String> blobNames, List<MatchCriteria> criteria);
    SqlTuple findByIds(Collection<String> blobId);
    Sql findAll();
  }
  
  
  
  interface GitBlobSqlBuilder {
    SqlTuple getById(String blobId);
    
    SqlTuple insertOne(Blob blob);
    SqlTupleList insertAll(Collection<Blob> blobs);
    
    SqlTuple find(@Nullable String name, boolean latestOnly, List<MatchCriteria> criteria);
    SqlTuple findByTree(String treeId, List<MatchCriteria> criteria);
    SqlTuple findByTree(String treeId, List<String> blobNames, List<MatchCriteria> criteria);
    SqlTuple findByIds(Collection<String> blobId);
    Sql findAll();
  }
  
  
  
  interface GitRefSqlBuilder {
    SqlTuple getByName(String name);
    SqlTuple getByNameOrCommit(String refNameOrCommit);
    Sql getFirst();
    Sql findAll();
    SqlTuple insertOne(Branch ref);
    SqlTuple updateOne(Branch ref, Commit commit);
  }
  
  interface GitTagSqlBuilder {
    SqlTuple getByName(String name);
    SqlTuple deleteByName(String name);
    Sql findAll();
    Sql getFirst();
    SqlTuple insertOne(Tag tag);
  }
  
  interface GitTreeSqlBuilder {
    SqlTuple getById(String id);
    Sql findAll();
    SqlTuple insertOne(Tree tree);
  }
  
  
  interface GitCommitSqlBuilder {
    SqlTuple getById(String id);
    SqlTuple getLock(LockCriteria crit);
    Sql findAll();
    SqlTuple insertOne(Commit commit);
  }
  
  interface GitTreeItemSqlBuilder {
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
