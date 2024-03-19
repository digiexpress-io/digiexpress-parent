package io.resys.permission.client.spi;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.resys.permission.client.api.model.ImmutablePermission;
import io.resys.permission.client.api.model.ImmutablePrincipal;
import io.resys.permission.client.api.model.ImmutableRoleHierarchy;
import io.resys.permission.client.api.model.ImmutableRoleHierarchyContainer;
import io.resys.permission.client.api.model.RoleHierarchyContainer;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroupRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserRole;
import io.resys.thena.docdb.api.visitors.OrgGroupContainerVisitor;
import io.resys.thena.docdb.api.visitors.OrgGroupContainerVisitor.GroupVisitor;
import io.resys.thena.docdb.api.visitors.OrgTreeContainer.OrgAnyTreeContainerContext;
import io.resys.thena.docdb.api.visitors.OrgTreeContainer.OrgAnyTreeContainerVisitor;


public class RoleHierarchyQueryVisitor extends OrgGroupContainerVisitor<RoleHierarchyContainer> 
  implements OrgAnyTreeContainerVisitor<RoleHierarchyContainer>, GroupVisitor {
  
  private final String groupIdOrNameOrExternalId;
 
  private final Map<String, ImmutablePermission> permissions = new LinkedHashMap<>(); // permissions by name
  private final Map<String, ImmutablePrincipal> principals = new LinkedHashMap<>();   // principals by name
    
  private String foundRoleId;
  private ImmutableRoleHierarchy.Builder foundRole;

  public RoleHierarchyQueryVisitor(String groupIdOrNameOrExternalId) {
    super(false);
    this.groupIdOrNameOrExternalId = groupIdOrNameOrExternalId;
  }
  
  private boolean isDirectGroup(OrgGroup group) {
    if(foundRoleId != null) {
      return group.getId().equals(foundRoleId);
    }
    return false;  
  }
  
  @Override
  public void visitMembershipWithInheritance(OrgGroup group, OrgUserMembership membership, OrgUser user, boolean isDisabled) {
    if(foundRoleId == null) {
      return;
    }
    if(!principals.containsKey(user.getUserName())) {
      permissions.put(user.getUserName(), null);
    }
    
    
    if(isDirectGroup(group)) {
      // TODO builder.addParenUsers(user);
    } else {
      // TODO       builder.addChildUsers(user);
    }
    
  }
  @Override
  public void visitMembership(OrgGroup group, OrgUserMembership membership, OrgUser user, boolean isDisabled) {
    if(!principals.containsKey(user.getUserName())) {
      permissions.put(user.getUserName(), null);
    }
    
    
    if(!isDirectGroup(group)) {
      return;
    }
    // TODO builder.addDirectUsers(user);
  }
  @Override
  public void visitRole(OrgGroup group, OrgGroupRole groupRole, OrgRole role, boolean isDisabled) {
    
    if(foundRoleId == null) {
      return;
    }
    // TODO builder.addDirectRoleNames(role);    
  }
  @Override
  public void visitRole(OrgGroup group, OrgUserRole groupRole, OrgRole role, boolean isDisabled) {

    
  }
  
  @Override
  public void visitChild(OrgGroup group, boolean isDisabled) {
    
    if(foundRoleId == null) {
      return;
    }
    // TODO builder.addChildGroups(group);
  }
  @Override
  public void start(OrgGroup group, List<OrgGroup> parents, boolean isDisabled) {

    if(group.isMatch(groupIdOrNameOrExternalId)) {
      
      foundRoleId = group.getId();
      foundRole = ImmutableRoleHierarchy.builder().role(null);
    }
  }
  
  @Override
  public void end(OrgGroup group, List<OrgGroup> parents, boolean isDisabled) {
    if(group.getId().equals(foundRoleId)) {
      foundRoleId = null;
    }
  }
  
  @Override
  public RoleHierarchyContainer close() {
    return ImmutableRoleHierarchyContainer.builder()
        .log("")
        .build();
  }
  @Override
  protected GroupVisitor visitTop(OrgGroup group, OrgAnyTreeContainerContext worldState) {
    return this;
  }
  @Override
  protected GroupVisitor visitChild(OrgGroup group, OrgAnyTreeContainerContext worldState) {
    return this;
  }
}
