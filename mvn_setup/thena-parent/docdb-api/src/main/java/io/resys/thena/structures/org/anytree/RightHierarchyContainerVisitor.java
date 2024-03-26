package io.resys.thena.structures.org.anytree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.barfuin.texttree.api.DefaultNode;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;

import io.resys.thena.api.entities.org.ImmutableOrgRightHierarchy;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMember;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMemberRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgMembership;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgParty;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgRight;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgRightHierarchy;
import io.resys.thena.api.visitors.OrgPartyContainerVisitor;
import io.resys.thena.api.visitors.OrgTreeContainer.OrgAnyTreeContainerContext;
import io.resys.thena.api.visitors.OrgTreeContainer.OrgAnyTreeContainerVisitor;
import lombok.RequiredArgsConstructor;



public class RightHierarchyContainerVisitor extends OrgPartyContainerVisitor<OrgRightHierarchy> 
  implements OrgAnyTreeContainerVisitor<OrgRightHierarchy> {
  
  private final String roleIdOrNameOrExternalId;
  private final ImmutableOrgRightHierarchy.Builder builder = ImmutableOrgRightHierarchy.builder();
  private final Map<String, GroupVisitorForRole> visitorsByGroup = new HashMap<>();
  
  private OrgAnyTreeContainerContext ctx;
  private TopPartyVisitor currentVisitor;
  private OrgRight target;
  private DefaultNode nodeRoot;
  
  public RightHierarchyContainerVisitor(String roleIdOrNameOrExternalId) {
    super(true);
    this.roleIdOrNameOrExternalId = roleIdOrNameOrExternalId;
  }
  public void start(OrgAnyTreeContainerContext ctx) {
    
    final var target = ctx.getRights().stream()
    .filter(role -> {
      return roleIdOrNameOrExternalId.equals(role.getExternalId()) ||
          roleIdOrNameOrExternalId.equals(role.getRightName()) ||
          roleIdOrNameOrExternalId.equals(role.getId());
    }).findAny();
    
    if(target.isEmpty()) {
      final var msg = new StringBuilder("Can't build role hierarchy!").append(System.lineSeparator()).append("Can't find the role!").append(System.lineSeparator())
          .append("  - tried to match by, id or name or external id: ").append(roleIdOrNameOrExternalId).append(System.lineSeparator())
          .append("  - known roles: ").append(System.lineSeparator());
      
      for(final var role : ctx.getRights()) {
        msg
          .append("    - existing role ").append(System.lineSeparator())
          .append("      id: ").append(role.getId()).append(System.lineSeparator())
          .append("      name: ").append(role.getRightName()).append(System.lineSeparator())
          .append("      external id: ").append(role.getExternalId()).append(System.lineSeparator());
      }
      
      throw new RoleNotFoundForHierarchyException(msg.toString());
    }
    this.target = target.get();
    this.ctx = ctx;
    this.nodeRoot = new DefaultNode(this.target.getRightName());
    super.start(ctx);
    
  }
  
  @Override
  public OrgRightHierarchy close() {
    final var options = new TreeOptions();
    final var tree = TextTree.newInstance(options).render(nodeRoot);

    
    return builder
        .status(ctx.getStatus(target).map(status -> status.getValue()).orElse(OrgActorStatusType.IN_FORCE))
        .log(tree)
        .roleId(target.getId())
        .commitId(target.getCommitId())
        .externalId(target.getExternalId())
        .roleName(target.getRightName())
        .roleDescription(target.getRightDescription())
        .build();
  }

  @Override
  protected TopPartyVisitor visitTop(OrgParty group, OrgAnyTreeContainerContext worldState) {
    final var currentVisitor = new GroupVisitorForRole(target, nodeRoot, ctx);
    this.visitorsByGroup.put(group.getId(), currentVisitor);
    this.currentVisitor = currentVisitor;
    return this.currentVisitor;
  }

  @Override
  protected PartyVisitor visitChild(OrgParty group, OrgAnyTreeContainerContext worldState) {
    return this.currentVisitor;
  }
  
  @RequiredArgsConstructor
  private static class GroupVisitorForRole implements TopPartyVisitor {
    private final OrgRight target;
    private final DefaultNode nodeRoot;
    private final OrgAnyTreeContainerContext ctx;
    
    private Boolean groupContainsRole;
    private final Map<String, DefaultNode> nodesGroup = new HashMap<>();
    private final Map<String, DefaultNode> nodesGroupMembers = new HashMap<>();
    
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
    public void visitMembership(OrgParty group, OrgMembership membership, OrgMember user, boolean isDisabled) {
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
    public void visitPartyRight(OrgParty group, OrgPartyRight groupRole, OrgRight role, boolean isDisabled) {
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
    public void visitMemberPartyRight(OrgParty group, OrgMemberRight groupRole, OrgRight role, boolean isDisabled) {
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
    public void visitLog(String log) {
      
    }
  }
  
  public static class RoleNotFoundForHierarchyException extends RuntimeException {
    private static final long serialVersionUID = 88546975164711743L;
    public RoleNotFoundForHierarchyException(String message) {
      super(message);
    }   
  }
}
