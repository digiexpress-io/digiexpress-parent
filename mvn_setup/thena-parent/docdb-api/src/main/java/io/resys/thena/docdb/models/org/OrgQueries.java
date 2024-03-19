package io.resys.thena.docdb.models.org;

import java.util.Collection;
import java.util.List;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgCommit;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRightFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberHierarchyEntry;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberRight;
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
    Uni<OrgMemberRight> getById(String id);
    Multi<OrgMemberRight> findAll();
    Multi<OrgMemberRight> findAll(List<String> id);
    Multi<OrgMemberRight> findAllByUserId(String id);
    Multi<OrgMemberRight> findAllByRoleId(String id);
  }
    
  interface GroupRolesQuery {
    Uni<OrgPartyRight> getById(String id);
    Multi<OrgPartyRight> findAll();
    Multi<OrgPartyRight> findAll(List<String> id);
    Multi<OrgPartyRight> findAllByGroupId(String id);
    Multi<OrgPartyRight> findAllByRoleId(String id);
  }
  
  
  interface UserMembershipQuery {
    Multi<OrgMembership> findAll();
    Multi<OrgMembership> findAll(List<String> id);
    Multi<OrgMembership> findAllByGroupId(String id);
    Multi<OrgMembership> findAllByUserId(String id);
    Uni<OrgMembership> getById(String id);
  }
  
  interface CommitQuery {
    Multi<OrgCommit> findAll();
    Uni<OrgCommit> getById(String id);
  }
  
  
  interface UserQuery {
    Multi<OrgMember> findAll();
    Multi<OrgMember> findAll(List<String> id);
    Uni<OrgMember> getById(String id); //user.id or user.email or user.external_id 
    Uni<List<OrgMemberHierarchyEntry>> findAllUserHierarchyEntries(String userId);
    Uni<List<OrgRightFlattened>> findAllRolesByUserId(String userId);
    Uni<OrgMemberFlattened> getStatusById(String userId);
  }
  
  interface RoleQuery {    
    Multi<OrgRight> findAll();
    Multi<OrgRight> findAll(Collection<String> id);
    Uni<OrgRight> getById(String id);
  }
  
  interface GroupQuery {
    Multi<OrgParty> findAll();
    Multi<OrgParty> findAll(Collection<String> id);
    Uni<OrgParty> getById(String id);
  }
}
