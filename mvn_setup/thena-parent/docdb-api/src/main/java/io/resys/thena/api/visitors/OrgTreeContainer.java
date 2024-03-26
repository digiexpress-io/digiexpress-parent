package io.resys.thena.api.visitors;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.resys.thena.api.entities.org.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMember;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMemberRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMembership;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgParty;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgRight;

public interface OrgTreeContainer {
  <T> T accept(OrgAnyTreeContainerVisitor<T> visitor);
  
  
  interface OrgAnyTreeContainerVisitor<T> {
    void start(OrgAnyTreeContainerContext ctx);
    T close();
  }
  
  interface OrgAnyTreeContainerContext {
    OrgMember getMember(String id);
    OrgRight getRight(String id);
    Collection<OrgRight> getRights();
    List<OrgMemberRight> getMemberRights(String memberId);
    
    // Group related 
    OrgParty getParty(String partyId);
    List<OrgParty> getPartyChildren(String partyId);
    List<OrgMembership> getPartyMemberships(String partyId);
    List<OrgPartyRight> getPartyRights(String partyId);
    
    List<OrgParty> getPartyTops();
    List<OrgParty> getPartyBottoms();
    
    List<OrgMembership> getPartyInheritedMembers(String partyId);

    // Status for all entities
    Optional<OrgActorStatus> getStatus(OrgParty party);
    Optional<OrgActorStatus> getStatus(OrgMembership membership);
    Optional<OrgActorStatus> getStatus(OrgMemberRight memberRight);
    Optional<OrgActorStatus> getStatus(OrgPartyRight partyRight);
    Optional<OrgActorStatus> getStatus(OrgMember member);
    Optional<OrgActorStatus> getStatus(OrgRight right);
    boolean isStatusDisabled(Optional<OrgActorStatus> status);
    boolean isPartyDisabledUpward(OrgParty group);
  }
}
