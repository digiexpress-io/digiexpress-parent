package io.resys.thena.docdb.models.org.anytree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.barfuin.texttree.api.DefaultNode;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;

import io.resys.thena.docdb.api.models.ImmutableOrgRoleHierarchy;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroupRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgRoleHierarchy;
import io.resys.thena.docdb.models.org.anytree.AnyTreeContainer.AnyTreeContainerContext;
import io.resys.thena.docdb.models.org.anytree.AnyTreeContainer.AnyTreeContainerVisitor;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class RoleHierarchyContainerVisitor extends AnyTreeContainerVisitorImpl<OrgRoleHierarchy> 
  implements AnyTreeContainerVisitor<OrgRoleHierarchy> {
  
  private final String roleIdOrNameOrExternalId;
  private final ImmutableOrgRoleHierarchy.Builder builder = ImmutableOrgRoleHierarchy.builder();
  private final Map<String, GroupVisitorForRole> visitorsByGroup = new HashMap<>();
  
  private AnyTreeContainerContext ctx;
  private GroupVisitor currentVisitor;
  private OrgRole target;
  private DefaultNode nodeRoot;
  
  public void start(AnyTreeContainerContext ctx) {
    
    final var target = ctx.getRoles().stream()
    .filter(role -> {
      return roleIdOrNameOrExternalId.equals(role.getExternalId()) ||
          roleIdOrNameOrExternalId.equals(role.getRoleName()) ||
          roleIdOrNameOrExternalId.equals(role.getId());
    }).findAny();
    
    if(target.isEmpty()) {
      final var msg = new StringBuilder("Can't build role hierarchy!").append(System.lineSeparator()).append("Can't find the role!").append(System.lineSeparator())
          .append("  - tried to match by, id or name or external id: ").append(roleIdOrNameOrExternalId).append(System.lineSeparator())
          .append("  - known roles: ").append(System.lineSeparator());
      
      for(final var role : ctx.getRoles()) {
        msg
          .append("    - existing role ").append(System.lineSeparator())
          .append("      id: ").append(role.getId()).append(System.lineSeparator())
          .append("      name: ").append(role.getRoleName()).append(System.lineSeparator())
          .append("      external id: ").append(role.getExternalId()).append(System.lineSeparator());
      }
      
      throw new RoleNotFoundForHierarchyException(msg.toString());
    }
    this.target = target.get();
    this.ctx = ctx;
    this.nodeRoot = new DefaultNode(this.target.getRoleName());
    super.start(ctx);
    
  }
  
  @Override
  public OrgRoleHierarchy close() {
    final var options = new TreeOptions();
    final var tree = TextTree.newInstance(options).render(nodeRoot);

    
    return builder
        .status(ctx.getStatus(target).map(status -> status.getValue()).orElse(OrgActorStatusType.IN_FORCE))
        .log(tree)
        .roleId(target.getId())
        .commitId(target.getCommitId())
        .externalId(target.getExternalId())
        .roleName(target.getRoleName())
        .roleDescription(target.getRoleDescription())
        .build();
  }

  @Override
  GroupVisitor visitTop(OrgGroup group, AnyTreeContainerContext worldState) {
    final var currentVisitor = new GroupVisitorForRole(target, nodeRoot, ctx);
    this.visitorsByGroup.put(group.getId(), currentVisitor);
    this.currentVisitor = currentVisitor;
    return this.currentVisitor;
  }

  @Override
  GroupVisitor visitChild(OrgGroup group, AnyTreeContainerContext worldState) {
    return this.currentVisitor;
  }
  
  @RequiredArgsConstructor
  private static class GroupVisitorForRole implements GroupVisitor {
    private final OrgRole target;
    private final DefaultNode nodeRoot;
    private final AnyTreeContainerContext ctx;
    
    private Boolean groupContainsRole;
    private final Map<String, DefaultNode> nodesGroup = new HashMap<>();
    private final Map<String, DefaultNode> nodesGroupMembers = new HashMap<>();
    
    @Override
    public void start(OrgGroup group, List<OrgGroup> parents, boolean isDisabled) {
      if(isDisabled) {
        return;
      }
      
      if(groupContainsRole == null) {
        return;
      }
      

      final var groupNode = new DefaultNode(group.getGroupName());
      nodesGroup.put(group.getId(), groupNode);
      nodesGroup.get(group.getParentId()).addChild(groupNode);
    }

    @Override
    public void visitMembership(OrgGroup group, OrgUserMembership membership, OrgUser user, boolean isDisabled) {
      if(isDisabled) {
        return;
      }
      
      if(groupContainsRole == null) {
        return;
      }
      
      final var disabledSpecificallyForUser = ctx.getUserRoles(user.getId()).stream()
          .filter(role -> role.getRoleId().equals(target.getId()))
          .filter(role -> ctx.isStatusDisabled(ctx.getStatus(role)))
          .findAny().isPresent();
      
      if(disabledSpecificallyForUser) {
        return;
      }
      
      
      nodesGroup.get(group.getId()).addChild(new DefaultNode(user.getUserName()));
    }

    @Override
    public void visitMembershipWithInheritance(OrgGroup group, OrgUserMembership membership, OrgUser user,
        boolean isDisabled) {
      
    }

    @Override
    public void visitRole(OrgGroup group, OrgGroupRole groupRole, OrgRole role, boolean isDisabled) {
      if(isDisabled) {
        return;
      }
      
      if(groupContainsRole != null) {
        return;
      }
      
      if(this.target.getId().equals(groupRole.getRoleId())) {
        groupContainsRole = true;
        
        final var groupNode = new DefaultNode(group.getGroupName() + " <= direct role");
        nodesGroup.put(group.getId(), groupNode);
        nodeRoot.addChild(groupNode);
      }
    }

    @Override
    public void visitChild(OrgGroup group, boolean isDisabled) {
      
    }

    @Override
    public void end(OrgGroup group, List<OrgGroup> parents, boolean isDisabled) {
      groupContainsRole = null;
    }
  }
  
  public static class RoleNotFoundForHierarchyException extends RuntimeException {
    private static final long serialVersionUID = 88546975164711743L;
    public RoleNotFoundForHierarchyException(String message) {
      super(message);
    }   
  }
}