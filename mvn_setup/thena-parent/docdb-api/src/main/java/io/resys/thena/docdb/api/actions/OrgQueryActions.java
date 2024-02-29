package io.resys.thena.docdb.api.actions;

import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgGroupObject;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgGroupObjects;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgRoleObject;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgRoleObjects;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserObject;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserObjects;
import io.smallrye.mutiny.Uni;

public interface OrgQueryActions {
  UserObjectsQuery userQuery();
  GroupObjectsQuery groupQuery();
  GroupObjectsQuery roleQuery();
  
  
  interface UserObjectsQuery {
    UserObjectsQuery repoId(String repoId);
    UserObjectsQuery includeLog(OrgSelection includeLog); // defaults to none
    UserObjectsQuery includeData(OrgSelection includeData); // defaults to none
    
    Uni<QueryEnvelope<OrgUserObject>> get(String userId);
    Uni<QueryEnvelope<OrgUserObjects>> findAll();
  }
  
  
  interface GroupObjectsQuery {
    UserObjectsQuery repoId(String repoId);
    UserObjectsQuery includeTransitive(boolean includeTransitive); // defaults to false
    UserObjectsQuery includeLog(OrgSelection includeLog); // defaults to none
    UserObjectsQuery includeUser(OrgSelection includeLog); // defaults to none
    UserObjectsQuery includeData(OrgSelection includeData); // defaults to none
    UserObjectsQuery groupId(String groupId);
    
    Uni<QueryEnvelope<OrgGroupObject>> get();
    Uni<QueryEnvelope<OrgGroupObjects>> findAll();
  }
  
  interface RoleObjectsQuery {
    UserObjectsQuery repoId(String repoId);
    UserObjectsQuery includeTransitive(boolean includeTransitive); // defaults to false
    UserObjectsQuery includeLog(OrgSelection includeLog); // defaults to none
    UserObjectsQuery includeUser(OrgSelection includeLog); // defaults to none
    UserObjectsQuery includeData(OrgSelection includeData); // defaults to none
    UserObjectsQuery includeGroup(OrgSelection includeData); // defaults to none
    UserObjectsQuery includeMembership(OrgSelection includeData); // defaults to none
    UserObjectsQuery roleId(String roleId);
    UserObjectsQuery groupId(String groupId);
    UserObjectsQuery userId(String userId);
    
    Uni<QueryEnvelope<OrgRoleObject>> get();
    Uni<QueryEnvelope<OrgRoleObjects>> findAll();
  }
  
  enum OrgSelection {
    LATEST, ALL, NONE
  }

}
