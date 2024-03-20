package io.resys.permission.client.spi;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.resys.permission.client.api.model.ImmutablePermission;
import io.resys.permission.client.api.model.ImmutablePrincipal;
import io.resys.permission.client.api.model.ImmutableRole;
import io.resys.permission.client.api.model.ImmutableRoleHierarchyContainer;
import io.resys.permission.client.api.model.RoleHierarchyContainer;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMembership;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgPartyRight;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRight;
import io.resys.thena.docdb.api.visitors.OrgPartyContainerVisitor;
import io.resys.thena.docdb.api.visitors.OrgTreeContainer.OrgAnyTreeContainerContext;
import io.resys.thena.docdb.api.visitors.OrgTreeContainer.OrgAnyTreeContainerVisitor;


public class RoleHierarchyQueryVisitor extends OrgPartyContainerVisitor<RoleHierarchyContainer> 
  implements OrgAnyTreeContainerVisitor<RoleHierarchyContainer> {
  
  private final String idOrNameOrExtId;
 
  private final Map<String, ImmutablePermission> permissions = new LinkedHashMap<>(); // permissions by name
  private final Map<String, ImmutablePrincipal> principals = new LinkedHashMap<>();   // principals by name
  private final Map<String, ImmutableRole> roles = new LinkedHashMap<>();   // principals by name
  private final ImmutableRoleHierarchyContainer.Builder result = ImmutableRoleHierarchyContainer.builder().log("");
  private String roleFoundId;  
  
  public RoleHierarchyQueryVisitor(String idOrNameOrExtId) {
    super(false, true);
    this.idOrNameOrExtId = idOrNameOrExtId;
  }
  
  @Override
  public RoleHierarchyContainer close() {
    return result.build();
  }
  
  private void visitRole(ImmutableRole.Builder role, OrgParty party, List<OrgParty> parents, List<OrgRight> parentRights) {
    role
    .id(party.getId())
    .name(party.getPartyName())
    .version(party.getCommitId())
    .description(party.getPartyDescription())
    .status(OrgActorStatusType.IN_FORCE)
    .parentId(party.getParentId())
    .addAllPermissions(parentRights.stream().map(r -> r.getRightName()).toList()); 
  }
  
  @Override
  protected TopPartyVisitor visitTop(OrgParty group, OrgAnyTreeContainerContext worldState) {
    final ImmutableRole.Builder role = ImmutableRole.builder();
    
    return new TopPartyVisitor() {
      @Override 
      public void visitPartyRight(OrgParty party, OrgPartyRight partyRight, OrgRight right, boolean isDisabled) {
        role.addPermissions(right.getRightName()); 
      }
      @Override
      public void visitMemberPartyRight(OrgParty party, OrgMemberRight memberRight, OrgRight right, boolean isDisabled) {
        //role.addPermissions(right.getRightName()); 
      }
      @Override
      public void visitMembershipWithInheritance(OrgParty group, OrgMembership membership, OrgMember user, boolean isDisabled) {
        role.addPrincipals(user.getUserName());
      }
      @Override
      public void visitMembership(OrgParty group, OrgMembership membership, OrgMember user, boolean isDisabled) {
        role.addPrincipals(user.getUserName()); // direct members
      }
      @Override
      public void visitChildParty(OrgParty party, boolean isDisabled) {
        
      }
      
      @Override
      public void start(OrgParty party, List<OrgParty> parents, List<OrgRight> parentRights, boolean isDisabled) {
        if(party.isMatch(idOrNameOrExtId)) {
          roleFoundId = party.getId();  
        }
        visitRole(role, party, parents, parentRights);
      }
      @Override
      public void visitLog(String log) {
        if(roleFoundId != null) {
          result.log(log);
        }
      }      
      @Override
      public void end(OrgParty group, List<OrgParty> parents, boolean isDisabled) {
        final var completedRole = role.build();
        roles.put(completedRole.getId(), completedRole);
        
        if(roleFoundId != null) {
          
          result
            .rootRoleId(group.getId())
            .targetRoleId(roleFoundId)
            .putAllPrincipals(principals)
            .putAllPermissions(permissions)
            .putAllRoles(roles);
          roleFoundId = null;  
        }
        permissions.clear();
        principals.clear();
        roles.clear();
      }

    };
  }
  
  @Override
  protected PartyVisitor visitChild(OrgParty group, OrgAnyTreeContainerContext worldState) {
   final ImmutableRole.Builder role = ImmutableRole.builder();
    
    return new PartyVisitor() {
      @Override 
      public void visitPartyRight(OrgParty party, OrgPartyRight partyRight, OrgRight right, boolean isDisabled) {
        role.addPermissions(right.getRightName()); 
      }
      @Override
      public void visitMemberPartyRight(OrgParty party, OrgMemberRight memberRight, OrgRight right, boolean isDisabled) {
        //role.addPermissions(right.getRightName()); 
      }
      @Override
      public void visitMembershipWithInheritance(OrgParty group, OrgMembership membership, OrgMember user, boolean isDisabled) {
        role.addPrincipals(user.getUserName());
      }
      @Override
      public void visitMembership(OrgParty group, OrgMembership membership, OrgMember user, boolean isDisabled) {
        role.addPrincipals(user.getUserName());
      }
      @Override
      public void visitChildParty(OrgParty party, boolean isDisabled) {
      }
      @Override
      public void start(OrgParty party, List<OrgParty> parents, List<OrgRight> parentRights, boolean isDisabled) {
        if(party.isMatch(idOrNameOrExtId)) {
          roleFoundId = party.getId();  
        }
        visitRole(role, party, parents, parentRights);
      }
      
      @Override
      public void end(OrgParty group, List<OrgParty> parents, boolean isDisabled) {
        final var completedRole = role.build();
        roles.put(completedRole.getId(), completedRole);
      }
    };
  }
}
