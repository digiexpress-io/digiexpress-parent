package io.resys.thena.structures.org.anytree;

import java.util.List;

import io.resys.thena.api.entities.org.ImmutableOrgPartyHierarchy;
import io.resys.thena.api.entities.org.OrgActorStatusType;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.envelope.OrgPartyContainerVisitor;
import io.resys.thena.api.envelope.OrgTreeContainer.OrgAnyTreeContainerContext;
import io.resys.thena.api.envelope.OrgTreeContainer.OrgAnyTreeContainerVisitor;



public class PartyHierarchyContainerVisitor extends OrgPartyContainerVisitor<ImmutableOrgPartyHierarchy> 
  implements OrgAnyTreeContainerVisitor<ImmutableOrgPartyHierarchy> {
  
  private final String criteria;
  private CollectAll foundParty;
  
  public PartyHierarchyContainerVisitor(String partyIdOrNameOrExternalId) {
    super(false);
    this.criteria = partyIdOrNameOrExternalId;
  }
  
  @Override
  public ImmutableOrgPartyHierarchy close() {
    if(foundParty == null) {
      return null;
    }
    return foundParty.build();
  }

  @Override
  protected TopPartyVisitor visitTop(OrgParty group, OrgAnyTreeContainerContext worldState) {
    return visitAnyParty(group, worldState);
  }

  @Override
  protected PartyVisitor visitChild(OrgParty group, OrgAnyTreeContainerContext worldState) {
    return visitAnyParty(group, worldState);
  }
  
  private TopPartyVisitor visitAnyParty(OrgParty party, OrgAnyTreeContainerContext worldState) {
    if(party.isMatch(criteria) && party.getStatus() == OrgActorStatusType.IN_FORCE) {
      foundParty = new CollectAll();
      return foundParty;
    }
    return new IgnoreAll();
  }

  
  private static class IgnoreAll implements TopPartyVisitor {
    @Override
    public void start(OrgParty directParty, List<OrgParty> parents, List<OrgRight> parentRights, boolean isDisabled) {}
    @Override
    public void visitMembership(OrgParty directParty, OrgMembership membership, OrgMember user, boolean isDisabled) { }
    @Override
    public void visitPartyRight(OrgParty directParty, OrgPartyRight partyRight, OrgRight right, boolean isDisabled) {}
    @Override
    public void visitMemberRight(OrgParty directParty, OrgMember member, OrgMemberRight memberRight, OrgRight right, boolean isDisabled) {}
    @Override
    public void end(OrgParty group, List<OrgParty> parents, boolean isDisabled) {}
    @Override
    public TopPartyLogger visitLogger(OrgParty party) { return null; }
  }
  
  
  private static class CollectAll implements TopPartyVisitor {
    private final ImmutableOrgPartyHierarchy.Builder builder = ImmutableOrgPartyHierarchy.builder();
    public ImmutableOrgPartyHierarchy build() {
      return builder.log("").build();
    }
    
    @Override
    public void visitMembership(OrgParty party, OrgMembership membership, OrgMember user, boolean isDisabled) {
      builder.addMembers(user);
    }
    @Override
    public void visitPartyRight(OrgParty party, OrgPartyRight partyRight, OrgRight right, boolean isDisabled) {
      builder.addDirectRights(right);    
    }
    @Override
    public void visitMemberRight(OrgParty party, OrgMember user, OrgMemberRight partyRight, OrgRight right, boolean isDisabled) {
      builder.addMembers(user);
    }
    @Override
    public void start(OrgParty party, List<OrgParty> parents, List<OrgRight> parentRights, boolean isDisabled) {
      builder
        .party(party)
        .parentParties(parents)
        .parentRights(parentRights);
    }
    @Override
    public void end(OrgParty party, List<OrgParty> parents, boolean isDisabled) {}
  
    @Override
    public TopPartyLogger visitLogger(OrgParty party) {
      return null;
    }
  }
}
