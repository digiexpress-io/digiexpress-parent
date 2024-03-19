package io.resys.thena.docdb.api.actions;

import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelopeList;
import io.resys.thena.docdb.api.models.ThenaEnvelope.ThenaObjects;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgGroupHierarchy;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgRoleHierarchy;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserHierarchy;
import io.resys.thena.docdb.api.visitors.OrgTreeContainer.OrgAnyTreeContainerVisitor;
import io.smallrye.mutiny.Uni;

public interface OrgQueryActions {

  UserObjectsQuery userQuery();
  UserHierarchyQuery userHierarchyQuery();
  GroupHierarchyQuery groupHierarchyQuery();
  RoleHierarchyQuery roleHierarchyQuery();
  //OrphanedUserQuery orphanedUserQuery();
  
  interface UserObjectsQuery {
    UserObjectsQuery repoId(String repoId);
    
    Uni<QueryEnvelope<OrgUser>> get(String userId);
    Uni<QueryEnvelopeList<OrgUser>> findAll();
  }

  interface RoleHierarchyQuery {
    RoleHierarchyQuery repoId(String repoId);
    Uni<QueryEnvelope<OrgRoleHierarchy>> get(String roleIdOrNameOrExternalId);
    <T extends ThenaObjects> Uni<QueryEnvelope<T>> get(String roleIdOrNameOrExternalId, OrgAnyTreeContainerVisitor<T> visitor);
    Uni<QueryEnvelopeList<OrgRoleHierarchy>> findAll();
  }

  
  interface UserHierarchyQuery {
  	UserHierarchyQuery repoId(String repoId);
  	Uni<QueryEnvelope<OrgUserHierarchy>> get(String userIdOrNameOrExternalId);
  	Uni<QueryEnvelopeList<OrgUserHierarchy>> findAll();
  }
  
  interface GroupHierarchyQuery {
    GroupHierarchyQuery repoId(String repoId);
    Uni<QueryEnvelope<OrgGroupHierarchy>> get(String groupIdOrNameOrExternalId);
    Uni<QueryEnvelopeList<OrgGroupHierarchy>> findAll();
    
  }
}
