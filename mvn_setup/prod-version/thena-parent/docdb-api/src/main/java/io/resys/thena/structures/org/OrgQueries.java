package io.resys.thena.structures.org;

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

import java.util.Collection;
import java.util.List;

import io.resys.thena.api.entities.org.OrgCommit;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.datasource.ThenaDataSource;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface OrgQueries {
  ThenaDataSource getDataSource();
  PartyQuery parties();
  MembershipQuery memberships();
  RightsQuery rights();
  MemberQuery members();
  PartyRightsQuery partyRights();
  MemberRightsQuery memberRights();
  
  CommitQuery commits();

  interface MemberRightsQuery {
    Uni<OrgMemberRight> getById(String id);
    Multi<OrgMemberRight> findAll();
    Multi<OrgMemberRight> findAll(List<String> id);
    Multi<OrgMemberRight> findAllByMemberId(String id);
    Multi<OrgMemberRight> findAllByRightId(String id);
    Multi<OrgMemberRight> findAllByPartyId(String partyId);
  }
    
  interface PartyRightsQuery {
    Uni<OrgPartyRight> getById(String id);
    Multi<OrgPartyRight> findAll();
    Multi<OrgPartyRight> findAll(List<String> id);
    Multi<OrgPartyRight> findAllByPartyId(String id);
    Multi<OrgPartyRight> findAllByRightId(String id);
  }
  
  
  interface MembershipQuery {
    Multi<OrgMembership> findAll();
    Multi<OrgMembership> findAll(List<String> id);
    Multi<OrgMembership> findAllByPartyId(String id);
    Multi<OrgMembership> findAllByMemberId(String id);
    Uni<OrgMembership> getById(String id);
  }
  
  interface CommitQuery {
    Multi<OrgCommit> findAll();
    Uni<OrgCommit> getById(String id);
  }
  interface MemberQuery {
    Multi<OrgMember> findAll();
    Multi<OrgMember> findAll(Collection<String> id);
    Multi<OrgMember> findAllByRightId(String rightId);
    
    Uni<OrgMember> getById(String id); //user.id or user.email or user.external_id 
    Multi<OrgMember> findAllByPartyId(String id);
  }
  
  interface RightsQuery {    
    Multi<OrgRight> findAll();
    Multi<OrgRight> findAll(Collection<String> id);
    Multi<OrgRight> findAllByPartyId(String id);
    Multi<OrgRight> findAllByMemberId(String memberId);
    
    Uni<OrgRight> getById(String id);
  }
  
  interface PartyQuery {
    Multi<OrgParty> findAll();
    Multi<OrgParty> findAll(Collection<String> id);
    Multi<OrgParty> findAllByRightId(String rightId);
    Multi<OrgParty> findAllByMemberId(String memberId);
    
    Uni<OrgParty> getById(String id);
  }
}
