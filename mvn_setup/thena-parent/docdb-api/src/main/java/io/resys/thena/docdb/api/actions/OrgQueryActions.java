package io.resys.thena.docdb.api.actions;

import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelopeList;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserGroupsAndRolesWithLog;
import io.smallrye.mutiny.Uni;

public interface OrgQueryActions {

  UserObjectsQuery userQuery();
  UserGroupsAndRolesQuery userGroupsAndRolesQuery();
  
  interface UserObjectsQuery {
    UserObjectsQuery repoId(String repoId);
    
    Uni<QueryEnvelope<OrgUser>> get(String userId);
    Uni<QueryEnvelopeList<OrgUser>> findAll();
  }
  
  interface UserGroupsAndRolesQuery {
  	UserGroupsAndRolesQuery repoId(String repoId);
  	Uni<QueryEnvelope<OrgUserGroupsAndRolesWithLog>> get(String userId);
  	Uni<QueryEnvelopeList<OrgUserGroupsAndRolesWithLog>> findAll();
  }
}
