package io.resys.thena.fs.spi;

/*-
 * #%L
 * thena-fs-client
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

import java.util.Optional;

import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.actions.TenantActions.CreatedTenant;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.TenantCacheImpl;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.datasource.ThenaSqlDataSource.TenantCache;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceImpl;
import io.resys.thena.datasource.vertx.ThenaSqlPoolVertx;
import io.resys.thena.fs.api.FileSystem;
import io.resys.thena.fs.api.branches.BranchQuery;
import io.resys.thena.fs.api.branches.CreateBranch;
import io.resys.thena.fs.api.commits.CommitBuilder;
import io.resys.thena.fs.api.commits.CommitQuery;
import io.resys.thena.fs.api.tags.CreateTag;
import io.resys.thena.fs.api.tags.ModifyTag;
import io.resys.thena.fs.api.tags.TagQuery;
import io.resys.thena.fs.spi.branch.BranchQueryImpl;
import io.resys.thena.fs.spi.branch.CreateBranchImpl;
import io.resys.thena.fs.spi.commit.CommitBuilderImpl;
import io.resys.thena.fs.spi.commit.CommitQueryImpl;
import io.resys.thena.fs.spi.tag.CreateTagImpl;
import io.resys.thena.fs.spi.tag.ModifyTagImpl;
import io.resys.thena.fs.spi.tag.TagQueryImpl;
import io.resys.thena.fs.tables.FsDb;
import io.resys.thena.fs.tables.spi.FsDbImpl;
import io.resys.thena.spi.TenantActionsImpl;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FileSystem_ThenaImpl implements FileSystem {

  private final FsDb startingState;
  
  @Override
  public TenantActions tenants() {
    return new TenantActionsImpl(startingState, StructureType.fs);
  }
  @Override
  public FileSystemTenant withTenant() {
    return withTenant(startingState.getDataSource().getTenant().getName());
  }
  @Override
  public FileSystemTenant withTenant(CreatedTenant repo) {
    return withTenant(repo.getRepo().getId());
  }
  @Override
  public FileSystemTenant withTenant(Tenant repo) {
    return this.withTenant(repo.getId());
  }
  @Override
  public FileSystemTenant withTenant(String tenantId) {
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    
    final Uni<FsDb> start = startingState.withTenant(tenantId);
    
    return new FileSystemTenant() {
      @Override public String getTenantId() { return tenantId; }
      
      @Override public CommitQuery commitQuery() { return new CommitQueryImpl(start, tenantId); }
      @Override public CommitBuilder commitBuilder() { return new CommitBuilderImpl(start, tenantId); }
      
      @Override public BranchQuery branchQuery() { return new BranchQueryImpl(start); }
      @Override public CreateBranch createBranch() { return new CreateBranchImpl(start, tenantId); }
      
      @Override public CreateTag createTag() { return new CreateTagImpl(start, tenantId); }
      @Override public ModifyTag modifyTag() { return new ModifyTagImpl(start, tenantId); }      
      @Override public TagQuery tagQuery() { return new TagQueryImpl(start, tenantId); }
    };
  }
  
  
  public static FsDbImpl createInstance(TenantContext names, io.vertx.mutiny.sqlclient.Pool client, TenantCache tenantCache, ThenaSqlDataSourceErrorHandler errorHAndler) {
    final var pool = new ThenaSqlPoolVertx(client);

    final var dataSource = new ThenaSqlDataSourceImpl(
        "", names, pool, errorHAndler, 
        Optional.empty(),
        tenantCache
    );
    return new FsDbImpl(dataSource);
  }
  
  public static CreateInstance createInstance() {
    return new CreateInstance();
  }

  public static class CreateInstance {
    private io.vertx.mutiny.sqlclient.Pool client;
    private String tenantName = "docdb";
    private ThenaSqlDataSourceErrorHandler errorHandler;

    private TenantCache tenantCache;    
    public CreateInstance errorHandler(ThenaSqlDataSourceErrorHandler errorHandler) {this.errorHandler = errorHandler; return this; }
    public CreateInstance tenantName(String tenantName) { this.tenantName = tenantName; return this; }
    public CreateInstance tenantCache(TenantCache tenantCache) { this.tenantCache = tenantCache; return this; }
    public CreateInstance client(io.vertx.mutiny.sqlclient.Pool client) { this.client = client; return this; }

    
    public ThenaSqlDataSourceImpl datasource() {
      RepoAssert.notNull(client, () -> "client must be defined!");
      RepoAssert.notNull(tenantName, () -> "tenantName must be defined!");
      RepoAssert.notNull(errorHandler, () -> "errorHandler must be defined!");
      
      final var tenantCache = this.tenantCache == null ? new TenantCacheImpl() : this.tenantCache;
      final var ctx = TenantContext.defaults(tenantName);
      final var pool = new ThenaSqlPoolVertx(client);
      
      final var dataSource = new ThenaSqlDataSourceImpl(
          tenantName, ctx, pool, errorHandler, 
          Optional.empty(),
          tenantCache
      );
      
      return dataSource;
    }
    
    public FileSystem_ThenaImpl build() {
      final var state = new FsDbImpl(datasource());
      return new FileSystem_ThenaImpl(state);
    }
  }

  public FsDb getStartingState() {
    return startingState;
  }
}
