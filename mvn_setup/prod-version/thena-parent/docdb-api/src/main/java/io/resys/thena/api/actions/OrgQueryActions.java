package io.resys.thena.api.actions;

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

import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgMemberHierarchy;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgPartyHierarchy;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgRightHierarchy;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.api.envelope.ThenaContainer;
import io.resys.thena.api.envelope.OrgTreeContainer.OrgAnyTreeContainerVisitor;
import io.smallrye.mutiny.Uni;

public interface OrgQueryActions {

  MemberObjectsQuery memberQuery();
  MemberHierarchyQuery memberHierarchyQuery();
  PartyHierarchyQuery partyHierarchyQuery();
  RightHierarchyQuery rightHierarchyQuery();
  //OrphanedUserQuery orphanedUserQuery();
  
  interface MemberObjectsQuery {
    Uni<QueryEnvelope<OrgMember>> get(String userId);
    Uni<QueryEnvelopeList<OrgMember>> findAll();
  }

  interface RightHierarchyQuery {
    Uni<QueryEnvelope<OrgRightHierarchy>> get(String idOrNameOrExtId);
    <T extends ThenaContainer> Uni<QueryEnvelope<T>> get(String idOrNameOrExtId, OrgAnyTreeContainerVisitor<T> visitor);
    Uni<QueryEnvelopeList<OrgRightHierarchy>> findAll();
  }

  
  interface MemberHierarchyQuery {
  	Uni<QueryEnvelope<OrgMemberHierarchy>> get(String idOrNameOrExtId);
  	Uni<QueryEnvelopeList<OrgMemberHierarchy>> findAll();
  }
  
  interface PartyHierarchyQuery {
    Uni<QueryEnvelope<OrgPartyHierarchy>> get(String idOrNameOrExtId);
    Uni<QueryEnvelopeList<OrgPartyHierarchy>> findAll();
    
  }
}
