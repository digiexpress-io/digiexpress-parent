package io.resys.thena.docdb.models.org.usertree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.barfuin.texttree.api.DefaultNode;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;

import io.resys.thena.docdb.api.LogConstants;
import io.resys.thena.docdb.api.models.ImmutableOrgUserGroupStatus;
import io.resys.thena.docdb.api.models.ImmutableOrgUserGroupsAndRolesWithLog;
import io.resys.thena.docdb.api.models.ImmutableOrgUserRoleStatus;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroupAndRoleFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRoleFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserGroupsAndRolesWithLog;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = LogConstants.SHOW_ORG_USER_TREE_CALC)
public class OrgTreeBuilderForUser {

  private final Map<String, OrgGroupAndRoleFlattened> groupsById = new HashMap<>();
  private final Map<String, List<OrgGroupAndRoleFlattened>> groupsByParentId = new HashMap<>();
  private final Map<String, OrgRoleFlattened> rolesById = new HashMap<>();

  private final Map<String, DefaultNode> membershipNodes = new HashMap<>();
  private final Map<String, DefaultNode> groupNodes = new HashMap<>();
  private final Map<String, DefaultNode> groupRoleNodes = new HashMap<>();

  private final List<String> visitedGroupIds = new ArrayList<>();
  private final List<String> visitedRoleIds = new ArrayList<>();
  private final List<String> visitedMemberships = new ArrayList<>();
  
  private final List<OrgGroupAndRoleFlattened> roots = new ArrayList<>();
  private final OrgUserFlattened user;
  private final ImmutableOrgUserGroupsAndRolesWithLog.Builder result;
  private final OrgUserGroupsAndRolesWithLog res;
  
  
  public OrgTreeBuilderForUser(OrgUserFlattened user, List<OrgGroupAndRoleFlattened> groupData, List<OrgRoleFlattened> roleData) {
    this.res = new UserTreeBuilder()
    .user(user)
    .groupData(groupData)
    .roleData(roleData)
    .build();
    
    this.user = user;
    this.result = ImmutableOrgUserGroupsAndRolesWithLog.builder()
        .userId(user.getId())
        .userName(user.getUserName())
        .externalId(user.getExternalId())
        .email(user.getEmail())
        .commitId(user.getCommitId())
        .status(user.getStatus() == null ? OrgActorStatusType.IN_FORCE : user.getStatus());
      
    for(final var entry : groupData) {
      if(entry.getGroupStatusId() != null && !groupsById.containsKey(entry.getGroupId())) {
        result.addUserGroupStatus(ImmutableOrgUserGroupStatus.builder()
            .groupId(entry.getGroupId())
            .status(entry.getGroupStatus())
            .statusId(entry.getGroupStatusId())
            .build());
      }
      
      
      groupsById.put(entry.getGroupId(), entry);
      if(entry.getGroupParentId() == null) {
        roots.add(entry);
        continue;
      }
      if(!groupsByParentId.containsKey(entry.getGroupParentId())) {
        groupsByParentId.put(entry.getGroupParentId(), new ArrayList<>());
      }
      groupsByParentId.get(entry.getGroupParentId()).add(entry);
    }
    
    for(final var role : roleData) {
      rolesById.put(role.getRoleId(), role);
      result.addRoleNames(role.getRoleName());
      result.addDirectRoleNames(role.getRoleName());
      
      if(role.getRoleStatusId() != null) {
        result.addUserRoleStatus(ImmutableOrgUserRoleStatus.builder()
            .roleId(role.getRoleId())
            .status(role.getRoleStatus())
            .statusId(role.getRoleStatusId())
            .build());
      }
    }
  }
  
  private void visitGroup(OrgGroupAndRoleFlattened entry) {
    visitTree(entry);
    visitResult(entry);
    visitChildren(entry);
  }
  
