package io.digiexpress.eveli.permission.client.spi;

/*-
 * #%L
 * eveli-permissions
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Optional;

import io.digiexpress.eveli.permission.client.api.PermissionClient;
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
}
