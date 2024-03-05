package io.resys.thena.docdb.api.actions;

import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserGroupsAndRolesWithLog;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserObjects;
import io.smallrye.mutiny.Uni;

public interface OrgQueryActions {

  UserObjectsQuery userQuery();
  UserGroupsAndRolesQuery userGroupsAndRolesQuery();
  
  interface UserObjectsQuery {
    UserObjectsQuery repoId(String repoId);
    
    Uni<QueryEnvelope<OrgUser>> get(String userId);
    Uni<QueryEnvelope<OrgUserObjects>> findAll();
  }
  
  interface UserGroupsAndRolesQuery {
  	UserGroupsAndRolesQuery repoId(String repoId);
  	Uni<QueryEnvelope<OrgUserGroupsAndRolesWithLog>> get(String userId);
  }
}
