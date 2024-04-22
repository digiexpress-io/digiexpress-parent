package io.resys.thena.api.envelope;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import io.resys.thena.api.entities.org.OrgActorStatusType;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.envelope.OrgTreeContainer.OrgAnyTreeContainerContext;
import io.resys.thena.api.envelope.OrgTreeContainer.OrgAnyTreeContainerVisitor;
import jakarta.annotation.Nullable;


public abstract class OrgPartyContainerVisitor<T> implements OrgAnyTreeContainerVisitor<T> {
  
  protected final boolean includeDisabled;
  private final boolean log;
  
  public OrgPartyContainerVisitor(boolean includeDisabled) {
    super();
    this.includeDisabled = includeDisabled;
    this.log = false;
  }
  
  public OrgPartyContainerVisitor(boolean includeDisabled, boolean logging) {
    super();
    this.includeDisabled = includeDisabled;
    this.log = logging;
  }

  public interface PartyVisitor {
    void start(OrgParty directParty, List<OrgParty> parents, List<OrgRight> parentRights, boolean isDisabled);
    
    void visitDirectMembership(OrgParty directParty, OrgMembership membership, OrgMember user, boolean isDisabled);
    void visitInheritedMembership(OrgParty directParty, OrgMembership membership, OrgMember user, boolean isDisabled);
    
    void visitPartyRight(OrgParty directParty, OrgPartyRight partyRight, OrgRight right, boolean isDisabled);
    
    // direct and inherited member with right
    void visitMemberRight(OrgParty directParty, OrgMemberRight memberRight, OrgRight right, boolean isDisabled);
    void visitChildParty(OrgParty myDirectNextLevelChild, boolean isDisabled);
    void end(OrgParty group, List<OrgParty> parents, boolean isDisabled);
  }

  public interface TopPartyVisitor extends PartyVisitor {
    @Nullable TopPartyLogger visitLogger(OrgParty party);
  }
  public interface TopPartyLogger extends TopPartyVisitor {
    void start(OrgAnyTreeContainerContext worldState);
    void visitGroup(OrgParty party, OrgAnyTreeContainerContext worldState, List<OrgParty> parents);
    String close();
  }
  
  protected abstract TopPartyVisitor visitTop(OrgParty group, OrgAnyTreeContainerContext worldState);
  protected abstract PartyVisitor visitChild(OrgParty group, OrgAnyTreeContainerContext worldState);
  
  @Override
  public void start(OrgAnyTreeContainerContext worldState) {
    for(final var top : worldState.getPartyTops()) {      
      visitGroup(top, worldState, Collections.emptyList());
    }
  }
  public void visitGroup(OrgParty party, OrgAnyTreeContainerContext worldState, List<OrgParty> parents) {
    //final var parentGroupIds = parents.stream().map(e -> e.getId()).toList();
    final var visitor = party.getParentId() == null ? visitTop(party, worldState) : visitChild(party, worldState);
    final var isDisabledDirectly = party.getStatus() == OrgActorStatusType.DISABLED;
    final var isDisabledUpward = worldState.isPartyDisabledUpward(party);
    
    if(isDisabledDirectly && !includeDisabled) {
      return;
    }

    final var parentRights = parents.stream()
        .flatMap(e -> worldState.getPartyRights(e.getId()).stream())
        .map(partyRight -> worldState.getRight(partyRight.getRightId()))
        .toList();
    
    visitor.start(party, parents, parentRights, isDisabledDirectly || isDisabledUpward);
    for(final var groupRole : worldState.getPartyRights(party.getId())) {
      
      final var right = worldState.getRight(groupRole.getRightId());
      final var isRoleDisabled = right.getStatus() == OrgActorStatusType.DISABLED;
      if(isRoleDisabled && !includeDisabled) {
        continue;
      }
      
      visitor.visitPartyRight(party, groupRole, right, isRoleDisabled);
    }
    
    // direct party members and their rights in current party
    for(final var member : worldState.getPartyMemberships(party.getId())) {
      final var user = worldState.getMember(member.getMemberId());
      final var isUserDisabled = user.getStatus() == OrgActorStatusType.DISABLED;
      
      if(isUserDisabled && !includeDisabled) {
        continue;
      }
      
      visitor.visitDirectMembership(party, member, user, isUserDisabled);
      
      // directly given to member but only for specified party via inheritance
      for(final var memberRight : worldState.getMemberRights(user.getId())) {
        if(memberRight.getPartyId() == null) {
          continue;
        }
        if(!party.getId().equals(memberRight.getPartyId())) {
          continue;
        }
        final var role = worldState.getRight(memberRight.getRightId());
        final var isRoleDisabled = role.getStatus() == OrgActorStatusType.DISABLED;
        
        if(isRoleDisabled && !includeDisabled) {
          continue;
        }
        visitor.visitMemberRight(party, memberRight, role, isRoleDisabled);
      }
    }
    
    // inherited members and their rights
    for(final var inheritedMembership : worldState.getPartyInheritedMembers(party.getId())) {
      final var inheritedMember = worldState.getMember(inheritedMembership.getMemberId());
      visitor.visitInheritedMembership(party, inheritedMembership, inheritedMember, false);
      
      for(final var memberRight : worldState.getMemberRights(inheritedMember.getId())) {
        if(memberRight.getPartyId() == null) {
          continue;
        }
        if(!memberRight.getPartyId().equals(inheritedMembership.getPartyId())) {
          continue;
        }
        
        final var right = worldState.getRight(memberRight.getRightId());
        final var isDisabled = right.getStatus() == OrgActorStatusType.DISABLED;

        if(isDisabled && !includeDisabled) {
          continue;
        }
        visitor.visitMemberRight(party, memberRight, right, isDisabled);
      }
      
    }
    
    
    final var nextParents = combineNotNull(parents, party);
    
    
    for(final var child : worldState.getPartyChildren(party.getId())) {
      visitor.visitChildParty(child, child.getStatus() == OrgActorStatusType.DISABLED);
      visitGroup(child, worldState, nextParents);
    }
    
    if(log && visitor instanceof TopPartyVisitor && party.getParentId() == null) {
      final var logger = ((TopPartyVisitor) visitor).visitLogger(party);
      if(logger != null) {
        logger.start(worldState);
        logger.visitGroup(party, worldState, Collections.emptyList());
        logger.close();
        logger.end(party, parents, includeDisabled);
      }
      
    }
    
    visitor.end(party, parents, isDisabledDirectly || isDisabledUpward);
  }
  
  @SuppressWarnings("hiding")
  private <T> List<T> combineNotNull(List<T> all, T single) {
    final var result = ImmutableList.<T>builder().addAll(all);
    if(single != null) {
      result.add(single);
    }
    return result.build();
  }
}
