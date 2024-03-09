package io.resys.thena.docdb.models.org.usertree;

import java.util.ArrayList;
import java.util.List;

import org.immutables.value.Value;
import org.slf4j.Logger;

import io.resys.thena.docdb.api.models.ImmutableOrgUserGroupStatus;
import io.resys.thena.docdb.api.models.ImmutableOrgUserRoleStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroupAndRoleFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRoleFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserGroupStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserRoleStatus;
import io.resys.thena.docdb.models.org.usertree.UserTreeContainer.BottomUpVisitor;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateGroups implements BottomUpVisitor<ImmutableUserGroupsData> {

  private final Logger logger;
  private final OrgUserFlattened user;
  private final List<OrgRoleFlattened> globalRoles;
  
  private final ImmutableUserGroupsData.Builder groups = ImmutableUserGroupsData.builder();
  private final List<String> inheritanceDisabledFromBottom = new ArrayList<>();
  private final List<String> groupsDisabled = new ArrayList<>();
  private final List<String> rolesDisabled = new ArrayList<>();
  private final List<String> visited = new ArrayList<>();

  
  @Override
  public void visitBottom(UserTree bottom) {
    visitTree(bottom); 
  }
  
  @Override
  public void visitParent(UserTree parent) {
    visitTree(parent);
    
    if(parent.getParent() == null && logger != null) {
      logger.debug("org/user_tree_inputs/{}/user_group_tree \r\n{}", user.getUserName(), JsonObject.mapFrom(parent).encodePrettily());
    }
  }
  
  @Override
  public ImmutableUserGroupsData close() {
    globalRoles.forEach(role -> visitGlobalRole(role));
    return groups.build();
  }

  private void visitGlobalRole(OrgRoleFlattened globalRole) {
    if((globalRole.getRoleStatus() == null || globalRole.getRoleStatus() == OrgActorStatusType.IN_FORCE)) {
      groups.addRoleNames(globalRole.getRoleName());
      groups.addRoleNamesWithDirectMembership(globalRole.getRoleName());
    }
  }
  
  private ImmutableUserGroupsData visitTree(UserTree tree) {
    // status need to be processed first, no exceptions
    tree.getGroupValues().forEach(value -> {
      visitRoleStatus(value);
      visitGroupStatus(tree, value);
    });
    tree.getRoleValues().values().forEach(value -> visitRoleStatus(value));
    
    
    // gather meta
    visitGroup(tree);    
    visitRole(tree);
    
    return groups.build();
  }
  private void visitGroup(UserTree tree) {
    
    
    if(isGroupDisabled(tree)) {
      groups.addGroupNamesWithDirectMembershipDisabled(tree.getGroupName());
      return;
    }
    
    final var directMembership = tree.isDirect();
    if(directMembership) {
      groups.addGroupNamesWithDirectMembership(tree.getGroupName());
    }
    groups.addGroupNames(tree.getGroupName());
  }
  
  private void visitRole(UserTree tree) {
    if(isGroupDisabled(tree)) {
      return;
    }
    final var directMembership = tree.isDirect();
    for(final var value : tree.getGroupValues()) {
      if(value.getRoleId() == null) {
        continue;
      }
      if(rolesDisabled.contains(value.getRoleId())) {
        continue;
      }
      groups.addRoleNames(value.getRoleName());
      if(directMembership) {
        groups.addRoleNamesWithDirectMembership(value.getRoleName());
      }
    }
  }
  
  
  private boolean isGroupDisabled(UserTree tree) {
    final var groupDisabled = groupsDisabled.contains(tree.getGroupId());
    return groupDisabled;
  }
  
  private void visitGroupStatus(UserTree tree, OrgGroupAndRoleFlattened value) {
    
    // disabled
    if(!tree.isDirect() && inheritanceDisabledFromBottom.contains(value.getGroupId())) {
      groupsDisabled.add(value.getGroupId());
    }
    
    if(value.getGroupStatusId() == null || this.visited.contains(value.getGroupStatusId())) {
      return;
    }
    
    // Extract status
    final var result = ImmutableOrgUserGroupStatus.builder()
        .groupId(value.getGroupId())
        .status(value.getGroupStatus())
        .statusId(value.getGroupStatusId())
        .build();
    this.groups.addGroupStatus(result);
    
    // disable one group
    if(result.getStatus() != OrgActorStatusType.IN_FORCE) {
      groupsDisabled.add(value.getGroupId());
    }
    
    // disable the whole chain
    if(result.getStatus() != OrgActorStatusType.IN_FORCE && value.getGroupParentId() != null) {
      inheritanceDisabledFromBottom.add(value.getGroupParentId());
    }
  }
  
  private void visitRoleStatus(OrgGroupAndRoleFlattened value) {
    if(value.getRoleStatusId() == null || this.visited.contains(value.getRoleStatusId())) {
      return;
    }
    
    // extract status
    final var result = ImmutableOrgUserRoleStatus.builder()
        .roleId(value.getRoleId())
        .status(value.getRoleStatus())
        .statusId(value.getRoleStatusId())
        .build();
    this.groups.addRoleStatus(result);
    if(result.getStatus() != OrgActorStatusType.IN_FORCE) {
      rolesDisabled.add(result.getRoleId());
    }
  }
  
  private void visitRoleStatus(OrgRoleFlattened value) {
    if(value.getRoleStatusId() == null || this.visited.contains(value.getRoleStatusId())) {
      return;
    }
    
    // extract status
    final var result = ImmutableOrgUserRoleStatus.builder()
        .roleId(value.getRoleId())
        .status(value.getRoleStatus())
        .statusId(value.getRoleStatusId())
        .build();
    this.groups.addRoleStatus(result);
    if(result.getStatus() != OrgActorStatusType.IN_FORCE) {
      rolesDisabled.add(result.getRoleId());
    }
  }
  
  
  @Value.Immutable
  public interface UserGroupsData {
    List<String> getRoleNames();
    List<String> getGroupNames();
    List<String> getGroupNamesWithDirectMembership();
    List<String> getRoleNamesWithDirectMembership();
    
    List<String> getGroupNamesWithDirectMembershipDisabled();
    List<OrgUserGroupStatus> getGroupStatus(); 
    List<OrgUserRoleStatus> getRoleStatus(); 
  }
}
