package io.resys.thena.api.entities.org;

import java.util.List;
import java.util.Map;

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
  }
  
  @Value.Immutable
  interface OrgMemberHierarchy extends ThenaOrgObjects {
    OrgMember getMember();
    
    String getLog();
    
    List<String> getRightNames();  // roles that are enabled
    List<String> getPartyNames(); // groups that are enabled

    List<String> getDirectRightNames();  // roles that are enabled
    List<String> getDirectPartyNames(); // groups that are enabled
    
    List<OrgMemberPartyStatus> getPartyStatus();
    List<OrgMemberRightStatus> getRightStatus();
  }
  
  @Value.Immutable
  interface OrgPartyHierarchy extends ThenaOrgObjects {
    OrgParty getParty();
    List<OrgParty> getParentParties();
    
    String getLog();
    
    List<OrgRight> getDirectRights();  // roles that are enabled
    List<OrgRight> getParentRights();  // roles that are enabled
    
    List<OrgMember> getMembers();
    
  }
  
  @Value.Immutable
  interface OrgRightHierarchy extends ThenaOrgObjects {
    OrgRight getRight();
    
    String getLog();
    
    List<OrgMember> getDirectMembers();
    List<OrgMember> getChildMembers();
    
    List<OrgParty> getDirectParty();
    List<OrgParty> getChildParty();
  }
  
  
  @Value.Immutable
  interface OrgMemberPartyStatus extends ThenaOrgObjects {
    String getPartyId();
    OrgActorStatusType getStatus();
  }
  @Value.Immutable
  interface OrgMemberRightStatus extends ThenaOrgObjects {
    String getRightId();
    OrgActorStatusType getStatus();
  }
}