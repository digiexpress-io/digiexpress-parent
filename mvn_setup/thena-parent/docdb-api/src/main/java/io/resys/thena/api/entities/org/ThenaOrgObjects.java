package io.resys.thena.api.entities.org;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.api.envelope.ThenaContainer;

public interface ThenaOrgObjects extends ThenaContainer {
  
  
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
    OrgActorStatus.OrgActorStatusType getStatus();
    
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
    OrgActorStatus.OrgActorStatusType getStatus();
    
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
    OrgActorStatus.OrgActorStatusType getStatus();
    
    String getLog();
    
    List<OrgMember> getDirectMembers();
    List<OrgMember> getChildMembers();
    
    List<OrgParty> getDirectGroup();
    List<OrgParty> getChildGroup();
  }
  
  
  @Value.Immutable
  interface OrgMemberPartyStatus extends ThenaOrgObjects {
    String getStatusId();
    String getGroupId();
    OrgActorStatus.OrgActorStatusType getStatus();
    default String getId() { 
      return getStatusId(); 
    }
  }
  @Value.Immutable
  interface OrgMemberRightStatus extends ThenaOrgObjects {
    String getStatusId();
    String getRoleId();
    OrgActorStatus.OrgActorStatusType getStatus();
    default String getId() { 
      return getStatusId(); 
    }
  }
}