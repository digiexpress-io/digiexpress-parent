package io.resys.thena.registry.org;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import io.resys.thena.api.registry.OrgRegistry;
import io.resys.thena.api.registry.org.OrgCommitRegistry;
import io.resys.thena.api.registry.org.OrgCommitTreeRegistry;
import io.resys.thena.api.registry.org.OrgMemberRegistry;
import io.resys.thena.api.registry.org.OrgMemberRightRegistry;
import io.resys.thena.api.registry.org.OrgMembershipRegistry;
import io.resys.thena.api.registry.org.OrgPartyRegistry;
import io.resys.thena.api.registry.org.OrgPartyRightRegistry;
import io.resys.thena.api.registry.org.OrgRightRegistry;
import io.resys.thena.datasource.TenantTableNames;

public class OrgRegistrySqlImpl implements OrgRegistry {
  @SuppressWarnings("unused")
  private final TenantTableNames options;
  private final OrgCommitRegistry orgCommits;
  private final OrgCommitTreeRegistry orgCommitTrees;
  private final OrgMemberRightRegistry orgMemberRights;
  private final OrgMembershipRegistry orgMemberships;
  private final OrgMemberRegistry orgMembers;
  private final OrgPartyRightRegistry orgPartyRights;
  private final OrgPartyRegistry orgParties;
  private final OrgRightRegistry orgRights;
  
  public OrgRegistrySqlImpl(TenantTableNames options) {
    this.options = options;  
    orgCommits = new OrgCommitRegistrySqlImpl(options);
    orgCommitTrees =new OrgCommitTreeRegistrySqlImpl(options);
    orgMemberRights = new OrgMemberRightRegistrySqlImpl(options);
    orgMemberships = new OrgMembershipRegistrySqlImpl(options);
    orgMembers = new OrgMemberRegistrySqlImpl(options);
    orgPartyRights =new OrgPartyRightRegistrySqlImpl(options);
    orgParties = new OrgPartyRegistrySqlImpl(options);
    orgRights = new OrgRightRegistrySqlImpl(options);
  }

  @Override
  public OrgCommitRegistry orgCommits() {
    return orgCommits;
  }

  @Override
  public OrgCommitTreeRegistry orgCommitTrees() {
    return orgCommitTrees;
  }

  @Override
  public OrgMemberRightRegistry orgMemberRights() {
    return orgMemberRights;
  }

  @Override
  public OrgMembershipRegistry orgMemberships() {
    return orgMemberships;
  }

  @Override
  public OrgMemberRegistry orgMembers() {
    return orgMembers;
  }

  @Override
  public OrgPartyRightRegistry orgPartyRights() {
    return orgPartyRights;
  }

  @Override
  public OrgPartyRegistry orgParties() {
    return orgParties;
  }

  @Override
  public OrgRightRegistry orgRights() {
    return orgRights;
  }

}
