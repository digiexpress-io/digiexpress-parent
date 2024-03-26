package io.resys.thena.api.actions;

import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMember;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgMemberHierarchy;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgPartyHierarchy;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgRightHierarchy;
import io.resys.thena.api.models.QueryEnvelope;
import io.resys.thena.api.models.QueryEnvelopeList;
import io.resys.thena.api.models.ThenaEnvelope.ThenaObjects;
import io.resys.thena.api.visitors.OrgTreeContainer.OrgAnyTreeContainerVisitor;
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
    <T extends ThenaObjects> Uni<QueryEnvelope<T>> get(String idOrNameOrExtId, OrgAnyTreeContainerVisitor<T> visitor);
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
