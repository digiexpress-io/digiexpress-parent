package io.resys.thena.spi;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.actions.FsCommitActions;
import io.resys.thena.api.actions.FsQueryActions;
import io.resys.thena.api.actions.OrgCommitActions;
import io.resys.thena.api.actions.OrgHistoryActions;
import io.resys.thena.api.actions.OrgQueryActions;
import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.structures.fs.actions.FsCommitActionsImpl;
import io.resys.thena.structures.fs.actions.FsQueryActionsImpl;
import io.resys.thena.structures.org.actions.OrgCommitActionsImpl;
import io.resys.thena.structures.org.actions.OrgHistoryActionsImpl;
import io.resys.thena.structures.org.actions.OrgQueryActionsImpl;
import io.resys.thena.structures.org.queries.OrgProjectQueryImpl;
import io.resys.thena.support.RepoAssert;

public class ThenaClientPgSql implements ThenaClient {
  private final DbState state;
  
  public ThenaClientPgSql(DbState state) {
    super();
    this.state = state;
  }
  
  @Override
  public TenantActions tenants() {
    return new TenantActionsImpl(state);
  }
  public DbState getState() {
    return state;
  }

  @Override
  public OrgStructuredTenant org(String repoId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    return new OrgStructuredTenant() {
      @Override public OrgHistoryActions history() { return new OrgHistoryActionsImpl(state, repoId); }
      @Override public OrgQueryActions find() { return new OrgQueryActionsImpl(state, repoId); }
      @Override public OrgCommitActions commit() { return new OrgCommitActionsImpl(state, repoId); }
      @Override public OrgProjectQuery tenants() { return new OrgProjectQueryImpl(state, repoId); }
    };
  }

  @Override
  public FsStructuredTenant fs(String tenantIdOrName) {
    RepoAssert.notEmpty(tenantIdOrName, () -> "tenantIdOrName can't be empty!");
    return new FsStructuredTenant() {
      @Override public FsProjectQuery tenants() { return null; }
      @Override public String getTenantId() { return tenantIdOrName; }
      @Override public FsQueryActions find() { return new FsQueryActionsImpl(state, tenantIdOrName); }
      @Override public FsCommitActions commit() { return new FsCommitActionsImpl(state, tenantIdOrName); }
    };
  }
  

  @Override
  public OrgStructuredTenant org(TenantCommitResult repo) {
    return org(repo.getRepo().getId());
  }
  @Override
  public OrgStructuredTenant org(Tenant repo) {
    return this.org(repo.getId());
  }

  @Override
  public FsStructuredTenant fs(TenantCommitResult repo) {
    return this.fs(repo.getRepo().getId());
  }
  @Override
  public FsStructuredTenant fs(Tenant repo) {
    return this.fs(repo.getId());
  }
}
