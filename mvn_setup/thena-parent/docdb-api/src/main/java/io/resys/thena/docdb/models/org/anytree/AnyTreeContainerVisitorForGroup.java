package io.resys.thena.docdb.models.org.anytree;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserMembership;
import io.resys.thena.docdb.models.org.anytree.AnyTreeContainer.AnyTreeContainerContext;
import io.resys.thena.docdb.models.org.anytree.AnyTreeContainer.AnyTreeContainerVisitor;


public abstract class AnyTreeContainerVisitorForGroup<T> implements AnyTreeContainerVisitor<T> {
  
  public interface GroupVisitor {
    void start(boolean isDisabled);
    void visitMembership(OrgUserMembership membership, boolean isDisabled);
    void visitMembershipWithInheritance(OrgUserMembership membership, boolean isDisabled);
    void end();
  }  
  @Override
  public void start(AnyTreeContainerContext worldState) {
    for(final var top : worldState.getGroupTops()) {
      visitGroup(top, worldState);
    }
  }
  abstract GroupVisitor visitTop(OrgGroup group, AnyTreeContainerContext worldState);
  abstract GroupVisitor visitChild(OrgGroup group, AnyTreeContainerContext worldState);

  
  protected void visitGroup(OrgGroup group, AnyTreeContainerContext worldState) {
    final var visitor = group.getParentId() == null ? visitTop(group, worldState) : visitChild(group, worldState);
    final var isDisabledDirectly = worldState.isStatusDisabled(worldState.getStatus(group));
    final var isDisabledUpward = worldState.isGroupDisabledUpward(group);
    
    visitor.start(isDisabledDirectly || isDisabledUpward);
    
    for(final var member : worldState.getGroupMemberships(group.getId())) {
      visitor.visitMembership(member, worldState.isStatusDisabled(worldState.getStatus(member)));
    }
    for(final var member : worldState.getGroupInheritedUsers(group.getId())) {
      visitor.visitMembership(member, false);
    }
    visitor.end();
    
    for(final var child : worldState.getGroupChildren(group.getId())) {
      visitGroup(child, worldState); 
    }
  }
}
