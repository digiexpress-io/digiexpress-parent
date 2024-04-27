package io.resys.permission.client.spi;

import java.util.List;

import io.resys.permission.client.api.PermissionClient.PrincipalAccessEvaluator;
import io.resys.permission.client.api.PermissionClient.PrincipalNotFoundException;
import io.resys.permission.client.api.PermissionClient.PrincipalQuery;
import io.resys.permission.client.api.model.ImmutablePrincipal;
import io.resys.permission.client.api.model.Principal;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgMemberHierarchy;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PrincipalQueryImpl implements PrincipalQuery {
  private final PermissionStore ctx;
  
  @Override
  public Uni<Principal> get(String principalId) {
    final var repoId = ctx.getConfig().getRepoId();
    final Uni<QueryEnvelope<OrgMemberHierarchy>> user = ctx.getOrg(repoId).find().memberHierarchyQuery().get(principalId);
    return user.onItem().transform((response) -> {
      if(response.isNotFound()) {
        throw new PrincipalNotFoundException("Can't find principal by id: " + principalId + "!");
      }
      
      if(response.getStatus() != QueryEnvelopeStatus.OK) {
        final var msg = "Failed to get principal by id = '%s'!".formatted(principalId);
        final var exception = new PrincipalQueryException(msg);
        
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
  public Uni<List<Principal>> findAllPrincipals() {
    final var repoId = ctx.getConfig().getRepoId();
    final Uni<QueryEnvelopeList<OrgMemberHierarchy>> users = ctx.getOrg(repoId).find().memberHierarchyQuery().findAll();
    return users.onItem().transform((response) -> {
      if(response.getStatus() != QueryEnvelopeStatus.OK) {
        final var msg = "Failed to find all principals!";
        final var exception = new PrincipalQueryException(msg);
        
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
  
  private Principal mapTo(OrgMemberHierarchy user) {
        
    return ImmutablePrincipal.builder()
        .id(user.getMember().getId())
        .version(user.getMember().getCommitId()) //TODO
        .name(user.getMember().getUserName())
        .email(user.getMember().getEmail())
        .status(user.getMember().getStatus())
        
        .addAllDirectPermissions(user.getDirectRightNames())
        .addAllDirectRoles(user.getDirectPartyNames())
        
        .addAllRoles(user.getPartyNames())
        .addAllPermissions(user.getRightNames())
        .build();
  }

  public static class PrincipalQueryException extends RuntimeException {

    private static final long serialVersionUID = 4727517899929638306L;

    public PrincipalQueryException(String message) {
      super(message);
    }
    
  }

  @Override
  public PrincipalQuery evalAccess(PrincipalAccessEvaluator eval) {
    // TODO Auto-generated method stub
    return null;
  }
  
}
