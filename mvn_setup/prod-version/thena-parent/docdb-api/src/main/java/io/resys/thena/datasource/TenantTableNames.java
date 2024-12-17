package io.resys.thena.datasource;

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
public abstract class TenantTableNames {
  private static final TenantTableNames DEFAULTS = defaults("");
  
  public interface WithTenant<T extends WithTenant<T>> {
    T withTenant(TenantTableNames options);
  }
  
  public abstract String getDb();
  public abstract String getTenant();
  public abstract String getPrefix();
  
  public abstract String getGrimAssignment();
  public abstract String getGrimCommit();
  public abstract String getGrimCommands();
  public abstract String getGrimCommitTree();
  public abstract String getGrimCommitViewer();
  public abstract String getGrimMission();
  public abstract String getGrimMissionRef();
  public abstract String getGrimMissionData();
  public abstract String getGrimMissionLabel();
  public abstract String getGrimMissionLink();
  public abstract String getGrimObjective();
  public abstract String getGrimObjectiveGoal();
  public abstract String getGrimRemark();
  
  // git structures
  public abstract String getRefs();
  public abstract String getTags();
  public abstract String getBlobs();
  public abstract String getTrees();
  public abstract String getTreeItems();
  public abstract String getCommits();
  
  // doc structures
  public abstract String getDocCommands();
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
  public abstract String getOrgCommits();
  public abstract String getOrgCommitTrees();

  
  public TenantTableNames toRepo(Tenant repo) {
    final String prefix = repo.getPrefix();
    
    return ImmutableTenantTableNames.builder()
        .db(this.getDb())
        .tenant(this.getTenant())
        .prefix(prefix)
        
        .refs(      prefix + DEFAULTS.getRefs())
        .tags(      prefix + DEFAULTS.getTags())
        .blobs(     prefix + DEFAULTS.getBlobs())
        .trees(     prefix + DEFAULTS.getTrees())
        .treeItems( prefix + DEFAULTS.getTreeItems())
        .commits(   prefix + DEFAULTS.getCommits())
        
        .docCommands(prefix + DEFAULTS.getDocCommands())
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
        .orgCommits(        prefix + DEFAULTS.getOrgCommits())
        .orgCommitTrees(    prefix + DEFAULTS.getOrgCommitTrees())

        
        .grimAssignment(    prefix + DEFAULTS.getGrimAssignment())
        .grimCommit(        prefix + DEFAULTS.getGrimCommit())
        .grimCommands(      prefix + DEFAULTS.getGrimCommands())
        .grimCommitTree(    prefix + DEFAULTS.getGrimCommitTree())
        .grimCommitViewer(  prefix + DEFAULTS.getGrimCommitViewer())
        .grimMission(       prefix + DEFAULTS.getGrimMission())
        .grimMissionRef(    prefix + DEFAULTS.getGrimMissionRef())
        .grimMissionData(   prefix + DEFAULTS.getGrimMissionData())
        .grimMissionLabel(  prefix + DEFAULTS.getGrimMissionLabel())
        .grimMissionLink(   prefix + DEFAULTS.getGrimMissionLink())
        .grimObjective(     prefix + DEFAULTS.getGrimObjective())
        .grimObjectiveGoal( prefix + DEFAULTS.getGrimObjectiveGoal())
        .grimRemark(        prefix + DEFAULTS.getGrimRemark())
        
        .build();
  }
  
  public static TenantTableNames defaults(String db) {
    return ImmutableTenantTableNames.builder()
        .db(db == null ? "docdb" : db)
        .tenant("tenants")
        .prefix("")
        
        .refs("git_refs")
        .tags("git_tags")
        .blobs("git_blobs")
        .trees("git_trees")
        .treeItems("git_treeItems")
        .commits("git_commits")
        
        .docCommands("doc_commands")
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
        
        .grimAssignment("grim_assignment")
        .grimCommit("grim_commit")
        .grimCommitTree("grim_commit_tree")
        .grimCommitViewer("grim_commit_viewer")
        .grimMission("grim_mission")
        .grimCommands("grim_commands")
        .grimMissionData("grim_mission_data")
        .grimMissionLabel("grim_mission_label")
        .grimMissionLink("grim_mission_link")
        .grimObjective("grim_objective")
        .grimObjectiveGoal("grim_objective_goal")
        .grimRemark("grim_remark")
        .grimMissionRef("grim_mission_ref")
        
        .build();
  }
}
