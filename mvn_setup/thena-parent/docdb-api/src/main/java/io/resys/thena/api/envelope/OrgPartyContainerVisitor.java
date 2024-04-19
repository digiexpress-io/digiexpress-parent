package io.resys.thena.api.envelope;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

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
    void start(OrgParty group, List<OrgParty> parents, List<OrgRight> parentRights, boolean isDisabled);
    
    void visitDirectMembership(OrgParty group, OrgMembership membership, OrgMember user, boolean isDisabled);
    void visitMembershipWithInheritance(OrgParty group, OrgMembership membership, OrgMember user, boolean isDisabled);
    
    // direct and inherited right
    void visitDirectMemberPartyRight(OrgParty party, OrgMemberRight memberRight, OrgRight right, boolean isDisabled);
    void visitDirectPartyRight(List<OrgParty> parents, OrgParty party, OrgPartyRight partyRight, OrgRight right, boolean isDisabled);
    void visitChildParty(OrgParty party, boolean isDisabled);
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
    final var isDisabledDirectly = worldState.isStatusDisabled(worldState.getStatus(party));
    final var isDisabledUpward = worldState.isPartyDisabledUpward(party);
    
    if(isDisabledDirectly && !includeDisabled) {
      return;
    }
    
    final var parentRights = parents.stream()
        .flatMap(e -> worldState.getPartyRights(e.getId()).stream())
        .map(right -> worldState.getRight(right.getId()))
        .toList();
    
    visitor.start(party, parents, parentRights, isDisabledDirectly || isDisabledUpward);
    for(final var groupRole : worldState.getPartyRights(party.getId())) {
      
      final var role = worldState.getRight(groupRole.getRightId());
      final var groupRoleStatus = worldState.isStatusDisabled(worldState.getStatus(groupRole));
      final var roleStatus = worldState.isStatusDisabled(worldState.getStatus(role));
      
      final var isRoleDisabled = groupRoleStatus || roleStatus;
      if(isRoleDisabled && !includeDisabled) {
        continue;
      }
      
      visitor.visitDirectPartyRight(parents, party, groupRole, role, groupRoleStatus || roleStatus);
    }
    
    // direct party members and their rights in current party
    for(final var member : worldState.getPartyMemberships(party.getId())) {
      final var user = worldState.getMember(member.getMemberId());
      final var memberStatus = worldState.isStatusDisabled(worldState.getStatus(member));
      final var userStatus = worldState.isStatusDisabled(worldState.getStatus(user));
      final var isUserDisabled = memberStatus || userStatus;
      
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
        final var groupRoleStatus = worldState.isStatusDisabled(worldState.getStatus(memberRight));
        final var roleStatus = worldState.isStatusDisabled(worldState.getStatus(role));
        final var isRoleDisabled = groupRoleStatus || roleStatus;
        
        if(isRoleDisabled && !includeDisabled) {
          continue;
        }
        visitor.visitDirectMemberPartyRight(party, memberRight, role, isRoleDisabled);
      }
    }
    
    // inherited members and their rights
    for(final var inheritedMembership : worldState.getPartyInheritedMembers(party.getId())) {
      final var inheritedMember = worldState.getMember(inheritedMembership.getMemberId());
      visitor.visitMembershipWithInheritance(party, inheritedMembership, inheritedMember, false);
      
      for(final var right : worldState.getMemberRights(inheritedMember.getId())) {
        if(right.getPartyId() == null) {
          continue;
        }
        if(!right.getPartyId().equals(inheritedMembership.getPartyId())) {
          continue;
        }
        
        final var role = worldState.getRight(right.getRightId());
        final var groupRoleStatus = worldState.isStatusDisabled(worldState.getStatus(right));
        final var roleStatus = worldState.isStatusDisabled(worldState.getStatus(role));
        visitor.visitDirectMemberPartyRight(party, right, role, groupRoleStatus || roleStatus);
      }
      
    }
    
    final var nextParents = ImmutableList.<OrgParty>builder().addAll(parents).add(party).build();
    for(final var child : worldState.getPartyChildren(party.getId())) {
      visitor.visitChildParty(child, worldState.isStatusDisabled(worldState.getStatus(child)));
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
}
