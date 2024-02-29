package io.resys.thena.docdb.store.sql;

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
import io.resys.thena.docdb.api.models.ThenaDocObject.Doc;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommit;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocLog;
import io.resys.thena.docdb.api.models.ThenaGitObject.Blob;
import io.resys.thena.docdb.api.models.ThenaGitObject.Branch;
import io.resys.thena.docdb.api.models.ThenaGitObject.Commit;
import io.resys.thena.docdb.api.models.ThenaGitObject.Tag;
import io.resys.thena.docdb.api.models.ThenaGitObject.Tree;
import io.resys.thena.docdb.api.models.ThenaGitObject.TreeValue;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgCommit;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgCommitTree;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroupRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserRole;
import io.resys.thena.docdb.models.doc.DocQueries.DocBranchLockCriteria;
import io.resys.thena.docdb.models.doc.DocQueries.DocLockCriteria;
import io.resys.thena.docdb.models.doc.DocQueries.FlattedCriteria;
import io.resys.thena.docdb.models.git.GitQueries.LockCriteria;
import io.resys.thena.docdb.spi.DbCollections;
import io.vertx.mutiny.sqlclient.Tuple;

public interface SqlBuilder extends DbCollections.WithOptions<SqlBuilder> {

  RepoSqlBuilder repo();
  
  DocSqlBuilder docs();
  DocLogSqlBuilder docLogs();
  DocCommitSqlBuilder docCommits();
  DocBranchSqlBuilder docBranches();
  
  GitRefSqlBuilder refs();
  GitTagSqlBuilder tags();
  GitBlobSqlBuilder blobs();
  GitCommitSqlBuilder commits();
  GitTreeSqlBuilder trees();
  GitTreeItemSqlBuilder treeItems();
  
  OrgUserSqlBuilder orgUsers();
  OrgGroupSqlBuilder orgGroups();
  OrgUserMembershipsSqlBuilder orgUserMemberships();
  OrgCommitSqlBuilder orgCommits();
  OrgCommitTreeSqlBuilder orgCommitTrees();
  
  OrgRoleSqlBuilder orgRoles();
  OrgUserRoleSqlBuilder orgUserRoles();
  OrgGroupRoleSqlBuilder orgGroupRoles();
  
  
  SqlBuilder withOptions(DbCollections options);

  interface RepoSqlBuilder {
    SqlTuple exists();
    Sql findAll();
    SqlTuple getByName(String name);
    SqlTuple getByNameOrId(String name);
    SqlTuple insertOne(Repo repo);
    SqlTuple deleteOne(Repo repo);
  }

  interface OrgUserSqlBuilder {
    SqlTuple getById(String id); //username or id or external_id
    Sql findAll();
    SqlTuple findAll(List<String> id);
    SqlTuple insertOne(OrgUser user);
    SqlTupleList insertAll(Collection<OrgUser> users);
    SqlTuple updateOne(OrgUser user);
    SqlTupleList updateMany(Collection<OrgUser> users);
  }
  
  interface OrgRoleSqlBuilder {
    SqlTuple getById(String id); //role name or id or external_id
    Sql findAll();
    SqlTuple findAll(List<String> id);
    SqlTuple insertOne(OrgRole role);
    SqlTupleList insertAll(Collection<OrgRole> roles);
    SqlTuple updateOne(OrgRole role);
    SqlTupleList updateMany(Collection<OrgRole> roles);
  }
  
  interface OrgUserRoleSqlBuilder {
    SqlTuple getById(String id); 
    SqlTuple findAllByUserId(String userId);
    SqlTuple findAllByRoleId(String userId);
    Sql findAll();
    SqlTuple findAll(List<String> id);
    SqlTuple insertOne(OrgUserRole role);
    SqlTupleList insertAll(Collection<OrgUserRole> roles);
  }
  
  interface OrgGroupRoleSqlBuilder {
    SqlTuple getById(String id); 
    SqlTuple findAllByGroupId(String groupId); 
    SqlTuple findAllByRoleId(String groupId);
    SqlTuple findAll(List<String> id);
    Sql findAll();
    
    SqlTuple insertOne(OrgGroupRole role);
    SqlTupleList insertAll(Collection<OrgGroupRole> roles);
  }
  
  
  
  interface OrgGroupSqlBuilder {
    SqlTuple getById(String id); //group name or id or external_id
    Sql findAll();
    SqlTuple findAll(List<String> id);
    SqlTuple insertOne(OrgGroup group);
    SqlTupleList insertAll(Collection<OrgGroup> OrgGroup);
    SqlTuple updateOne(OrgGroup group);
    SqlTupleList updateMany(Collection<OrgGroup> groups);
  }

  interface OrgUserMembershipsSqlBuilder {
    Sql findAll();
    SqlTuple findAll(List<String> id);
    SqlTuple getById(String id); 
    SqlTuple findAllByGroupId(String groupId);
    SqlTuple findAllByUserId(String userId);
    SqlTuple insertOne(OrgUserMembership membership);
    SqlTupleList insertAll(Collection<OrgUserMembership> memberships);
  }

  interface OrgCommitSqlBuilder {
    SqlTuple getById(String id);
    Sql findAll();
    SqlTuple insertOne(OrgCommit commit);
    SqlTupleList insertAll(Collection<OrgCommit> commit);
  }
  interface OrgCommitTreeSqlBuilder {
    SqlTuple getById(String id);
    SqlTuple findByCommmitId(String commitId);
    Sql findAll();
    SqlTupleList insertAll(Collection<OrgCommitTree> tree);
  }
  
  interface DocCommitSqlBuilder {
    SqlTuple getById(String id);
    Sql findAll();
    SqlTuple insertOne(DocCommit commit);
    SqlTupleList insertAll(Collection<DocCommit> commits);
  }
  
  interface DocSqlBuilder {
    SqlTuple findAllFlatted(FlattedCriteria criteria);
    Sql findAllFlatted();
    SqlTuple findById(String id); // matches by external_id or id or parent_id
    SqlTuple getById(String id);  // matches by external_id or id
    SqlTuple deleteById(String id);
    Sql findAll();
    SqlTuple insertOne(Doc doc);
    SqlTuple updateOne(Doc doc);
    
    SqlTupleList insertMany(List<Doc> docs);
    SqlTupleList updateMany(List<Doc> docs);
  }
  
  interface DocLogSqlBuilder {
    SqlTuple getById(String id);
    SqlTuple findByBranchId(String branchId);
    Sql findAll();
    SqlTuple insertOne(DocLog doc);
    SqlTupleList insertAll(Collection<DocLog> logs);
  }
  
  
  
  interface DocBranchSqlBuilder {
    SqlTuple getById(String branchId);
    SqlTuple updateOne(DocBranch doc);
    SqlTuple insertOne(DocBranch doc);
    SqlTupleList insertAll(Collection<DocBranch> docs);
    SqlTupleList updateAll(List<DocBranch> doc);
    SqlTuple getBranchLock(DocBranchLockCriteria crit);
    SqlTuple getBranchLocks(List<DocBranchLockCriteria> crit);
    SqlTuple getDocLock(DocLockCriteria crit);
    SqlTuple getDocLocks(List<DocLockCriteria> crit);
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
