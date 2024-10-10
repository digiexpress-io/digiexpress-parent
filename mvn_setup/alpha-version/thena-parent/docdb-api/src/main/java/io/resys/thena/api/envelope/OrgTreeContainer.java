package io.resys.thena.api.envelope;

import java.util.Collection;
import java.util.List;

import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.entities.org.OrgRight;

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
    List<OrgMemberRight> getMembersWithRights(String rightId);
    
    // Group related 
    OrgParty getParty(String partyId);
    List<OrgParty> getPartyChildren(String partyId);
    List<OrgMembership> getPartyMemberships(String partyId);
    List<OrgPartyRight> getPartyRights(String partyId);
    
    List<OrgParty> getPartyTops();
    List<OrgParty> getPartyBottoms();
    
    List<OrgMembership> getPartyInheritedMembers(String partyId);

    boolean isPartyDisabledUpward(OrgParty group);
  }
}
