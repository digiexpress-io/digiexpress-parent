package io.resys.thena.api.envelope;

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

import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.entities.org.OrgRight;

public interface OrgTreeContainer {
  <T> T accept(OrgAnyTreeContainerVisitor<T> visitor);
  
  
  interface OrgAnyTreeContainerVisitor<T> {
    void start(OrgAnyTreeContainerContext ctx);
    T close();
  }
  
  interface OrgAnyTreeContainerContext {
    OrgMember getMember(String id);
    OrgRight getRight(String id);
    Collection<OrgRight> getRights();
    List<OrgMemberRight> getMemberRights(String memberId);
    List<OrgMemberRight> getMembersWithRights(String rightId);
    
    // Group related 
    OrgParty getParty(String partyId);
    List<OrgParty> getPartyChildren(String partyId);
    List<OrgMembership> getPartyMemberships(String partyId);
    List<OrgPartyRight> getPartyRights(String partyId);
    
    List<OrgParty> getPartyTops();
    List<OrgParty> getPartyBottoms();
    
    List<OrgMembership> getPartyInheritedMembers(String partyId);

    boolean isPartyDisabledUpward(OrgParty group);
  }
}