  private void visitTree(OrgGroupAndRoleFlattened node) {
    // create or get group
    final DefaultNode groupNode;
    if(groupNodes.containsKey(node.getGroupId())) {
      groupNode = groupNodes.get(node.getGroupId());
    } else {
      groupNode = new DefaultNode(node.getGroupName());
      groupNodes.put(node.getGroupId(), groupNode);
      
      // connect to parent
      if(node.getGroupParentId() != null) {
        groupNodes.get(node.getGroupParentId()).addChild(groupNode);
      }
      if(node.getGroupStatus() != null) {
        groupNode.addChild(new DefaultNode(node.getGroupStatus().name()));
      }
    }

    // create roles if present
    if(node.getRoleId() != null) {
      final DefaultNode rolesNode;
      if(groupRoleNodes.containsKey(node.getGroupId())) {
        rolesNode = groupRoleNodes.get(node.getGroupId());
      } else {
        rolesNode = new DefaultNode("roles");
        groupNode.addChild(rolesNode);
        groupRoleNodes.put(node.getGroupId(), rolesNode);        
      }
      rolesNode.addChild(new DefaultNode(node.getRoleName()));
    }
    
    // create membership
    if(node.getMembershipId() != null && !membershipNodes.containsKey(node.getMembershipId())) {
      final var membershipNode = new DefaultNode("direct-membership");
      membershipNodes.put(node.getMembershipId(), membershipNode);
      groupNode.addChild(membershipNode);
    }
  }
  
  private void visitResult(OrgGroupAndRoleFlattened node) {
    if(node.getGroupStatus() == OrgActorStatusType.REMOVED) {
      // disable UP - disable DOWN
    }
    
    // Add group
    if(!visitedGroupIds.contains(node.getGroupId())) {
      result.addGroupNames(node.getGroupName());
      visitedGroupIds.add(node.getGroupId());
    } 
    
    // Add role
    if(node.getRoleId() != null && !visitedRoleIds.contains(node.getRoleId())) {
      result.addRoleNames(node.getRoleName());
      visitedRoleIds.add(node.getRoleId());
    }
    
    // direct group and role from it
    if(node.getMembershipId() != null && !visitedMemberships.contains(node.getMembershipId())) {
      result.addDirectGroupNames(node.getGroupName());
      if(node.getRoleId() != null) {
        result.addDirectRoleNames(node.getRoleName());
      }
      visitedMemberships.add(node.getMembershipId());
    }
  }
  
  
  private void visitChildren(OrgGroupAndRoleFlattened entry) {
    final var children = groupsByParentId.get(entry.getGroupId());
    if(children == null) {
      return;
    }
    for(final var child : children.stream()
        .sorted((a, b) -> getSortableId(a).compareTo(getSortableId(b)))
        .toList()) {
      visitGroup(child);
    }
  }
  
  private String getSortableId(OrgGroupAndRoleFlattened entry) {
    return entry.getGroupName() + entry.getRoleName() + entry.getGroupStatus() + entry.getRoleStatus();
  }
  
  public OrgUserGroupsAndRolesWithLog build() {
    for(final var root : this.roots.stream()
        .sorted((a, b) -> a.getGroupName().compareTo(b.getGroupName()))
        .toList()) {
      
      visitGroup(root);
    }
    
    
    final var logTree = generateTree();
    return result.log(logTree)
        .groupNames(res.getGroupNames())
        .roleNames(res.getRoleNames())
        .directGroupNames(res.getDirectGroupNames())
        .directRoleNames(res.getDirectRoleNames())
        .build();
  }

  private String generateTree() {
    final var userNode = new DefaultNode(user.getUserName());
    for(final var root : this.roots) {
      final var rootNode = groupNodes.get(root.getGroupId());
      userNode.addChild(rootNode);
    }

    final var userRoles = new DefaultNode("roles");
    userNode.addChild(userRoles);
    
    final var directRoles = new DefaultNode("direct");
    final var inheritedRoles = new DefaultNode("inherited");
    userRoles.addChild(directRoles);
    userRoles.addChild(inheritedRoles);
    
    
    for(final var role : rolesById.values()
        .stream()
        .sorted((a, b) -> a.getRoleName().compareTo(b.getRoleName()))
        .toList()) {
      directRoles.addChild(new DefaultNode(role.getRoleName()));
    }
    
    for(final var role : groupsById.values().stream()
        .filter(role -> role.getRoleName() != null)
        .filter(entry -> entry.getGroupStatus() != OrgActorStatusType.REMOVED)
        .sorted((a, b) -> getSortableId(a).compareTo(getSortableId(b)))
        .toList()) {

      inheritedRoles.addChild(new DefaultNode(role.getRoleName()));
      
    }
    
    final var options = new TreeOptions();
    final var tree = TextTree.newInstance(options).render(userNode);

    if(log.isDebugEnabled()) {
      log.debug(System.lineSeparator() + tree); 
    }
    return tree;
  }
}
