package io.resys.thena.docdb.models.org.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.barfuin.texttree.api.DefaultNode;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;
import org.barfuin.texttree.api.style.TreeStyles;

import io.resys.thena.docdb.api.models.ImmutableOrgUserGroupsAndRolesWithLog;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroupAndRoleFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRoleFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserGroupsAndRolesWithLog;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrgTreeBuilderForUser {

  private final Map<String, OrgGroupAndRoleFlattened> groupsById = new HashMap<>();
  private final Map<String, List<OrgGroupAndRoleFlattened>> groupsByParentId = new HashMap<>();
  private final Map<String, OrgRoleFlattened> rolesById = new HashMap<>();
  
  
  private final Map<String, DefaultNode> membershipNodes = new HashMap<>();
  private final Map<String, DefaultNode> groupNodes = new HashMap<>();
  private final Map<String, DefaultNode> groupRoleNodes = new HashMap<>();
  
  
  private final List<OrgGroupAndRoleFlattened> roots = new ArrayList<>();
  private final OrgUserFlattened user;
  private final ImmutableOrgUserGroupsAndRolesWithLog.Builder result = ImmutableOrgUserGroupsAndRolesWithLog.builder();
  
  
  public OrgTreeBuilderForUser(OrgUserFlattened user, List<OrgGroupAndRoleFlattened> groupData, List<OrgRoleFlattened> roleData) {
    this.user = user;
    
    for(final var entry : groupData) {
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
    
    for(final var entry : roleData) {
      rolesById.put(entry.getRoleId(), entry);
    }
  }
  
  private void visitGroup(OrgGroupAndRoleFlattened entry) {
    visitTree(entry);
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
  
  private void visitChildren(OrgGroupAndRoleFlattened entry) {
    final var children = groupsByParentId.get(entry.getGroupId());
    if(children == null) {
      return;
    }
    for(final var child : children) {
      visitGroup(child);
    }
  }
  
  public OrgUserGroupsAndRolesWithLog build() {
    for(final var root : this.roots) {
      visitGroup(root);
    }
    generateTree();
    
    return null;
  }

  private String generateTree() {
    final var userNode = new DefaultNode(user.getUserName());
    for(final var root : this.roots) {
      userNode.addChild(groupNodes.get(root.getGroupId()));
    }

    final var userRoles = new DefaultNode("roles");
    userNode.addChild(userRoles);
    
    final var directRoles = new DefaultNode("direct");
    final var inheritedRoles = new DefaultNode("inherited");
    userRoles.addChild(directRoles);
    userRoles.addChild(inheritedRoles);
    
    
    for(final var role : rolesById.values()) {
      directRoles.addChild(new DefaultNode(role.getRoleName()));
    }
    
    for(final var role : groupsById.values()) {
      if(role.getRoleName() != null) {
        inheritedRoles.addChild(new DefaultNode(role.getRoleName()));
      }
    }
    

    final var options = new TreeOptions();
    options.setStyle(TreeStyles.UNICODE_ROUNDED);
    options.setEnableDefaultColoring(true);
    final var tree = TextTree.newInstance(options).render(userNode);

    log.error(
      System.lineSeparator() +
      "##############################" + System.lineSeparator() +
      tree
    );  
    return tree;
  }
}
