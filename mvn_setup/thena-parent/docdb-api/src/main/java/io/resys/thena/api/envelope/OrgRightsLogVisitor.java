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



public class OrgRightsLogVisitor extends OrgPartyContainerVisitor<String> 
  implements OrgAnyTreeContainerVisitor<String>, TopPartyLogger {

  private final Map<String, DefaultNode> nodesGroup = new HashMap<>();
  private final Map<String, DefaultNode> nodesGroupMembers = new HashMap<>();
  private final OrgRight target;
  
  private OrgAnyTreeContainerContext ctx;
  private DefaultNode nodeRoot;
  private Boolean groupContainsRole;
  
  public OrgRightsLogVisitor(OrgRight target) {
    super(true);
    this.target = target;
  }
  public void start(OrgAnyTreeContainerContext ctx) {
    this.ctx = ctx;
    super.start(ctx);
    
  }
  @Override
  protected TopPartyVisitor visitTop(OrgParty group, OrgAnyTreeContainerContext worldState) {
    nodeRoot = new DefaultNode(this.target.getRightName());
    groupContainsRole = null;
    nodesGroup.clear();
    nodesGroupMembers.clear();
    return this;
  }

  @Override
  protected PartyVisitor visitChild(OrgParty group, OrgAnyTreeContainerContext worldState) {
    return this;
  }
  
  @Override
  public String close() {
    if(nodeRoot.getChildren().isEmpty()) {
      return "";
    }
    final var options = new TreeOptions();
    final var tree = TextTree.newInstance(options).render(nodeRoot);
    return tree;
  }
  
  @Override
  public void start(OrgParty group, List<OrgParty> parents, List<OrgRight> parentRights, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    
    if(groupContainsRole == null) {
      return;
    }
    

    final var groupNode = new DefaultNode(group.getPartyName());
    nodesGroup.put(group.getId(), groupNode);
    nodesGroup.get(group.getParentId()).addChild(groupNode);
  }

  @Override
  public void visitDirectMembership(OrgParty group, OrgMembership membership, OrgMember user, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    
    if(groupContainsRole == null) {
      return;
    }
    
    final var disabledSpecificallyForUser = ctx.getMemberRights(user.getId()).stream()
        .filter(role -> role.getRightId().equals(target.getId()))
        .filter(role -> ctx.isStatusDisabled(ctx.getStatus(role)))
        .findAny().isPresent();
    
    if(disabledSpecificallyForUser) {
      return;
    }
    
    
    nodesGroup.get(group.getId()).addChild(new DefaultNode(user.getUserName()));
  }

  @Override
  public void visitMembershipWithInheritance(OrgParty group, OrgMembership membership, OrgMember user,
      boolean isDisabled) {
    
  }

  @Override
  public void visitDirectPartyRight(List<OrgParty> _parents, OrgParty group, OrgPartyRight groupRole, OrgRight role, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    
    if(groupContainsRole != null) {
      return;
    }
    
    if(this.target.getId().equals(groupRole.getRightId())) {
      groupContainsRole = true;
      
      final var groupNode = new DefaultNode(group.getPartyName() + " <= direct role");
      nodesGroup.put(group.getId(), groupNode);
      nodeRoot.addChild(groupNode);
    }
  }
  
  @Override
  public void visitDirectMemberPartyRight(OrgParty group, OrgMemberRight groupRole, OrgRight role, boolean isDisabled) {
    if(isDisabled) {
      return;
    }
    if(groupContainsRole != null) {
      return;
    }
    if(this.target.getId().equals(groupRole.getRightId())) {
      groupContainsRole = true;
      
      final var groupNode = new DefaultNode(group.getPartyName() + " <= inherited role");
      nodesGroup.put(group.getId(), groupNode);
      nodeRoot.addChild(groupNode);
    }
  }

  @Override
  public void visitChildParty(OrgParty group, boolean isDisabled) {
    
  }

  @Override
  public void end(OrgParty group, List<OrgParty> parents, boolean isDisabled) {
    groupContainsRole = null;
  }

  @Override
  public TopPartyLogger visitLogger(OrgParty party) {
    return null;
  }

}
