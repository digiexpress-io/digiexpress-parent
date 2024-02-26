package io.resys.thena.docdb.api.models;

import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.ThenaEnvelope.ThenaObjects;
import io.resys.thena.docdb.api.models.ThenaOrgObject.IsOrgObject;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorCommitLog;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorData;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroupRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserRole;

public interface ThenaOrgObjects extends ThenaObjects {
  
  
  // world state
  @Value.Immutable
  interface OrgProjectObjects extends ThenaOrgObjects { 
    Map<String, OrgGroup> getGroups();
    Map<String, OrgRole> getRoles();
    Map<String, OrgUser> getUser();
    
    Map<String, OrgUserMembership> getUserMembership();
    Map<String, OrgGroupRole> getGroupRoles();
    Map<String, OrgUserRole> getUserRoles();
    
    Map<String, OrgActorCommitLog> getActorLogs();
    Map<String, OrgActorData> getActorData();
    Map<String, OrgActorStatus> getActorStatus(); 
  }

  
  @Value.Immutable
  interface OrgActorObjects extends ThenaOrgObjects { 
    IsOrgObject getTarget();
    Map<String, OrgActorCommitLog> getActorLogs();
    Map<String, OrgActorData> getActorData();
    Map<String, OrgActorStatus> getActorStatus();
  }
  
  
  @Value.Immutable
  interface OrgUserObject extends ThenaOrgObjects { 
    OrgUser getUser();
    
    Map<String, OrgGroup> getGroups();
    Map<String, OrgRole> getRoles();
    
    Map<String, OrgUserMembership> getUserMembership();
    Map<String, OrgGroupRole> getGroupRoles();
    Map<String, OrgUserRole> getUserRoles();
    
    Map<String, OrgActorCommitLog> getActorLogs();
    Map<String, OrgActorData> getActorData();
    Map<String, OrgActorStatus> getActorStatus(); 
  }
  
  @Value.Immutable
  interface OrgUserObjects extends ThenaOrgObjects {
    List<OrgUserObject> getUsers();
  }
  
  
  
  @Value.Immutable
  interface OrgGroupObject extends ThenaOrgObjects { 
    OrgGroup getGroup();
    Map<String, OrgUser> getUser();
    Map<String, OrgRole> getRoles();
    
    Map<String, OrgGroup> getTransitiveGroups();
    
    Map<String, OrgUserMembership> getUserMembership();
    Map<String, OrgGroupRole> getGroupRoles();
    Map<String, OrgUserRole> getUserRoles();
    
    Map<String, OrgActorCommitLog> getActorLogs();
    Map<String, OrgActorData> getActorData();
    Map<String, OrgActorStatus> getActorStatus(); 
  }
  
  
  @Value.Immutable
  interface OrgGroupObjects extends ThenaOrgObjects {
    List<OrgGroupObject> getGroups();
  }
  
  
  
  @Value.Immutable
  interface OrgRoleObject extends ThenaOrgObjects {
    OrgRole getRole();
    Map<String, OrgUser> getUser();
    Map<String, OrgGroup> getGroups();
    
    Map<String, OrgUserMembership> getUserMembership();
    Map<String, OrgGroupRole> getGroupRoles();
    Map<String, OrgUserRole> getUserRoles();
  }
  
  
  @Value.Immutable
  interface OrgRoleObjects extends ThenaOrgObjects {
    List<OrgRoleObject> getRoles();
  }
}