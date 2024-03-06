package io.resys.thena.docdb.models.org;

import java.util.List;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgCommit;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroupAndRoleFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroupRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRoleFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserRole;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface OrgQueries {
  
  GroupQuery groups();
  UserMembershipQuery userMemberships();
  RoleQuery roles();
  UserQuery users();
  GroupRolesQuery groupRoles();
  ActorStatusQuery actorStatus();
  UserRolesQuery userRoles();
  CommitQuery commits();
  
  interface ActorStatusQuery {
    Uni<OrgActorStatus> getById(String id);
    Multi<OrgActorStatus> findAll();

  }
  interface UserRolesQuery {
    Uni<OrgUserRole> getById(String id);
    Multi<OrgUserRole> findAll();
    Multi<OrgUserRole> findAll(List<String> id);
    Multi<OrgUserRole> findAllByUserId(String id);
    Multi<OrgUserRole> findAllByRoleId(String id);
  }
    
  interface GroupRolesQuery {
    Uni<OrgGroupRole> getById(String id);
    Multi<OrgGroupRole> findAll();
    Multi<OrgGroupRole> findAll(List<String> id);
    Multi<OrgGroupRole> findAllByGroupId(String id);
    Multi<OrgGroupRole> findAllByRoleId(String id);
  }
  
  
  interface UserMembershipQuery {
    Multi<OrgUserMembership> findAll();
    Multi<OrgUserMembership> findAll(List<String> id);
    Multi<OrgUserMembership> findAllByGroupId(String id);
    Multi<OrgUserMembership> findAllByUserId(String id);
    Uni<OrgUserMembership> getById(String id);
  }
  
  interface CommitQuery {
    Multi<OrgCommit> findAll();
    Uni<OrgCommit> getById(String id);
  }
  
  
  interface UserQuery {
    Multi<OrgUser> findAll();
    Multi<OrgUser> findAll(List<String> id);
    Uni<OrgUser> getById(String id); //user.id or user.email or user.external_id 
    Uni<List<OrgGroupAndRoleFlattened>> findAllGroupsAndRolesByUserId(String userId);
    Uni<List<OrgRoleFlattened>> findAllRolesByUserId(String userId);
    Uni<OrgUserFlattened> getStatusById(String userId);
  }
  
  interface RoleQuery {    
    Multi<OrgRole> findAll();
    Multi<OrgRole> findAll(List<String> id);
    Uni<OrgRole> getById(String id);
  }
  
  interface GroupQuery {
    Multi<OrgGroup> findAll();
    Multi<OrgGroup> findAll(List<String> id);
    Uni<OrgGroup> getById(String id);
  }
}
