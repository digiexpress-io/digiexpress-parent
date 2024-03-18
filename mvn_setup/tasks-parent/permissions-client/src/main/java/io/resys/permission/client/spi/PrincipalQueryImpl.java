package io.resys.permission.client.spi;

import java.util.List;

import io.resys.permission.client.api.PermissionClient.PrincipalQuery;
import io.resys.permission.client.api.model.ImmutablePrincipal;
import io.resys.permission.client.api.model.Principal;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.QueryEnvelopeList;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgUserHierarchy;
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
    final Uni<QueryEnvelope<OrgUserHierarchy>> user = ctx.getOrg().find().userHierarchyQuery().repoId(repoId).get(principalId);
    return user.onItem().transform((response) -> {
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
    final Uni<QueryEnvelopeList<OrgUserHierarchy>> users = ctx.getOrg().find().userHierarchyQuery().repoId(repoId).findAll();
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
  
  private Principal mapTo(OrgUserHierarchy user) {
        
    return ImmutablePrincipal.builder()
        .id(user.getUserId())
        .version(user.getCommitId()) //TODO
        .name(user.getUserName())
        .email(user.getEmail())
        .status(user.getStatus())
        .addAllDirectPermissions(user.getDirectRoleNames())
        .addAllDirectRoles(user.getDirectGroupNames())
        .addAllRoles(user.getGroupNames())
        .addAllPermissions(user.getRoleNames())
        .build();
  }

  public static class PrincipalQueryException extends RuntimeException {

    private static final long serialVersionUID = 4727517899929638306L;

    public PrincipalQueryException(String message) {
      super(message);
    }
    
  }
  
}
