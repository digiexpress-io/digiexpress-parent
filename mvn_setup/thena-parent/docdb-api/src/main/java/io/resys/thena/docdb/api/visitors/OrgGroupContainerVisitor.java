package io.resys.thena.docdb.api.visitors;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberRight;
import io.resys.thena.docdb.api.visitors.OrgTreeContainer.OrgAnyTreeContainerContext;
import io.resys.thena.docdb.api.visitors.OrgTreeContainer.OrgAnyTreeContainerVisitor;


public abstract class OrgGroupContainerVisitor<T> implements OrgAnyTreeContainerVisitor<T> {
  
  private final boolean includeDisabled;
  
  public OrgGroupContainerVisitor(boolean includeDisabled) {
    super();
    this.includeDisabled = includeDisabled;
  }


  public interface GroupVisitor {
    void start(OrgParty group, List<OrgParty> parents, boolean isDisabled);
    
    void visitMembership(OrgParty group, OrgMembership membership, OrgMember user, boolean isDisabled);
    void visitMembershipWithInheritance(OrgParty group, OrgMembership membership, OrgMember user, boolean isDisabled);
    
    void visitRole(OrgParty group, OrgMemberRight groupRole, OrgRight role, boolean isDisabled);
    void visitRole(OrgParty group, OrgPartyRight groupRole, OrgRight role, boolean isDisabled);
    void visitChild(OrgParty group, boolean isDisabled);
    void end(OrgParty group, List<OrgParty> parents, boolean isDisabled);
  }
  
  @Override
  public void start(OrgAnyTreeContainerContext worldState) {
    for(final var top : worldState.getPartyTops()) {
      visitGroup(top, worldState, Collections.emptyList());
    }
  }
  protected abstract GroupVisitor visitTop(OrgParty group, OrgAnyTreeContainerContext worldState);
  protected abstract GroupVisitor visitChild(OrgParty group, OrgAnyTreeContainerContext worldState);

  
  protected void visitGroup(OrgParty group, OrgAnyTreeContainerContext worldState, List<OrgParty> parents) {
    final var parentGroupIds = parents.stream().map(e -> e.getId()).toList();
    final var visitor = group.getParentId() == null ? visitTop(group, worldState) : visitChild(group, worldState);
    final var isDisabledDirectly = worldState.isStatusDisabled(worldState.getStatus(group));
    final var isDisabledUpward = worldState.isGroupDisabledUpward(group);
    
    if(isDisabledDirectly && !includeDisabled) {
      return;
    }
    
    visitor.start(group, parents, isDisabledDirectly || isDisabledUpward);
    for(final var groupRole : worldState.getPartyRights(group.getId())) {
      
      final var role = worldState.getRole(groupRole.getRightId());
      final var groupRoleStatus = worldState.isStatusDisabled(worldState.getStatus(groupRole));
      final var roleStatus = worldState.isStatusDisabled(worldState.getStatus(role));
      
      final var isRoleDisabled = groupRoleStatus || roleStatus;
      if(isRoleDisabled && !includeDisabled) {
        continue;
      }
      
      visitor.visitRole(group, groupRole, role, groupRoleStatus || roleStatus);
    }
    
    for(final var member : worldState.getPartyMemberships(group.getId())) {
      
      final var user = worldState.getUser(member.getMemberId());
      final var memberStatus = worldState.isStatusDisabled(worldState.getStatus(member));
      final var userStatus = worldState.isStatusDisabled(worldState.getStatus(user));
      final var isUserDisabled = memberStatus || userStatus;
      
      if(isUserDisabled && !includeDisabled) {
        continue;
      }
      
      visitor.visitMembership(group, member, user, isUserDisabled);
      
      
      for(final var groupUserRole : worldState.getMemberRoles(user.getId())) {
        if(groupUserRole.getPartyId() == null) {
          continue;
        }
        if(!parentGroupIds.contains(groupUserRole.getPartyId())) {
          continue;
        }
        final var role = worldState.getRole(groupUserRole.getRightId());
        final var groupRoleStatus = worldState.isStatusDisabled(worldState.getStatus(groupUserRole));
        final var roleStatus = worldState.isStatusDisabled(worldState.getStatus(role));
        final var isRoleDisabled = groupRoleStatus || roleStatus;
        
        if(isRoleDisabled && !includeDisabled) {
          continue;
        }
        visitor.visitRole(group, groupUserRole, role, isRoleDisabled);
      }
      
    }
    for(final var member : worldState.getPartyInheritedMembers(group.getId())) {
      final var user = worldState.getUser(member.getMemberId());
      visitor.visitMembershipWithInheritance(group, member, user, false);
      
      for(final var groupUserRole : worldState.getMemberRoles(user.getId())) {
        if(!group.getId().equals(groupUserRole.getPartyId())) {
          continue;
        }
        final var role = worldState.getRole(groupUserRole.getRightId());
        final var groupRoleStatus = worldState.isStatusDisabled(worldState.getStatus(groupUserRole));
        final var roleStatus = worldState.isStatusDisabled(worldState.getStatus(role));
        visitor.visitRole(group, groupUserRole, role, groupRoleStatus || roleStatus);
      }
      
    }
    
    final var nextParents = ImmutableList.<OrgParty>builder().addAll(parents).add(group).build();
    for(final var child : worldState.getPartyChildren(group.getId())) {
      visitor.visitChild(child, worldState.isStatusDisabled(worldState.getStatus(child)));
      visitGroup(child, worldState, nextParents);
    }
    
    visitor.end(group, parents, isDisabledDirectly || isDisabledUpward);
  }
}
