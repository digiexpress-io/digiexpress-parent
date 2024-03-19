package io.resys.permission.client.spi;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.resys.permission.client.api.model.ImmutablePermission;
import io.resys.permission.client.api.model.ImmutablePrincipal;
import io.resys.permission.client.api.model.ImmutableRoleHierarchy;
import io.resys.permission.client.api.model.ImmutableRoleHierarchyContainer;
import io.resys.permission.client.api.model.RoleHierarchyContainer;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberRight;
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
  
  private boolean isDirectGroup(OrgParty group) {
    if(foundRoleId != null) {
      return group.getId().equals(foundRoleId);
    }
    return false;  
  }
  
  @Override
  public void visitMembershipWithInheritance(OrgParty group, OrgMembership membership, OrgMember user, boolean isDisabled) {
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
  public void visitMembership(OrgParty group, OrgMembership membership, OrgMember user, boolean isDisabled) {
    if(!principals.containsKey(user.getUserName())) {
      permissions.put(user.getUserName(), null);
    }
    
    
    if(!isDirectGroup(group)) {
      return;
    }
    // TODO builder.addDirectUsers(user);
  }
  @Override
  public void visitRole(OrgParty group, OrgPartyRight groupRole, OrgRight role, boolean isDisabled) {
    
    if(foundRoleId == null) {
      return;
    }
    // TODO builder.addDirectRoleNames(role);    
  }
  @Override
  public void visitRole(OrgParty group, OrgMemberRight groupRole, OrgRight role, boolean isDisabled) {

    
  }
  
  @Override
  public void visitChild(OrgParty group, boolean isDisabled) {
    
    if(foundRoleId == null) {
      return;
    }
    // TODO builder.addChildGroups(group);
  }
  @Override
  public void start(OrgParty group, List<OrgParty> parents, boolean isDisabled) {

    if(group.isMatch(groupIdOrNameOrExternalId)) {
      
      foundRoleId = group.getId();
      foundRole = ImmutableRoleHierarchy.builder().role(null);
    }
  }
  
  @Override
  public void end(OrgParty group, List<OrgParty> parents, boolean isDisabled) {
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
  protected GroupVisitor visitTop(OrgParty group, OrgAnyTreeContainerContext worldState) {
    return this;
  }
  @Override
  protected GroupVisitor visitChild(OrgParty group, OrgAnyTreeContainerContext worldState) {
    return this;
  }
}
