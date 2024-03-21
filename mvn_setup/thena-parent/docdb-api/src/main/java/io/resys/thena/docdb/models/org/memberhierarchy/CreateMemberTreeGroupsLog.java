package io.resys.thena.docdb.models.org.memberhierarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.barfuin.texttree.api.DefaultNode;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRightFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberFlattened;



public class CreateMemberTreeGroupsLog extends BottomUpVisitorTemplate<String> {

  private final DefaultNode userNode;
  private final DefaultNode userRoles = new DefaultNode("roles");
  private final DefaultNode greyRoles = new DefaultNode("grey-roles");
  private final DefaultNode greyGroups = new DefaultNode("grey-groups");
  private final Map<String, List<DefaultNode>> groupsByParent = new HashMap<>();
  private final Map<String, DefaultNode> rolesByGroup = new HashMap<>();
  
  
  public CreateMemberTreeGroupsLog(List<OrgRightFlattened> globalRoles, OrgMemberFlattened user) {
    super(globalRoles);
    this.userNode = new DefaultNode(user.getUserName());
  } 
  @Override
  public String close() {
    if(!userRoles.getChildren().isEmpty()) {
      this.userNode.addChild(userRoles);
    }
    if(!greyRoles.getChildren().isEmpty()) {
      this.userNode.addChild(greyRoles);
    }
    
    if(!greyGroups.getChildren().isEmpty()) {
      this.userNode.addChild(greyGroups);
    }
    final var options = new TreeOptions();
    final var tree = TextTree.newInstance(options).render(userNode);
    return tree;    
  }
  @Override
  public void visitGlobalRoleEnabled(String roleName) {
    userRoles.addChild(new DefaultNode(roleName));
  }
  @Override
  public void visitGroupEnabled(String groupName, Optional<String> parentGroupName, boolean isDirect) {
    final var groupNode = new DefaultNode(groupName + (isDirect ? "::DIRECT" : ""));
    
    final var rolesNode = new DefaultNode("roles");
    groupNode.addChild(rolesNode);
    rolesByGroup.put(groupName, rolesNode);
  
    
    if(this.groupsByParent.containsKey(groupName)) {
      this.groupsByParent.get(groupName).forEach(node -> groupNode.addChild(node));
    }
    
    // root
    if(parentGroupName.isEmpty()) {
      userNode.addChild(groupNode);
      return;
    } 

    // children
    if(!this.groupsByParent.containsKey(parentGroupName.get())) {
      this.groupsByParent.put(parentGroupName.get(), new ArrayList<>());
    }
    this.groupsByParent.get(parentGroupName.get()).add(groupNode);
  }

  @Override
  public void visitRoleEnabled(String roleName, String groupName, Optional<String> parentGroupName, boolean isDirect) {
    if(isDirect) {
      rolesByGroup.get(groupName).addChild(new DefaultNode(roleName));
    }
  }

  @Override
  public void visitGroupDisabled(String groupName, Optional<String> parentGroupName, boolean isDirect) {
    if(isDirect) {
      greyGroups.addChild(new DefaultNode(groupName));
    }
  }
  @Override
  public void visitRoleDisabled(String roleName, String groupName, Optional<String> parentGroupName, boolean isDirect) {
    if(isDirect) {
      greyRoles.addChild(new DefaultNode(roleName));
    }
  }
  @Override
  public void visitGlobalRoleDisabled(String roleName) {
    greyRoles.addChild(new DefaultNode(roleName));
  }
}
