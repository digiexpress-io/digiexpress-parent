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
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRight;

public interface ThenaOrgObjects extends ThenaObjects {
  
  
  // world state
  @Value.Immutable
  interface OrgProjectObjects extends ThenaOrgObjects { 
    Map<String, OrgParty> getParties();
    Map<String, OrgRight> getRights();
    Map<String, OrgMember> getMembers();
    
    Map<String, OrgMembership> getMemberships();
    Map<String, OrgPartyRight> getPartyRights();
    Map<String, OrgMemberRight> getMemberRights();
    
    Map<String, OrgCommit> getCommits();
    Map<String, OrgActorData> getActorData();
    Map<String, OrgActorStatus> getActorStatus(); 
  }
  
  @Value.Immutable
  interface OrgMemberHierarchy extends ThenaOrgObjects {
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
    
    List<OrgMemberPartyStatus> getUserGroupStatus();
    List<OrgMemberRightStatus> getUserRoleStatus();
  }
  
  @Value.Immutable
  interface OrgPartyHierarchy extends ThenaOrgObjects {
    String getCommitId();
    String getPartyId();
    String getPartyName();
    String getPartyDescription();
    
    @Nullable String getParentPartyId();
    @Nullable String getExternalId();
    
    String getLog();
    OrgActorStatusType getStatus();
    
    List<OrgRight> getRights();  // roles that are enabled
    List<OrgRight> getDirectRights();  // roles that are enabled
    
    List<OrgMember> getDirectMembers();
    List<OrgMember> getParenMembers();
    List<OrgMember> getChildMembers();
    
    List<OrgParty> getParentParties();
    List<OrgParty> getChildParties();
  }
  
  @Value.Immutable
  interface OrgRightHierarchy extends ThenaOrgObjects {
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
  interface OrgMemberPartyStatus extends ThenaOrgObjects {
    String getStatusId();
    String getGroupId();
    OrgActorStatusType getStatus();
  }
  @Value.Immutable
  interface OrgMemberRightStatus extends ThenaOrgObjects {
    String getStatusId();
    String getRoleId();
    OrgActorStatusType getStatus();
  }
}