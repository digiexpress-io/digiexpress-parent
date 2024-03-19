package io.resys.permission.client.spi;

import java.util.Optional;

import io.resys.permission.client.api.PermissionClient;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.support.RepoAssert;
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
  public Uni<Repo> getRepo() {
    return ctx.getRepo();
  }
  @Override
  public RepositoryQuery repoQuery() {
    PermissionStore.PermissionRepositoryQuery repo = ctx.query();
    return new RepositoryQuery() {
      private String repoName;
      @Override
      public RepositoryQuery repoName(String repoName) {
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
        return client.repo().projectsQuery().id(repoName)
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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CreateRoleAction createRole() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public UpdatePermissionAction updatePermission() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public UpdatePrincipalAction updatePrincipal() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public UpdateRoleAction updateRole() {
    // TODO Auto-generated method stub
    return null;
  }
  
  
  @Override
  public PermissionQuery permissionQuery() {
    return new PermissionQueryImpl(ctx);
  }

  @Override
  public RoleQuery roleQuery() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PrincipalQuery principalQuery() {
    return new PrincipalQueryImpl(ctx);
  }

  @Override
  public RoleHierarchyQuery roleHierarchyQuery() {
    // TODO Auto-generated method stub
    return null;
  }
}
