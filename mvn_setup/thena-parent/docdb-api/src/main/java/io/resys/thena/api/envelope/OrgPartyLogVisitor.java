package io.resys.thena.api.envelope;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.barfuin.texttree.api.DefaultNode;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;

import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.envelope.OrgPartyContainerVisitor.TopPartyLogger;
import io.resys.thena.api.envelope.OrgTreeContainer.OrgAnyTreeContainerContext;
import io.resys.thena.api.envelope.OrgTreeContainer.OrgAnyTreeContainerVisitor;



public class OrgPartyLogVisitor extends OrgPartyContainerVisitor<String> 
  implements OrgAnyTreeContainerVisitor<String>, TopPartyLogger {
  
  private final String groupIdOrNameOrExternalId;
  private final DefaultNode nodeRoot = new DefaultNode("organization");
  
  private final Map<String, DefaultNode> nodesGroup = new HashMap<>();
  private final Map<String, DefaultNode> nodesGroupUsers = new HashMap<>();
  private final Map<String, DefaultNode> nodesGroupRoles = new HashMap<>();
  private final Map<String, DefaultNode> nodesGroupMembers = new HashMap<>();
  
  private String foundGroupId;
  private DefaultNode nodeToLog;
  
  
  public OrgPartyLogVisitor(String groupIdOrNameOrExternalId, boolean includeDisabled) {
    super(includeDisabled);
    this.groupIdOrNameOrExternalId = groupIdOrNameOrExternalId;
  }

  @Override
  public void visitInheritedMembership(OrgParty group, OrgMembership membership, OrgMember user, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    
    if(!nodesGroupMembers.containsKey(group.getId())) {
      final var users = new DefaultNode("users");
      nodesGroup.get(group.getId()).addChild(users);
      nodesGroupMembers.put(group.getId(), users);
    }
    final var userNodeId = group.getId() + user.getId();
    if(!nodesGroupUsers.containsKey(userNodeId)) {
      final var nodeUser = new DefaultNode(user.getUserName() + "::inherited");
      nodesGroupMembers.get(group.getId()).addChild(nodeUser);
      nodesGroupUsers.put(userNodeId, nodeUser);
    }
  }
  @Override
  public void visitDirectMembership(OrgParty group, OrgMembership membership, OrgMember user, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    
    if(!nodesGroupMembers.containsKey(group.getId())) {
      final var users = new DefaultNode("users");
      nodesGroup.get(group.getId()).addChild(users);
      nodesGroupMembers.put(group.getId(), users);
    }
    final var userNodeId = group.getId() + user.getId();
    if(!nodesGroupUsers.containsKey(userNodeId)) {
      final var nodeUser = new DefaultNode(user.getUserName());
      nodesGroupMembers.get(group.getId()).addChild(nodeUser);
      nodesGroupUsers.put(group.getId() + user.getId(), nodeUser);
    }
    
  }
  @Override
  public void visitPartyRight(OrgParty group, OrgPartyRight groupRole, OrgRight role, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    if(!nodesGroupRoles.containsKey(group.getId())) {
      final var roles = new DefaultNode("roles");
      nodesGroup.get(group.getId()).addChild(roles);
      nodesGroupRoles.put(group.getId(), roles);
    }
    nodesGroupRoles.get(group.getId()).addChild(new DefaultNode(role.getRightName()));
  }
  @Override
  public void visitMemberRight(OrgParty party, OrgMemberRight memberRight, OrgRight right, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    final var isDirect = memberRight.getPartyId().equals(party.getId());
    final var nodeId = party.getId() + memberRight.getMemberId();
    final DefaultNode userNode = nodesGroupUsers.get(nodeId);
    final var rightName = right.getRightName() + (isDirect ? "" : "::inherited");
    
    if(userNode.getText().endsWith(")")) {
      userNode.setText(userNode.getText().substring(0, userNode.getText().length() -1) + ", " + rightName + ")");            
    } else {
      userNode.setText(userNode.getText() + " (" + rightName + ")");
    }
  }
  
  @Override
  public void visitChildParty(OrgParty group, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
  }
  @Override
  public void start(OrgParty group, List<OrgParty> parents, List<OrgRight> parentRights, boolean isDisabled) {
    final var previousNode = group.getParentId() == null ? nodeRoot : nodesGroup.get(group.getParentId());    

    
    if( group.isMatch(groupIdOrNameOrExternalId) ) {
      
      final var groupNode = new DefaultNode(group.getPartyName() + " <= you are here");
      previousNode.addChild(groupNode);
      nodesGroup.put(group.getId(), groupNode);
      
      nodeToLog = parents.isEmpty() ? groupNode : nodesGroup.get(parents.iterator().next().getId());
      foundGroupId = group.getId();
    } else {
      final var groupNode = new DefaultNode(group.getPartyName());
      previousNode.addChild(groupNode);
      nodesGroup.put(group.getId(), groupNode);
    }
  }
  @Override
  public void end(OrgParty group, List<OrgParty> parents, boolean isDisabled) {
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
  protected TopPartyVisitor visitTop(OrgParty group, OrgAnyTreeContainerContext worldState) {
    return this;
  }
  @Override
  protected PartyVisitor visitChild(OrgParty group, OrgAnyTreeContainerContext worldState) {
    return this;
  }

  @Override
  public TopPartyLogger visitLogger(OrgParty party) {
    return null;
  }

}
