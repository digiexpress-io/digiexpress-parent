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
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserGroupsAndRolesWithLog;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrgTreeBuilderForUser {

  private final Map<String, OrgGroupAndRoleFlattened> byGroupId = new HashMap<>();
  private final Map<String, List<OrgGroupAndRoleFlattened>> byParentGroupId = new HashMap<>();
  
  private final Map<String, DefaultNode> membershipNodes = new HashMap<>();
  private final Map<String, DefaultNode> groupNodes = new HashMap<>();
  private final Map<String, DefaultNode> groupRoleNodes = new HashMap<>();
  
  
  private final List<OrgGroupAndRoleFlattened> roots = new ArrayList<>();
  private final ImmutableOrgUserGroupsAndRolesWithLog.Builder result = ImmutableOrgUserGroupsAndRolesWithLog.builder();
  
  
  public OrgTreeBuilderForUser(List<OrgGroupAndRoleFlattened> groupData) {
    for(final var entry : groupData) {
      byGroupId.put(entry.getGroupId(), entry);
      if(entry.getGroupParentId() == null) {
        roots.add(entry);
        continue;
      }
      if(!byParentGroupId.containsKey(entry.getGroupParentId())) {
        byParentGroupId.put(entry.getGroupParentId(), new ArrayList<>());
      }
      byParentGroupId.get(entry.getGroupParentId()).add(entry);
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
      rolesNode.addChild(new DefaultNode(node.getRoleId()));
    }
    
    // create membership
    if(node.getMembershipId() != null && !membershipNodes.containsKey(node.getMembershipId())) {
      final var membershipNode = new DefaultNode("direct-membership");
      membershipNodes.put(node.getMembershipId(), membershipNode);
      groupNode.addChild(membershipNode);
    }
  }
  
  private void visitChildren(OrgGroupAndRoleFlattened entry) {
    final var children = byParentGroupId.get(entry.getGroupId());
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
      printTree(root);
    }
    return null;
  }
  
  private void printTree(OrgGroupAndRoleFlattened entry) {
    final var options = new TreeOptions();
    options.setStyle(TreeStyles.UNICODE_ROUNDED);
    options.setEnableDefaultColoring(true);
    final var rendered = TextTree.newInstance(options).render(groupNodes.get(entry.getGroupId()));
    log.error(System.lineSeparator() +
        "##############################" + System.lineSeparator() +
        rendered
        );
  }
}
