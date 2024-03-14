package io.resys.thena.docdb.models.org.userhierarchy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.ImmutableOrgUserGroupStatus;
import io.resys.thena.docdb.api.models.ImmutableOrgUserRoleStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserHierarchyEntry;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRoleFlattened;
import io.resys.thena.docdb.models.org.userhierarchy.UserTreeContainer.BottomUpVisitor;


public abstract class BottomUpVisitorTemplate<T> implements BottomUpVisitor<T> {

  private final List<String> inheritanceDisabledFromBottom = new ArrayList<>();
  private final List<String> groupsDisabled = new ArrayList<>();
  private final List<String> rolesDisabled = new ArrayList<>();
  private final List<String> visited = new ArrayList<>();
  private final List<OrgRoleFlattened> globalRoles;
  private boolean initDone;
  
  public BottomUpVisitorTemplate(List<OrgRoleFlattened> globalRoles) {
    this.globalRoles = globalRoles.stream().sorted((a, b) -> b.getRoleName().compareTo(a.getRoleName())).toList();
    this.initDone = false;
  }

  public abstract void visitGlobalRoleEnabled(String roleName);
  public abstract void visitGlobalRoleDisabled(String roleName);
  public abstract void visitGroupEnabled(String groupName, Optional<String> parentGroupName, boolean isDirect);
  public abstract void visitGroupDisabled(String groupName, Optional<String> parentGroupName, boolean isDirect);
  public abstract void visitRoleEnabled(String roleName, String groupName, Optional<String> parentGroupName, boolean isDirect);
  public abstract void visitRoleDisabled(String roleName, String groupName, Optional<String> parentGroupName, boolean isDirect);
  
  
  @Override
  public void visitBottom(UserTree bottom) {
    if(!initDone) {
      initDone = true;
      globalRoles.forEach(role -> visitGlobalRole(role));
    }
    visitTree(bottom);
  }
  @Override
  public void visitParent(UserTree parent) {
    visitTree(parent);
  }
  
  private void visitGlobalRole(OrgRoleFlattened globalRole) {
    if((globalRole.getRoleStatus() == null || globalRole.getRoleStatus() == OrgActorStatusType.IN_FORCE)) {
      visitGlobalRoleEnabled(globalRole.getRoleName());
    } else {
      visitGlobalRoleDisabled(globalRole.getRoleName());
    }
  }
  
  private void visitTree(UserTree tree) {
    // status need to be processed first, no exceptions
    tree.getGroupValues().forEach(value -> {
      visitRoleStatus(value);
      visitGroupStatus(tree, value);
    });
    tree.getRoleValues().values().forEach(value -> visitRoleStatus(value));
    
    
    // gather meta
    visitGroup(tree);    
    visitRole(tree);
  }
  
  private void visitGroup(UserTree tree) {
    final var directMembership = tree.isDirect();
    final var parentGroup = Optional.ofNullable(tree.getParent()).map(e -> e.getGroupName());
    if(isGroupDisabled(tree)) {
      visitGroupDisabled(tree.getGroupName(), parentGroup, directMembership);
      return;
    }
    
    visitGroupEnabled(tree.getGroupName(), parentGroup, directMembership);
  }
  
  private void visitRole(UserTree tree) {
    final var groupDisabled = isGroupDisabled(tree);
    
    final var directMembership = tree.isDirect();
    for(final var value : tree.getGroupValues()) {
      if(value.getRoleId() == null) {
        continue;
      }
      final var groupName = tree.getGroupName();
      final var parentGroup = Optional.ofNullable(tree.getParent()).map(e -> e.getGroupName());
      final var roleDisabled = rolesDisabled.contains(value.getRoleId());
      if(roleDisabled || groupDisabled) {
        this.visitRoleDisabled(value.getRoleName(), groupName, parentGroup, directMembership);
      } else {
        this.visitRoleEnabled(value.getRoleName(), groupName, parentGroup, directMembership);        
      }
    }
  }
  
  
  private boolean isGroupDisabled(UserTree tree) {
    final var groupDisabled = groupsDisabled.contains(tree.getGroupId());
    return groupDisabled;
  }
  
  private void visitGroupStatus(UserTree tree, OrgUserHierarchyEntry value) {
    
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
    
    // disable one group
    if(result.getStatus() != OrgActorStatusType.IN_FORCE) {
      groupsDisabled.add(value.getGroupId());
    }
    
    // disable the whole chain
    if(result.getStatus() != OrgActorStatusType.IN_FORCE && value.getGroupParentId() != null) {
      inheritanceDisabledFromBottom.add(value.getGroupParentId());
    }
  }
  
  private void visitRoleStatus(OrgUserHierarchyEntry value) {
    if(value.getRoleStatusId() == null || this.visited.contains(value.getRoleStatusId())) {
      return;
    }
    
    // extract status
    final var result = ImmutableOrgUserRoleStatus.builder()
        .roleId(value.getRoleId())
        .status(value.getRoleStatus())
        .statusId(value.getRoleStatusId())
        .build();

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
  }
}
