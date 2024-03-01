package io.resys.thena.docdb.api.models;

import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.ThenaEnvelope.ThenaObjects;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorData;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgCommit;
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
    
    Map<String, OrgCommit> getCommits();
    Map<String, OrgActorData> getActorData();
    Map<String, OrgActorStatus> getActorStatus(); 
  }


  @Value.Immutable
  interface OrgUserObject extends ThenaOrgObjects { 
    OrgUser getUser();
    
    /*
    Map<String, OrgGroup> getGroups();
    Map<String, OrgRole> getRoles();
    
    Map<String, OrgUserMembership> getUserMembership();
    Map<String, OrgGroupRole> getGroupRoles();
    Map<String, OrgUserRole> getUserRoles();
    
    Map<String, OrgCommit> getCommits();
    Map<String, OrgActorData> getActorData();
    Map<String, OrgActorStatus> getActorStatus(); 
    */
  }
  
  @Value.Immutable
  interface OrgUserObjects extends ThenaOrgObjects {
    List<OrgUserObject> getUsers();
  }
  
  
  
  /*
  @Value.Immutable
  interface OrgUserRoleOrGroup extends ThenaOrgObject {
    String getId();
    String getNames();
    OrgUserRoleOrGroupType getType();
    OrgActorValue getActorStatus();
  }
  */
  
  
  @Value.Immutable
  interface OrgUserGroupsAndRolesWithLog extends ThenaOrgObjects {
  	String getUserId();
    String getExternalId();
    String getUserName();
    String getEmail();
    
    List<String> getRoleNames();  // roles that are enabled
    List<String> getGroupNames(); // groups that are enabled
    
    List<OrgUserRoleEvalLog> getRoleMeta();
    List<OrgUserGroupEvalLog> getGroupMeta();
  }
  
  @Value.Immutable
  interface OrgUserRoleEvalLog extends ThenaOrgObjects {
  	String getRoleName();
  	OrgActorStatusType getStatus();
  	String getSource();
  	Boolean getEnabled();
  }
  @Value.Immutable
  interface OrgUserGroupEvalLog extends ThenaOrgObjects {
  	String getGroupName();
  	OrgActorStatusType getStatus();
  	String getSource();
  	Boolean getEnabled();
  }
}