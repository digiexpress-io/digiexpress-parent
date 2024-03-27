package io.resys.thena.spi;

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

@Value.Immutable
public abstract class DbCollections {
  private static final DbCollections DEFAULTS = defaults("");
  
  public interface WithOptions<T extends WithOptions<T>> {
    T withOptions(DbCollections options);
  }
  
  public abstract String getDb();
  public abstract String getTenant();
  
  // git structures
  public abstract String getRefs();
  public abstract String getTags();
  public abstract String getBlobs();
  public abstract String getTrees();
  public abstract String getTreeItems();
  public abstract String getCommits();
  
  // doc structures
  public abstract String getDocCommits();
  public abstract String getDocBranch();
  public abstract String getDocLog();
  public abstract String getDoc();
  
  // org structures
  public abstract String getOrgRights();
  public abstract String getOrgParties();
  public abstract String getOrgPartyRights();
  public abstract String getOrgMembers();
  public abstract String getOrgMemberRights();
  public abstract String getOrgMemberships();
  public abstract String getOrgActorStatus();
  public abstract String getOrgCommits();
  public abstract String getOrgCommitTrees();
  public abstract String getOrgActorData();
  
  public DbCollections toRepo(Tenant repo) {
    final String prefix = repo.getPrefix();
    
    return ImmutableDbCollections.builder()
        .db(this.getDb())
        .tenant(this.getTenant())
        
        .refs(      prefix + DEFAULTS.getRefs())
        .tags(      prefix + DEFAULTS.getTags())
        .blobs(     prefix + DEFAULTS.getBlobs())
        .trees(     prefix + DEFAULTS.getTrees())
        .treeItems( prefix + DEFAULTS.getTreeItems())
        .commits(   prefix + DEFAULTS.getCommits())
        
        .docCommits(prefix + DEFAULTS.getDocCommits())
        .docBranch( prefix + DEFAULTS.getDocBranch())
        .docLog(    prefix + DEFAULTS.getDocLog())
        .doc(       prefix + DEFAULTS.getDoc())
        
        .orgRights(         prefix + DEFAULTS.getOrgRights())
        .orgParties(        prefix + DEFAULTS.getOrgParties())
        .orgPartyRights(    prefix + DEFAULTS.getOrgPartyRights())
        .orgMembers(        prefix + DEFAULTS.getOrgMembers())
        .orgMemberRights(   prefix + DEFAULTS.getOrgMemberRights())
        .orgMemberships(    prefix + DEFAULTS.getOrgMemberships())
        .orgActorStatus(    prefix + DEFAULTS.getOrgActorStatus())
        .orgCommits(        prefix + DEFAULTS.getOrgCommits())
        .orgCommitTrees(    prefix + DEFAULTS.getOrgCommitTrees())
        .orgActorData(      prefix + DEFAULTS.getOrgActorData())
        
        .build();
  }
  
  public static DbCollections defaults(String db) {
    return ImmutableDbCollections.builder()
        .db(db == null ? "docdb" : db)
        .tenant("repos")
        
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
        
        .orgRights("org_rights")
        .orgParties("org_parties")
        .orgPartyRights("org_party_rights")
        .orgMembers("org_members")
        .orgMemberRights("org_member_rights")
        .orgMemberships("org_memberships")
        
        .orgCommits("org_commits")
        .orgCommitTrees("org_commit_trees")
        .orgActorStatus("org_actor_status")
        .orgActorData("org_actor_data")
        
        .build();
  }
}
