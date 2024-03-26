package io.resys.thena.structures.org.memberhierarchy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.api.entities.org.ImmutableOrgMemberPartyStatus;
import io.resys.thena.api.entities.org.ImmutableOrgMemberRightStatus;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMemberHierarchyEntry;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgRightFlattened;
import io.resys.thena.structures.org.memberhierarchy.MemberTreeContainer.BottomUpVisitor;


public abstract class BottomUpVisitorTemplate<T> implements BottomUpVisitor<T> {

  private final List<String> inheritanceDisabledFromBottom = new ArrayList<>();
  private final List<String> groupsDisabled = new ArrayList<>();
  private final List<String> rolesDisabled = new ArrayList<>();
  private final List<String> visited = new ArrayList<>();
  private final List<OrgRightFlattened> globalRoles;
  private boolean initDone;
  
  public BottomUpVisitorTemplate(List<OrgRightFlattened> globalRoles) {
    this.globalRoles = globalRoles.stream().sorted((a, b) -> b.getRightName().compareTo(a.getRightName())).toList();
    this.initDone = false;
  }

  public abstract void visitGlobalRoleEnabled(String roleName);
  public abstract void visitGlobalRoleDisabled(String roleName);
  public abstract void visitGroupEnabled(String groupName, Optional<String> parentGroupName, boolean isDirect);
  public abstract void visitGroupDisabled(String groupName, Optional<String> parentGroupName, boolean isDirect);
  public abstract void visitRoleEnabled(String roleName, String groupName, Optional<String> parentGroupName, boolean isDirect);
  public abstract void visitRoleDisabled(String roleName, String groupName, Optional<String> parentGroupName, boolean isDirect);
  
  
  @Override
  public void visitBottom(MemberTree bottom) {
    if(!initDone) {
      initDone = true;
      globalRoles.forEach(role -> visitGlobalRole(role));
    }
    visitTree(bottom);
  }
  @Override
  public void visitParent(MemberTree parent) {
    visitTree(parent);
  }
  
  private void visitGlobalRole(OrgRightFlattened globalRole) {
    if((globalRole.getRightStatus() == null || globalRole.getRightStatus() == OrgActorStatusType.IN_FORCE)) {
      visitGlobalRoleEnabled(globalRole.getRightName());
    } else {
      visitGlobalRoleDisabled(globalRole.getRightName());
    }
  }
  
  private void visitTree(MemberTree tree) {
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
  
  private void visitGroup(MemberTree tree) {
    final var directMembership = tree.isDirect();
    final var parentGroup = Optional.ofNullable(tree.getParent()).map(e -> e.getGroupName());
    if(isGroupDisabled(tree)) {
      visitGroupDisabled(tree.getGroupName(), parentGroup, directMembership);
      return;
    }
    
    visitGroupEnabled(tree.getGroupName(), parentGroup, directMembership);
  }
  
  private void visitRole(MemberTree tree) {
    final var groupDisabled = isGroupDisabled(tree);
    
    final var directMembership = tree.isDirect();
    for(final var value : tree.getGroupValues()) {
      if(value.getRightId() == null) {
        continue;
      }
      final var groupName = tree.getGroupName();
      final var parentGroup = Optional.ofNullable(tree.getParent()).map(e -> e.getGroupName());
      final var roleDisabled = rolesDisabled.contains(value.getRightId());
      if(roleDisabled || groupDisabled) {
        this.visitRoleDisabled(value.getRightName(), groupName, parentGroup, directMembership);
      } else {
        this.visitRoleEnabled(value.getRightName(), groupName, parentGroup, directMembership);        
      }
    }
  }
  
  
  private boolean isGroupDisabled(MemberTree tree) {
    final var groupDisabled = groupsDisabled.contains(tree.getGroupId());
    return groupDisabled;
  }
  
  private void visitGroupStatus(MemberTree tree, OrgMemberHierarchyEntry value) {
    
    // disabled
    if(!tree.isDirect() && inheritanceDisabledFromBottom.contains(value.getPartyId())) {
      groupsDisabled.add(value.getPartyId());
    }
    
    if(value.getPartyStatusId() == null || this.visited.contains(value.getPartyStatusId())) {
      return;
    }
    
    // Extract status
    final var result = ImmutableOrgMemberPartyStatus.builder()
        .groupId(value.getPartyId())
        .status(value.getPartyStatus())
        .statusId(value.getPartyStatusId())
        .build();
    
    // disable one group
    if(result.getStatus() != OrgActorStatusType.IN_FORCE) {
      groupsDisabled.add(value.getPartyId());
    }
    
    // disable the whole chain
    if(result.getStatus() != OrgActorStatusType.IN_FORCE && value.getPartyParentId() != null) {
      inheritanceDisabledFromBottom.add(value.getPartyParentId());
    }
  }
  
  private void visitRoleStatus(OrgMemberHierarchyEntry value) {
    if(value.getRightStatusId() == null || this.visited.contains(value.getRightStatusId())) {
      return;
    }
    
    // extract status
    final var result = ImmutableOrgMemberRightStatus.builder()
        .roleId(value.getRightId())
        .status(value.getRightStatus())
        .statusId(value.getRightStatusId())
        .build();

    if(result.getStatus() != OrgActorStatusType.IN_FORCE) {
      rolesDisabled.add(result.getRoleId());
    }
  }
  
  private void visitRoleStatus(OrgRightFlattened value) {
    if(value.getRightStatusId() == null || this.visited.contains(value.getRightStatusId())) {
      return;
    }
    
    // extract status
    final var result = ImmutableOrgMemberRightStatus.builder()
        .roleId(value.getRightId())
        .status(value.getRightStatus())
        .statusId(value.getRightStatusId())
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
