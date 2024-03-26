package io.resys.thena.storesql;

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

import io.resys.thena.api.actions.PullActions.MatchCriteria;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.doc.ThenaDocObject.Doc;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocBranch;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocCommit;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocLog;
import io.resys.thena.api.entities.git.Blob;
import io.resys.thena.api.entities.git.Branch;
import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.entities.git.Tag;
import io.resys.thena.api.entities.git.Tree;
import io.resys.thena.api.entities.git.TreeValue;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgCommit;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgCommitTree;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMember;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMemberRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMembership;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgParty;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgRight;
import io.resys.thena.spi.DbCollections;
import io.resys.thena.structures.doc.DocQueries.DocBranchLockCriteria;
import io.resys.thena.structures.doc.DocQueries.DocLockCriteria;
import io.resys.thena.structures.doc.DocQueries.FlattedCriteria;
import io.resys.thena.structures.git.GitQueries.LockCriteria;
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
  
  OrgUserSqlBuilder orgMembers();
  OrgGroupSqlBuilder orgParties();
  OrgUserMembershipsSqlBuilder orgMemberships();
  OrgCommitSqlBuilder orgCommits();
  OrgCommitTreeSqlBuilder orgCommitTrees();
  
  OrgActorStatusSqlBuilder orgActorStatus();
  
  OrgRoleSqlBuilder orgRights();
  OrgUserRoleSqlBuilder orgMemberRights();
  OrgGroupRoleSqlBuilder orgPartyRights();
  
  SqlBuilder withOptions(DbCollections options);

  interface RepoSqlBuilder {
    SqlTuple exists();
    Sql findAll();
    SqlTuple getByName(String name);
    SqlTuple getByNameOrId(String name);
    SqlTuple insertOne(Tenant repo);
    SqlTuple deleteOne(Tenant repo);
  }

  interface OrgUserSqlBuilder {
    SqlTuple getById(String id); //username or id or external_id
    SqlTuple findAllUserGroupsAndRolesByUserId(String userId);
    SqlTuple findAllRolesByUserId(String userId);
    SqlTuple getStatusByUserId(String userId);
    
    Sql findAll();
    SqlTuple findAll(Collection<String> id);
    SqlTuple insertOne(OrgMember user);
    SqlTupleList insertAll(Collection<OrgMember> users);
    SqlTuple updateOne(OrgMember user);
    SqlTupleList updateMany(Collection<OrgMember> users);
  }
  
  interface OrgActorStatusSqlBuilder {
    SqlTuple getById(String id);
    SqlTuple findAllByIdRightId(String rightId);
    SqlTuple findAllByIdMemberId(String memberId);
    
    Sql findAll();
    SqlTuple findAll(List<String> id);
    SqlTuple insertOne(OrgActorStatus user);
    SqlTupleList insertAll(Collection<OrgActorStatus> users);
    SqlTuple updateOne(OrgActorStatus user);
    SqlTupleList updateMany(Collection<OrgActorStatus> users);

  }
  
  interface OrgRoleSqlBuilder {
    SqlTuple getById(String id); //role name or id or external_id
    Sql findAll();
    SqlTuple findAll(Collection<String> id);
    SqlTuple insertOne(OrgRight role);
    SqlTupleList insertAll(Collection<OrgRight> roles);
    SqlTuple updateOne(OrgRight role);
    SqlTupleList updateMany(Collection<OrgRight> roles);
  }
  
  interface OrgUserRoleSqlBuilder {
    SqlTuple getById(String id); 
    SqlTuple findAllByUserId(String userId);
    SqlTuple findAllByRoleId(String userId);
    Sql findAll();
    SqlTuple findAll(List<String> id);
    SqlTuple insertOne(OrgMemberRight role);
    SqlTupleList insertAll(Collection<OrgMemberRight> roles);
  }
  
  interface OrgGroupRoleSqlBuilder {
    SqlTuple getById(String id); 
    SqlTuple findAllByGroupId(String groupId); 
    SqlTuple findAllByRoleId(String groupId);
    SqlTuple findAll(List<String> id);
    Sql findAll();
    
    SqlTuple insertOne(OrgPartyRight role);
    SqlTupleList insertAll(Collection<OrgPartyRight> roles);
  }
  
  
  
  interface OrgGroupSqlBuilder {
    SqlTuple getById(String id); //group name or id or external_id
    Sql findAll();
    SqlTuple findAll(Collection<String> id);
    SqlTuple insertOne(OrgParty group);
    SqlTupleList insertAll(Collection<OrgParty> OrgGroup);
    SqlTuple updateOne(OrgParty group);
    SqlTupleList updateMany(Collection<OrgParty> groups);
  }

  interface OrgUserMembershipsSqlBuilder {
    Sql findAll();
    SqlTuple findAll(List<String> id);
    SqlTuple getById(String id); 
    SqlTuple findAllByGroupId(String groupId);
    SqlTuple findAllByUserId(String userId);
    SqlTuple insertOne(OrgMembership membership);
    SqlTupleList insertAll(Collection<OrgMembership> memberships);
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
    
    default String getPropsDeepString() {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      final int size = getProps().size();
      for (int i = 0; i < size; i++) {
        final var value = getProps().getValue(i);
        if(value instanceof String[]) {
          final var unwrapped = (String[]) value;
          sb.append("[")
          .append(String.join(",", unwrapped))
          .append("]");   
        } else {
          sb.append(value);
        }

        if (i + 1 < size)
          sb.append(",");
      }
      sb.append("]");
      return sb.toString();
    }
  }
  @Value.Immutable
  interface SqlTupleList {
    String getValue();
    List<Tuple> getProps();
  }
}
