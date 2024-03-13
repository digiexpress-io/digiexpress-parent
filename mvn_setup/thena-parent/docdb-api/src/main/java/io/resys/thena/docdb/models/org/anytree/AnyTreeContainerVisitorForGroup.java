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


public abstract class AnyTreeContainerVisitorForGroup<T> implements AnyTreeContainerVisitor<T> {
  
  public interface GroupVisitor {
    void start(OrgGroup group, List<OrgGroup> parents, boolean isDisabled);
    
    void visitMembership(OrgGroup group, OrgUserMembership membership, OrgUser user, boolean isDisabled);
    void visitMembershipWithInheritance(OrgGroup group, OrgUserMembership membership, OrgUser user, boolean isDisabled);
    
    void visitRole(OrgGroup group, OrgGroupRole groupRole, OrgRole role, boolean isDisabled);
    void visitChild(OrgGroup group, boolean isDisabled);
    void end(OrgGroup group);
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
    
    for(final var member : worldState.getGroupMemberships(group.getId())) {
      visitor.visitMembership(group, member, worldState.getUser(member.getUserId()), worldState.isStatusDisabled(worldState.getStatus(member)));
    }
    for(final var member : worldState.getGroupInheritedUsers(group.getId())) {
      visitor.visitMembership(group, member, worldState.getUser(member.getUserId()), false);
    }
    for(final var groupRole : worldState.getGroupRoles(group.getId())) {
      visitor.visitRole(group, groupRole, worldState.getRole(groupRole.getRoleId()), worldState.isStatusDisabled(worldState.getStatus(groupRole)));
    }
    
    final var nextParents = ImmutableList.<OrgGroup>builder().addAll(parents).add(group).build();
    for(final var child : worldState.getGroupChildren(group.getId())) {
      visitor.visitChild(child, worldState.isStatusDisabled(worldState.getStatus(child)));
      visitGroup(child, worldState, nextParents);
    }
    
    visitor.end(group);
  }
}
