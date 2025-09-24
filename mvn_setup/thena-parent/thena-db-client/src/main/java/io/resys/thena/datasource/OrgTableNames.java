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
public abstract class OrgTableNames {
  private static final OrgTableNames DEFAULTS = defaults();
  public abstract String getPrefix();

  
  // org structures
  public abstract String getOrgRights();
  public abstract String getOrgParties();
  public abstract String getOrgPartyRights();
  public abstract String getOrgMembers();
  public abstract String getOrgMemberRights();
  public abstract String getOrgMemberships();
  public abstract String getOrgCommits();
  public abstract String getOrgCommitTrees();

  
  public OrgTableNames toRepo(Tenant repo) {
    final String prefix = repo.getPrefix();
    return toRepo(prefix);
  }
  
  public OrgTableNames toRepo(String prefix) {
    return ImmutableOrgTableNames.builder()
        .prefix(prefix)
 
        .orgRights(         prefix + DEFAULTS.getOrgRights())
        .orgParties(        prefix + DEFAULTS.getOrgParties())
        .orgPartyRights(    prefix + DEFAULTS.getOrgPartyRights())
        .orgMembers(        prefix + DEFAULTS.getOrgMembers())
        .orgMemberRights(   prefix + DEFAULTS.getOrgMemberRights())
        .orgMemberships(    prefix + DEFAULTS.getOrgMemberships())
        .orgCommits(        prefix + DEFAULTS.getOrgCommits())
        .orgCommitTrees(    prefix + DEFAULTS.getOrgCommitTrees())
        
        .build();
  }
  
  public static OrgTableNames defaults() {
    return ImmutableOrgTableNames.builder()
      .prefix("")

      .orgRights("org_rights")
      .orgParties("org_parties")
      .orgPartyRights("org_party_rights")
      .orgMembers("org_members")
      .orgMemberRights("org_member_rights")
      .orgMemberships("org_memberships")
      
      .orgCommits("org_commits")
      .orgCommitTrees("org_commit_trees")
      
      
      .build();
  }
}
