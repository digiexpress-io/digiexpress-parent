package io.resys.thena.docdb.api.actions;

import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelopeList;
import io.resys.thena.docdb.api.models.ThenaEnvelope.ThenaObjects;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgPartyHierarchy;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgRightHierarchy;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgMemberHierarchy;
import io.resys.thena.docdb.api.visitors.OrgTreeContainer.OrgAnyTreeContainerVisitor;
import io.smallrye.mutiny.Uni;

public interface OrgQueryActions {

  MemberObjectsQuery memberQuery();
  MemberHierarchyQuery memberHierarchyQuery();
  PartyHierarchyQuery partyHierarchyQuery();
  RightHierarchyQuery rightHierarchyQuery();
  //OrphanedUserQuery orphanedUserQuery();
  
  interface MemberObjectsQuery {
    MemberObjectsQuery repoId(String repoId);
    
    Uni<QueryEnvelope<OrgMember>> get(String userId);
    Uni<QueryEnvelopeList<OrgMember>> findAll();
  }

  interface RightHierarchyQuery {
    RightHierarchyQuery repoId(String repoId);
    Uni<QueryEnvelope<OrgRightHierarchy>> get(String roleIdOrNameOrExternalId);
    <T extends ThenaObjects> Uni<QueryEnvelope<T>> get(String roleIdOrNameOrExternalId, OrgAnyTreeContainerVisitor<T> visitor);
    Uni<QueryEnvelopeList<OrgRightHierarchy>> findAll();
  }

  
  interface MemberHierarchyQuery {
  	MemberHierarchyQuery repoId(String repoId);
  	Uni<QueryEnvelope<OrgMemberHierarchy>> get(String userIdOrNameOrExternalId);
  	Uni<QueryEnvelopeList<OrgMemberHierarchy>> findAll();
  }
  
  interface PartyHierarchyQuery {
    PartyHierarchyQuery repoId(String repoId);
    Uni<QueryEnvelope<OrgPartyHierarchy>> get(String groupIdOrNameOrExternalId);
    Uni<QueryEnvelopeList<OrgPartyHierarchy>> findAll();
    
  }
}
