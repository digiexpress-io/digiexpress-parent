package io.resys.thena.docdb.models.org.anytree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.barfuin.texttree.api.DefaultNode;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;

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



public class GroupHierarchyContainerLogVisitor extends OrgGroupContainerVisitor<String> 
  implements OrgAnyTreeContainerVisitor<String>, GroupVisitor {
  
  private final String groupIdOrNameOrExternalId;
  private final DefaultNode nodeRoot = new DefaultNode("organization");
  
  private final Map<String, DefaultNode> nodesGroup = new HashMap<>();
  private final Map<String, DefaultNode> nodesGroupUsers = new HashMap<>();
  private final Map<String, DefaultNode> nodesGroupRoles = new HashMap<>();
  private final Map<String, DefaultNode> nodesGroupMembers = new HashMap<>();
  
  private String foundGroupId;
  private DefaultNode nodeToLog;
  
  
  public GroupHierarchyContainerLogVisitor(String groupIdOrNameOrExternalId, boolean includeDisabled) {
    super(includeDisabled);
    this.groupIdOrNameOrExternalId = groupIdOrNameOrExternalId;
  }

  @Override
  public void visitMembershipWithInheritance(OrgGroup group, OrgUserMembership membership, OrgUser user, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
  }
  @Override
  public void visitMembership(OrgGroup group, OrgUserMembership membership, OrgUser user, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    
    if(!nodesGroupMembers.containsKey(group.getId())) {
      final var users = new DefaultNode("users");
      nodesGroup.get(group.getId()).addChild(users);
      nodesGroupMembers.put(group.getId(), users);
    }
    
    final var nodeUser = new DefaultNode(user.getUserName());
    nodesGroupMembers.get(group.getId()).addChild(nodeUser);
    nodesGroupUsers.put(group.getId() + user.getId(), nodeUser);
    
  }
  @Override
  public void visitRole(OrgGroup group, OrgGroupRole groupRole, OrgRole role, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    if(!nodesGroupRoles.containsKey(group.getId())) {
      final var roles = new DefaultNode("roles");
      nodesGroup.get(group.getId()).addChild(roles);
      nodesGroupRoles.put(group.getId(), roles);
    }
    nodesGroupRoles.get(group.getId()).addChild(new DefaultNode(role.getRoleName()));
  }
  @Override
  public void visitRole(OrgGroup group, OrgUserRole groupRole, OrgRole role, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    
    final var userNode = nodesGroupUsers.get(group.getId() + groupRole.getUserId());
    if(userNode.getText().endsWith(")")) {
      userNode.setText(userNode.getText().substring(0, userNode.getText().length() -2) + ", " + role.getRoleName() + ")");            
    } else {
      userNode.setText(userNode.getText() + " (" + role.getRoleName() + ")");      
    }
  }
  
  @Override
  public void visitChild(OrgGroup group, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
  }
  @Override
  public void start(OrgGroup group, List<OrgGroup> parents, boolean isDisabled) {
    final var previousNode = group.getParentId() == null ? nodeRoot : nodesGroup.get(group.getParentId());    

    
    if( groupIdOrNameOrExternalId.equals(group.getExternalId()) ||
        groupIdOrNameOrExternalId.equals(group.getGroupName()) ||
        groupIdOrNameOrExternalId.equals(group.getId())) {
      
      final var groupNode = new DefaultNode(group.getGroupName() + " <= you are here");
      previousNode.addChild(groupNode);
      nodesGroup.put(group.getId(), groupNode);
      
      nodeToLog = parents.isEmpty() ? groupNode : nodesGroup.get(parents.iterator().next().getId());
      foundGroupId = group.getId();
    } else {
      final var groupNode = new DefaultNode(group.getGroupName());
      previousNode.addChild(groupNode);
      nodesGroup.put(group.getId(), groupNode);
    }
  }
  @Override
  public void end(OrgGroup group, List<OrgGroup> parents, boolean isDisabled) {
    if(group.getId().equals(foundGroupId)) {
      foundGroupId = null;
    }
  }
  
  @Override
  public String close() {
    final var options = new TreeOptions();
    final var tree = TextTree.newInstance(options).render(nodeToLog);

    return tree;
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
