package io.resys.permission.client.spi;

import java.util.List;

import io.resys.permission.client.api.PermissionClient.RoleAccessEvaluator;
import io.resys.permission.client.api.PermissionClient.RoleNotFoundException;
import io.resys.permission.client.api.PermissionClient.RoleQuery;
import io.resys.permission.client.api.model.ImmutableRole;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgPartyHierarchy;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RoleQueryImpl implements RoleQuery {
  
  private final PermissionStore ctx;

  @Override
  public Uni<Role> get(String roleId) {
    
    
    final var repoId = ctx.getConfig().getRepoId();
    final Uni<QueryEnvelope<OrgPartyHierarchy>> role = ctx.getOrg(repoId).find().partyHierarchyQuery().get(roleId);
    
    return role.onItem().transform((response) -> {
      if(response.isNotFound()) {
        throw new RoleNotFoundException("Can't find role by id: " + roleId + "!");
      }

      if(response.getStatus() != QueryEnvelopeStatus.OK) {
        final var msg = "failed to get role by id = '%s'!".formatted(roleId);
        final var exception = new RoleQueryException(msg);
        
        response.getMessages()
          .forEach((e) -> {
            if(e.getException() != null) {
              exception.addSuppressed(e.getException());
            }
          });
        log.error(msg);
        throw exception;
      }
      return mapTo(response.getObjects());
    });
  }

  @Override
  public Uni<List<Role>> findAllRoles() {
    final var repoId = ctx.getConfig().getRepoId();
    final Uni<QueryEnvelopeList<OrgPartyHierarchy>> roles = ctx.getOrg(repoId).find().partyHierarchyQuery().findAll();
    
    return roles.onItem().transform((response) -> {
      if(response.getStatus() != QueryEnvelopeStatus.OK) {
        final var msg = "failed to find all roles!";
        final var exception = new RoleQueryException(msg);
        
        response.getMessages()
          .forEach((e) -> {
            if(e.getException() != null) {
              exception.addSuppressed(e.getException());
            }
          });
        log.error(msg);
        throw exception;
      }
      return response.getObjects().stream().map(this::mapTo).toList();
    });
  }
  
 private Role mapTo(OrgPartyHierarchy hierarchy) {
    
    return ImmutableRole.builder()
      .parentId(hierarchy.getParty().getParentId())
      .id(hierarchy.getParty().getId())
      .version(hierarchy.getParty().getCommitId())
      .name(hierarchy.getParty().getPartyName())
      .description(hierarchy.getParty().getPartyDescription())
      .status(hierarchy.getParty().getStatus())
      
      .addAllDirectPermissions(hierarchy.getDirectRights().stream().map(right -> right.getRightName()).toList())
      
      .addAllPermissions(hierarchy.getParentRights().stream().map(right -> right.getRightName()).toList())
      .addAllPermissions(hierarchy.getDirectRights().stream().map(right -> right.getRightName()).toList())
      .addAllPrincipals(hierarchy.getMembers().stream().map(member -> member.getUserName()).toList())
      .build();
  }
  
  public static class RoleQueryException extends RuntimeException {
    private static final long serialVersionUID = 4727517899929638306L;

    public RoleQueryException(String message) {
      super(message);
    }
  }

  @Override
  public RoleQuery evalAccess(RoleAccessEvaluator eval) {
    // TODO Auto-generated method stub
    return null;
  }

}
