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

import io.resys.thena.docdb.api.models.Repo;

@Value.Immutable
public abstract class DbCollections {
  public interface WithOptions<T extends WithOptions<T>> {
    T withOptions(DbCollections options);
  }
  
  
  public abstract String getDb();
  public abstract String getRepos();
  public abstract String getRefs();
  public abstract String getTags();
  public abstract String getBlobs();
  public abstract String getTrees();
  public abstract String getTreeItems();
  public abstract String getCommits();
  
  
  public abstract String getDocCommits();
  public abstract String getDocBranch();
  public abstract String getDocLog();
  public abstract String getDoc();
  
  
  public abstract String getOrgRoles();
  public abstract String getOrgGroups();
  public abstract String getOrgGroupRoles();
  public abstract String getOrgUsers();
  public abstract String getOrgUserRoles();
  public abstract String getOrgUserMemberships();
  public abstract String getOrgActorStatus();
  public abstract String getOrgCommits();
  public abstract String getOrgCommitTrees();
  public abstract String getOrgActorData();
  
  
  
  
  
  public DbCollections toRepo(Repo repo) {
    String prefix = repo.getPrefix();
    return ImmutableDbCollections.builder()
        .db(this.getDb())
        .repos(this.getRepos())
        .refs(      prefix + this.getRefs())
        .tags(      prefix + this.getTags())
        .blobs(     prefix + this.getBlobs())
        .trees(     prefix + this.getTrees())
        .treeItems( prefix + this.getTreeItems())
        .commits(   prefix + this.getCommits())
        
        .docCommits(prefix + this.getDocCommits())
        .docBranch( prefix + this.getDocBranch())
        .docLog(    prefix + this.getDocLog())
        .doc(       prefix + this.getDoc())
        
        .orgRoles(          prefix + this.getOrgRoles())
        .orgGroups(         prefix + this.getOrgGroups())
        .orgGroupRoles(     prefix + this.getOrgGroupRoles())
        .orgUsers(          prefix + this.getOrgUsers())
        .orgUserRoles(      prefix + this.getOrgUserRoles())
        .orgUserMemberships(prefix + this.getOrgUserMemberships())
        .orgActorStatus(    prefix + this.getOrgActorStatus())
        .orgCommits(        prefix + this.getOrgCommits())
        .orgCommitTrees(    prefix + this.getOrgCommitTrees())
        .orgActorData(      prefix + this.getOrgActorData())
        
        .build();
  }
  
  public static DbCollections defaults(String db) {
    return ImmutableDbCollections.builder()
        .db(db == null ? "docdb" : db)
        .repos("repos")
        
        .refs("refs")
        .tags("tags")
        .blobs("blobs")
        .trees("trees")
        .treeItems("treeItems")
        .commits("commits")
        
        .docCommits("doc_commits")
        .docBranch("doc_branch")
        .docLog("doc_log")
        .doc("doc")
        
        .orgRoles("org_roles")
        .orgGroups("org_groups")
        .orgGroupRoles("org_group_roles")
        .orgUsers("org_users")
        .orgUserRoles("org_user_roles")
        .orgUserMemberships("org_user_memberships")
        .orgCommits("org_commits")
        .orgCommitTrees("org_commit_trees")
        .orgActorStatus("org_actor_status")
        .orgActorData("org_actor_data")
        
        .build();
  }
}
