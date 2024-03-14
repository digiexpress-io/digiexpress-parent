package io.resys.thena.docdb.models.org.anytree;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroupRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserMembership;
import io.resys.thena.docdb.models.org.anytree.AnyTreeContainer.AnyTreeContainerContext;
import io.resys.thena.docdb.models.org.anytree.AnyTreeContainer.AnyTreeContainerVisitor;


public abstract class AnyTreeContainerVisitorImpl<T> implements AnyTreeContainerVisitor<T> {
  
  public interface GroupVisitor {
    void start(OrgGroup group, List<OrgGroup> parents, boolean isDisabled);
    
    void visitMembership(OrgGroup group, OrgUserMembership membership, OrgUser user, boolean isDisabled);
    void visitMembershipWithInheritance(OrgGroup group, OrgUserMembership membership, OrgUser user, boolean isDisabled);
    
    void visitRole(OrgGroup group, OrgGroupRole groupRole, OrgRole role, boolean isDisabled);
    void visitChild(OrgGroup group, boolean isDisabled);
    void end(OrgGroup group, List<OrgGroup> parents, boolean isDisabled);
  }  
  @Override
  public void start(AnyTreeContainerContext worldState) {
    for(final var top : worldState.getGroupTops()) {
      visitGroup(top, worldState, Collections.emptyList());
    }
  }
  abstract GroupVisitor visitTop(OrgGroup group, AnyTreeContainerContext worldState);
  abstract GroupVisitor visitChild(OrgGroup group, AnyTreeContainerContext worldState);

  
  protected void visitGroup(OrgGroup group, AnyTreeContainerContext worldState, List<OrgGroup> parents) {
    final var visitor = group.getParentId() == null ? visitTop(group, worldState) : visitChild(group, worldState);
    final var isDisabledDirectly = worldState.isStatusDisabled(worldState.getStatus(group));
    final var isDisabledUpward = worldState.isGroupDisabledUpward(group);
    
    visitor.start(group, parents, isDisabledDirectly || isDisabledUpward);
    for(final var groupRole : worldState.getGroupRoles(group.getId())) {
      
      final var role = worldState.getRole(groupRole.getRoleId());
      final var groupRoleStatus = worldState.isStatusDisabled(worldState.getStatus(groupRole));
      final var roleStatus = worldState.isStatusDisabled(worldState.getStatus(role));
      
      visitor.visitRole(group, groupRole, role, groupRoleStatus || roleStatus);
    }
    for(final var member : worldState.getGroupMemberships(group.getId())) {
      
      final var user = worldState.getUser(member.getUserId());
      final var memberStatus = worldState.isStatusDisabled(worldState.getStatus(member));
      final var userStatus = worldState.isStatusDisabled(worldState.getStatus(user));
      
      visitor.visitMembership(group, member, user, memberStatus || userStatus);
    }
    for(final var member : worldState.getGroupInheritedUsers(group.getId())) {
      visitor.visitMembershipWithInheritance(group, member, worldState.getUser(member.getUserId()), false);
    }
    
    final var nextParents = ImmutableList.<OrgGroup>builder().addAll(parents).add(group).build();
    for(final var child : worldState.getGroupChildren(group.getId())) {
      visitor.visitChild(child, worldState.isStatusDisabled(worldState.getStatus(child)));
      visitGroup(child, worldState, nextParents);
    }
    
    visitor.end(group, parents, isDisabledDirectly || isDisabledUpward);
  }
}
