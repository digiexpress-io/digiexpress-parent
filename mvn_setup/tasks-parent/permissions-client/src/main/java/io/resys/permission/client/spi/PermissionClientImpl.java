package io.resys.permission.client.spi;

import java.util.Optional;

import io.resys.permission.client.api.PermissionClient;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class PermissionClientImpl implements PermissionClient {
  private final PermissionStore ctx;
  
  public PermissionStore getCtx() {
    return ctx;
  }
  @Override
  public PermissionClientImpl withRepoId(String repoId) {
    return new PermissionClientImpl(ctx.withRepoId(repoId));
  }
  @Override
  public Uni<Tenant> getTenant() {
    return ctx.getRepo();
  }
  @Override
  public PermissionTenantQuery tenantQuery() {
    PermissionStore.PermissionRepositoryQuery repo = ctx.query();
    return new PermissionTenantQuery() {
      private String repoName;
      @Override
      public PermissionTenantQuery repoName(String repoName) {
        this.repoName = repoName;
        repo.repoName(repoName);
        return this;
      }
      @Override public Uni<PermissionClient> createIfNot() { return repo.createIfNot().onItem().transform(doc -> new PermissionClientImpl(doc)); }
      @Override public Uni<PermissionClient> create() { return repo.create().onItem().transform(doc -> new PermissionClientImpl(doc)); }
      @Override public PermissionClient build() { return new PermissionClientImpl(repo.build()); }
      @Override public Uni<PermissionClient> delete() { return repo.delete().onItem().transform(doc -> new PermissionClientImpl(doc)); }
      @Override public Uni<PermissionClient> deleteAll() { return repo.deleteAll().onItem().transform(doc -> new PermissionClientImpl(ctx)); }
      @Override
      public Uni<Optional<PermissionClient>> get() {
        RepoAssert.notEmpty(repoName, () -> "repoName must be defined!");
        final var client = ctx.getConfig().getClient();
        return client.tenants().find().id(repoName)
        .get().onItem().transform(existing -> {
          if(existing == null) {
            final Optional<PermissionClient> result = Optional.empty();
            return result;
          }
          return Optional.of(new PermissionClientImpl(repo.build()));
        });
      }
    };
  }

  @Override
  public CreatePermissionAction createPermission() {
    return new CreatePermissionActionImpl(ctx);
  }

  @Override
  public CreateRoleAction createRole() {
    return new CreateRoleActionImpl(ctx);
  }
  
  public CreatePrincipalAction createPrincipal() {
    return new CreatePrincipalActionImpl(ctx);
  }

  @Override
  public UpdatePermissionAction updatePermission() {
    return new UpdatePermissionActionImpl(ctx);
  }

  @Override
  public UpdatePrincipalAction updatePrincipal() {
    return new UpdatePrincipalActionImpl(ctx);
  }

  @Override
  public UpdateRoleAction updateRole() {
    return new UpdateRoleActionImpl(ctx);
  }
  
  @Override
  public PermissionQuery permissionQuery() {
    return new PermissionQueryImpl(ctx);
  }

  @Override
  public RoleQuery roleQuery() {
    return new RoleQueryImpl(ctx);
  }

  @Override
  public PrincipalQuery principalQuery() {
    return new PrincipalQueryImpl(ctx);
  }

  @Override
  public RoleHierarchyQuery roleHierarchyQuery() {
    return new RoleHierarchyQueryImpl(ctx);
  }
}
