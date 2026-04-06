package io.resys.limaone.persistence.world;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

import io.resys.limaone.authoring.Authoring.WorldIndexQuery;
import io.resys.limaone.authoring.Authoring.WorldQuery;
import io.resys.limaone.model.ImmutableModelWorldIndex;
import io.resys.limaone.model.Model.ModelWorldIndex;
import io.resys.limaone.persistence.ModelWorldDb;
import io.resys.limaone.spi.dialob.FormDb;
import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.actions.TenantActions.TenantAware;
import io.resys.thena.api.actions.TenantActions.TenantDb;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.fs.api.FileSystem;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ModelWorldDb_FS implements ModelWorldDb, TenantAware<ModelWorldDb_FS> {

  private final FormDb formDb;
  private final FileSystem fileSystem;
  private final ScheduledExecutorService workerPool;
  private final Duration workerTimeout;
  private final String branchName;
  private final Optional<TenantDb> tenantDb;

  @Override
  public ModelWorldDb withBranchName(Optional<String> branchName) {
    final var dirtyBranchName = branchName == null ? null : branchName.orElse(null);
    final var resolveBranchName = dirtyBranchName == null || dirtyBranchName.trim().isEmpty() ? 
        WorldBuilderImpl.branchName : dirtyBranchName;
    final var finalBranchName = resolveBranchName.trim().isEmpty() ? WorldBuilderImpl.branchName : resolveBranchName;
    return new ModelWorldDb_FS(formDb, getUserFileSystem(), workerPool, workerTimeout, finalBranchName, tenantDb);
  }
  @Override
  public CreateModelWorldDb createModelWorldDb() {
    return new CreateModelWorldDb() {
      @Override
      public Uni<Void> createDbIfNotPresent() {
        final var tenantName = fileSystem.getTenantName();
        return fileSystem.tenants().createOneTenant()
            .name(tenantName, StructureType.fs)
            .buildOnlyIfNotCreated()
            .onItem().transformToUni(ignore -> Uni.createFrom().voidItem());
      }
    };
  }
  @Override
  public TenantActions getActions() {
    return fileSystem.tenants();
  }
  @Override
  public WorldBuilder worldBuilder() {
    return new WorldBuilderImpl(getUserFileSystem());
  }
  @Override
  public WorldQuery worldQuery() {
    return new WorldQueryImpl(workerPool, workerTimeout, getUserFileSystem(), formDb, branchName);
  }
  @Override
  public WorldIndexQuery worldIndexQuery() {
    return new WorldIndexQuery() {
      @Override
      public Multi<ModelWorldIndex> findAll() {
        return getUserFileSystem().withTenant().branchQuery()
            .branchName(name -> name.equals(branchName))
            .findIndexOnly()
            .onItem().transform(item -> ImmutableModelWorldIndex.builder()
                
                .objectId(item.getObjectId())
                
                .createdAt(item.getCreatedAt())
                .createdBy(item.getCreatedByAuthor())
                
                .updatedAt(item.getUpdatedAt())
                .updatedBy(item.getUpdatedByAuthor())
                
                .build());
      }
    };
  }

  @Override
  public ModelWorldDb withTenant(Optional<String> tenantName) {
    final var tenant = fileSystem.withDefaultTenant(tenantName)
        .runSubscriptionOn(workerPool)
        .await().atMost(workerTimeout);
    return new ModelWorldDb_FS(formDb, tenant, workerPool, workerTimeout, branchName, tenantDb);
  }

  @Override
  public ModelWorldDb_FS withTenantDb(TenantDb tenantDb) {
    return new ModelWorldDb_FS(formDb, fileSystem, workerPool, workerTimeout, branchName, Optional.ofNullable(tenantDb));
  }
  
  private FileSystem getUserFileSystem() {
    if(tenantDb.isEmpty()) {
      return fileSystem;
    }
    
    final var tenantName = Optional.ofNullable(tenantDb.get().getCurrentUserTenant());
    if(tenantName.isEmpty()) {
      return fileSystem;
    }
    return fileSystem.withDefaultTenant(tenantName)
        .runSubscriptionOn(workerPool)
        .await().atMost(workerTimeout);
  }
  
  
  public static class WorldLockException extends RuntimeException {
    private static final long serialVersionUID = -1868980098559928896L;
  }
}
