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
import io.resys.thena.api.envelope.OrgPartyContainerVisitor.TopPartyVisitor;
import io.resys.thena.api.envelope.OrgPartyLogVisitor;
import io.resys.thena.api.envelope.OrgTreeContainer.OrgAnyTreeContainerContext;
import io.resys.thena.api.envelope.OrgTreeContainer.OrgAnyTreeContainerVisitor;



public class PartyHierarchyContainerVisitor extends OrgPartyContainerVisitor<ImmutableOrgPartyHierarchy> 
  implements OrgAnyTreeContainerVisitor<ImmutableOrgPartyHierarchy>, TopPartyVisitor {
  
  private final String partyIdOrNameOrExternalId;
  private final ImmutableOrgPartyHierarchy.Builder builder = ImmutableOrgPartyHierarchy.builder();
  
  private String foundPartyId;
  private boolean partyFound;
  
  public PartyHierarchyContainerVisitor(String partyIdOrNameOrExternalId) {
    super(true);
    this.partyIdOrNameOrExternalId = partyIdOrNameOrExternalId;
  }
  
  private boolean isDirectParty(OrgParty party) {
    if(foundPartyId != null) {
      return party.getId().equals(foundPartyId);
    }
    return false;  
  }
  
  @Override
  public void visitInheritedMembership(OrgParty party, OrgMembership membership, OrgMember user, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    if(foundPartyId == null) {
      return;
    }
  }
  @Override
  public void visitDirectMembership(OrgParty party, OrgMembership membership, OrgMember user, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    if(!isDirectParty(party)) {
      return;
    }
    builder.addDirectMembers(user);
  }
  @Override
  public void visitPartyRight(OrgParty party, OrgPartyRight partyRight, OrgRight right, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    
    if(foundPartyId == null) {
      return;
    }
    
    builder.addDirectRights(right);    
  }
  @Override
  public void visitMemberRight(OrgParty party, OrgMemberRight partyRight, OrgRight right, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    
  }
  
  @Override
  public void visitChildParty(OrgParty party, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    
    if(foundPartyId == null) {
      return;
    }
    builder.addChildParties(party);
  }
  @Override
  public void start(OrgParty party, List<OrgParty> parents, List<OrgRight> parentRights, boolean isDisabled) {
    if(party.isMatch(partyIdOrNameOrExternalId)) {
      
      partyFound = true;
      foundPartyId = party.getId();
      builder
        .partyId(party.getId())
        .partyName(party.getPartyName())
        .partyDescription(party.getPartyDescription())
        .externalId(party.getExternalId())
        .commitId(party.getCommitId())
        .parentPartyId(party.getParentId())
        .parentParties(parents)
        .parentRights(parentRights)
        .status(isDisabled ? OrgActorStatusType.DISABLED : OrgActorStatusType.IN_FORCE);
    }
  }
  @Override
  public void end(OrgParty party, List<OrgParty> parents, boolean isDisabled) {
    if(party.getId().equals(foundPartyId)) {
      foundPartyId = null;
    }
  }
  
  @Override
  public ImmutableOrgPartyHierarchy close() {
    if(partyFound) {
      return builder.log("").build();
    }
    return null;
  }
  @Override
  protected TopPartyVisitor visitTop(OrgParty party, OrgAnyTreeContainerContext worldState) {
    return this;
  }
  @Override
  protected PartyVisitor visitChild(OrgParty party, OrgAnyTreeContainerContext worldState) {
    return this;
  }

  @Override
  public TopPartyLogger visitLogger(OrgParty party) {
    return new OrgPartyLogVisitor(party.getId(), false) {
      @Override
      public String close() {
        final var log = super.close();
        builder.log(log);
        return log;
      }
    };
  }

}
