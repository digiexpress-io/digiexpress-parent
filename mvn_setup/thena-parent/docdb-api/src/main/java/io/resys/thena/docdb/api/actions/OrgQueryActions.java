package io.resys.thena.docdb.api.actions;

import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelopeList;
import io.resys.thena.docdb.api.models.ThenaEnvelope.ThenaObjects;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgMemberHierarchy;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgPartyHierarchy;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgRightHierarchy;
import io.resys.thena.docdb.api.visitors.OrgTreeContainer.OrgAnyTreeContainerVisitor;
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
