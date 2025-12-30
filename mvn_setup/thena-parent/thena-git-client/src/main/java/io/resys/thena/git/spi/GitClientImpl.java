package io.resys.thena.git.spi;

/*-
 * #%L
 * thena-git-client
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

import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.actions.TenantActions.CreatedTenant;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.git.api.GitBranchActions;
import io.resys.thena.git.api.GitClient;
import io.resys.thena.git.api.GitCommitActions;
import io.resys.thena.git.api.GitDataSource;
import io.resys.thena.git.api.GitDiffActions;
import io.resys.thena.git.api.GitHistoryActions;
import io.resys.thena.git.api.GitPullActions;
import io.resys.thena.git.api.GitTagActions;
import io.resys.thena.git.spi.actions.commits.CommitActionsImpl;
import io.resys.thena.git.spi.actions.diff.DiffActionsImpl;
import io.resys.thena.git.spi.actions.history.HistoryActionsDefault;
import io.resys.thena.git.spi.actions.objects.BranchActionsImpl;
import io.resys.thena.git.spi.actions.objects.ObjectsActionsImpl;
import io.resys.thena.git.spi.actions.tags.TagActionsDefault;
import io.resys.thena.git.spi.builders.GitRepoQueryImpl;
import io.resys.thena.spi.TenantActionsImpl;
import io.resys.thena.support.RepoAssert;

public class GitClientImpl implements GitClient {
  private final GitDataSource state;
  
  public GitClientImpl(GitDataSource state) {
    super();
    this.state = state;
  }
  
  @Override
  public TenantActions tenants() {
    return new TenantActionsImpl(state);
  }
  public GitDataSource getState() {
    return state;
  }

  @Override
  public GitStructuredTenant git(String repoId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    return new GitStructuredTenant() {
      @Override public GitTenantQuery tenants() { return new GitRepoQueryImpl(state, repoId); }
      @Override public GitCommitActions commit() { return new CommitActionsImpl(state, repoId); }
      @Override public GitTagActions tag() { return new TagActionsDefault(state, repoId); }
      @Override public GitHistoryActions history() { return new HistoryActionsDefault(state, repoId); }
      @Override public GitPullActions pull() { return new ObjectsActionsImpl(state, repoId); }
      @Override public GitDiffActions diff() { return new DiffActionsImpl(state, pull(), commit(), () -> tenants()); }
      @Override public GitBranchActions branch() { return new BranchActionsImpl(state, repoId); }
    };
  }


  @Override
  public GitStructuredTenant git(CreatedTenant repo) {
    return git(repo.getRepo().getId());
  }
  @Override
  public GitStructuredTenant git(Tenant repo) {
    return git(repo.getId());
  }
}
