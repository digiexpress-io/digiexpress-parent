package io.resys.thena.docdb.models.org.anytree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.barfuin.texttree.api.DefaultNode;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;

import io.resys.thena.docdb.api.models.ImmutableOrgGroupHierarchy;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroupRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgGroupHierarchy;
import io.resys.thena.docdb.models.org.anytree.AnyTreeContainer.AnyTreeContainerContext;
import io.resys.thena.docdb.models.org.anytree.AnyTreeContainer.AnyTreeContainerVisitor;
import io.resys.thena.docdb.models.org.anytree.AnyTreeContainerVisitorImpl.GroupVisitor;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GroupHierarchyContainerVisitor extends AnyTreeContainerVisitorImpl<OrgGroupHierarchy> 
  implements AnyTreeContainerVisitor<OrgGroupHierarchy>, GroupVisitor {
  
  private final String groupIdOrNameOrExternalId;
  private final ImmutableOrgGroupHierarchy.Builder builder = ImmutableOrgGroupHierarchy.builder();
  private final DefaultNode nodeRoot = new DefaultNode("organization");
  
  private final Map<String, DefaultNode> nodesGroup = new HashMap<>();
  private final Map<String, DefaultNode> nodesGroupRoles = new HashMap<>();
  private final Map<String, DefaultNode> nodesGroupMembers = new HashMap<>();
  
  private String foundGroupId;
  private DefaultNode nodeToLog;
  
  private boolean isDirectGroup(OrgGroup group) {
    if(foundGroupId != null) {
      return group.getId().equals(foundGroupId);
    }
    return false;  
  }
  
  @Override
  public void visitMembershipWithInheritance(OrgGroup group, OrgUserMembership membership, OrgUser user, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    if(foundGroupId == null) {
      return;
    }
    if(isDirectGroup(group)) {
      builder.addParenUsers(user);
    } else {
      builder.addChildUsers(user);
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
    nodesGroupMembers.get(group.getId()).addChild(new DefaultNode(user.getUserName()));
    
    
    if(!isDirectGroup(group)) {
      return;
    }
    builder.addDirectUsers(user);
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
    
    
    if(foundGroupId == null) {
      return;
    }
    builder.addDirectRoleNames(role);    
  }
  @Override
  public void visitChild(OrgGroup group, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    
    if(foundGroupId == null) {
      return;
    }
    builder.addChildGroups(group);
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
      builder
        .groupId(group.getId())
        .groupName(group.getGroupName())
        .externalId(group.getExternalId())
        .commitId(group.getCommitId())
        .parentGroupId(group.getParentId())
        .parentGroups(parents)
        .status(isDisabled ? OrgActorStatusType.DISABLED : OrgActorStatusType.IN_FORCE);
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
  public OrgGroupHierarchy close() {
    final var options = new TreeOptions();
    final var tree = TextTree.newInstance(options).render(nodeToLog);

    return builder.log(tree).build();
  }
  @Override
  GroupVisitor visitTop(OrgGroup group, AnyTreeContainerContext worldState) {
    return this;
  }
  @Override
  GroupVisitor visitChild(OrgGroup group, AnyTreeContainerContext worldState) {
    return this;
  }
  
}
