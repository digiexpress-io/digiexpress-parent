package io.resys.thena.structures.org.anytree;

import java.util.List;

import io.resys.thena.api.entities.org.ImmutableOrgPartyHierarchy;
import io.resys.thena.api.entities.org.OrgActorStatus;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.envelope.OrgPartyContainerVisitor;
import io.resys.thena.api.envelope.OrgPartyContainerVisitor.TopPartyVisitor;
import io.resys.thena.api.envelope.OrgTreeContainer.OrgAnyTreeContainerContext;
import io.resys.thena.api.envelope.OrgTreeContainer.OrgAnyTreeContainerVisitor;



public class PartyHierarchyContainerVisitor extends OrgPartyContainerVisitor<ImmutableOrgPartyHierarchy> 
  implements OrgAnyTreeContainerVisitor<ImmutableOrgPartyHierarchy>, TopPartyVisitor {
  
  private final String groupIdOrNameOrExternalId;
  private final ImmutableOrgPartyHierarchy.Builder builder = ImmutableOrgPartyHierarchy.builder();
  
  private String foundGroupId;
  private boolean partyFound;
  
  public PartyHierarchyContainerVisitor(String groupIdOrNameOrExternalId) {
    super(true);
    this.groupIdOrNameOrExternalId = groupIdOrNameOrExternalId;
  }
  
  private boolean isDirectGroup(OrgParty group) {
    if(foundGroupId != null) {
      return group.getId().equals(foundGroupId);
    }
    return false;  
  }
  
  @Override
  public void visitMembershipWithInheritance(OrgParty group, OrgMembership membership, OrgMember user, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    
    if(foundGroupId == null) {
      return;
    }
    if(isDirectGroup(group)) {
      builder.addParenMembers(user);
    } else {
      builder.addChildMembers(user);
    }
    
  }
  @Override
  public void visitMembership(OrgParty group, OrgMembership membership, OrgMember user, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    
    
    if(!isDirectGroup(group)) {
      return;
    }
    builder.addDirectMembers(user);
  }
  @Override
  public void visitPartyRight(List<OrgParty> parents, OrgParty group, OrgPartyRight groupRole, OrgRight role, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    
    if(foundGroupId == null) {
      return;
    }
    
    // visiting child right
    if(parents.stream().filter(p -> p.getId().equals(foundGroupId)).findFirst().isPresent()) {
      return;
    }
    
    builder.addDirectRights(role);    
  }
  @Override
  public void visitMemberPartyRight(OrgParty group, OrgMemberRight groupRole, OrgRight role, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    
  }
  
  @Override
  public void visitChildParty(OrgParty group, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    
    if(foundGroupId == null) {
      return;
    }
    builder.addChildParties(group);
  }
  @Override
  public void start(OrgParty group, List<OrgParty> parents, List<OrgRight> parentRights, boolean isDisabled) {

    
    if( groupIdOrNameOrExternalId.equals(group.getExternalId()) ||
        groupIdOrNameOrExternalId.equals(group.getPartyName()) ||
        groupIdOrNameOrExternalId.equals(group.getId())) {
      
      partyFound = true;
      foundGroupId = group.getId();
      builder
        .partyId(group.getId())
        .partyName(group.getPartyName())
        .partyDescription(group.getPartyDescription())
        .externalId(group.getExternalId())
        .commitId(group.getCommitId())
        .parentPartyId(group.getParentId())
        .parentParties(parents)

        .status(isDisabled ? OrgActorStatus.OrgActorStatusType.DISABLED : OrgActorStatus.OrgActorStatusType.IN_FORCE);
    }
  }
  @Override
  public void end(OrgParty group, List<OrgParty> parents, boolean isDisabled) {
    if(group.getId().equals(foundGroupId)) {
      foundGroupId = null;
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
  protected TopPartyVisitor visitTop(OrgParty group, OrgAnyTreeContainerContext worldState) {
    return this;
  }
  @Override
  protected PartyVisitor visitChild(OrgParty group, OrgAnyTreeContainerContext worldState) {
    return this;
  }

  @Override
  public void visitLog(String log) {
    
  }
}
