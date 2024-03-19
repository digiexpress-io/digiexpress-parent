package io.resys.thena.docdb.api.models;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.ThenaEnvelope.ThenaObjects;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorData;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgCommit;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberRight;

public interface ThenaOrgObjects extends ThenaObjects {
  
  
  // world state
  @Value.Immutable
  interface OrgProjectObjects extends ThenaOrgObjects { 
    Map<String, OrgParty> getGroups();
    Map<String, OrgRight> getRoles();
    Map<String, OrgMember> getUsers();
    
    Map<String, OrgMembership> getUserMemberships();
    Map<String, OrgPartyRight> getGroupRoles();
    Map<String, OrgMemberRight> getUserRoles();
    
    Map<String, OrgCommit> getCommits();
    Map<String, OrgActorData> getActorData();
    Map<String, OrgActorStatus> getActorStatus(); 
  }
  
  @Value.Immutable
  interface OrgUserHierarchy extends ThenaOrgObjects {
    String getCommitId();
  	String getUserId();
    @Nullable String getExternalId();
    String getUserName();
    String getEmail();
    
    String getLog();
    OrgActorStatusType getStatus();
    
    List<String> getRoleNames();  // roles that are enabled
    List<String> getGroupNames(); // groups that are enabled

    List<String> getDirectRoleNames();  // roles that are enabled
    List<String> getDirectGroupNames(); // groups that are enabled
    
    List<OrgUserGroupStatus> getUserGroupStatus();
    List<OrgUserRoleStatus> getUserRoleStatus();
  }
  
  @Value.Immutable
  interface OrgGroupHierarchy extends ThenaOrgObjects {
    String getCommitId();
    String getGroupId();
    String getGroupName();
    
    @Nullable String getParentGroupId();
    @Nullable String getExternalId();
    
    String getLog();
    OrgActorStatusType getStatus();
    
    List<OrgRight> getRoleNames();  // roles that are enabled
    List<OrgRight> getDirectRoleNames();  // roles that are enabled
    
    List<OrgMember> getDirectUsers();
    List<OrgMember> getParenUsers();
    List<OrgMember> getChildUsers();
    
    List<OrgParty> getParentGroups();
    List<OrgParty> getChildGroups();
  }
  
  @Value.Immutable
  interface OrgRoleHierarchy extends ThenaOrgObjects {
    String getRoleId();
    String getCommitId();
    @Nullable String getExternalId();
    String getRoleName();
    String getRoleDescription();
    OrgActorStatusType getStatus();
    
    String getLog();
    
    List<OrgMember> getDirectUsers();
    List<OrgMember> getChildUsers();
    
    List<OrgParty> getDirectGroup();
    List<OrgParty> getChildGroup();
  }
  
  
  @Value.Immutable
  interface OrgUserGroupStatus extends ThenaOrgObjects {
    String getStatusId();
    String getGroupId();
    OrgActorStatusType getStatus();
  }
  @Value.Immutable
  interface OrgUserRoleStatus extends ThenaOrgObjects {
    String getStatusId();
    String getRoleId();
    OrgActorStatusType getStatus();
  }
}