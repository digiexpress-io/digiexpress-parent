package io.resys.thena.docdb.spi;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import io.resys.thena.docdb.api.ThenaClient;
import io.resys.thena.docdb.api.actions.BranchActions;
import io.resys.thena.docdb.api.actions.CommitActions;
import io.resys.thena.docdb.api.actions.DiffActions;
import io.resys.thena.docdb.api.actions.DocCommitActions;
import io.resys.thena.docdb.api.actions.DocQueryActions;
import io.resys.thena.docdb.api.actions.HistoryActions;
import io.resys.thena.docdb.api.actions.OrgCommitActions;
import io.resys.thena.docdb.api.actions.OrgHistoryActions;
import io.resys.thena.docdb.api.actions.OrgQueryActions;
import io.resys.thena.docdb.api.actions.PullActions;
import io.resys.thena.docdb.api.actions.TenantActions;
import io.resys.thena.docdb.api.actions.TenantActions.RepoResult;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.actions.TagActions;
import io.resys.thena.docdb.models.doc.actions.DocAppendActionsImpl;
import io.resys.thena.docdb.models.doc.actions.DocQueryActionsImpl;
import io.resys.thena.docdb.models.git.GitRepoQueryImpl;
import io.resys.thena.docdb.models.git.commits.CommitActionsImpl;
import io.resys.thena.docdb.models.git.diff.DiffActionsImpl;
import io.resys.thena.docdb.models.git.history.HistoryActionsDefault;
import io.resys.thena.docdb.models.git.objects.BranchActionsImpl;
import io.resys.thena.docdb.models.git.objects.ObjectsActionsImpl;
import io.resys.thena.docdb.models.git.tags.TagActionsDefault;
import io.resys.thena.docdb.models.org.actions.OrgCommitActionsImpl;
import io.resys.thena.docdb.models.org.actions.OrgHistoryActionsImpl;
import io.resys.thena.docdb.models.org.actions.OrgQueryActionsImpl;
import io.resys.thena.docdb.models.org.queries.OrgProjectQueryImpl;
import io.resys.thena.docdb.support.RepoAssert;

public class ThenaClientPgSql implements ThenaClient {
  private final DbState state;
  
  public ThenaClientPgSql(DbState state) {
    super();
    this.state = state;
  }
  
  @Override
  public TenantActions tenants() {
    return new RepoActionsImpl(state);
  }
  public DbState getState() {
    return state;
  }

  @Override
  public GitStructuredTenant git(String repoId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    return new GitStructuredTenant() {
      @Override public GitRepoQuery project() { return new GitRepoQueryImpl(state, repoId); }
      @Override public CommitActions commit() { return new CommitActionsImpl(state, repoId); }
      @Override public TagActions tag() { return new TagActionsDefault(state, repoId); }
      @Override public HistoryActions history() { return new HistoryActionsDefault(state, repoId); }
      @Override public PullActions pull() { return new ObjectsActionsImpl(state, repoId); }
      @Override public DiffActions diff() { return new DiffActionsImpl(state, pull(), commit(), () -> project()); }
      @Override public BranchActions branch() { return new BranchActionsImpl(state, repoId); }
    };
  }

  @Override
  public DocStructuredTenant doc(String repoId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    return new DocStructuredTenant() {
      @Override public DocQueryActions find() { return new DocQueryActionsImpl(state, repoId); }
      @Override public DocCommitActions commit() { return new DocAppendActionsImpl(state, repoId); }
    };
  }

  @Override
  public OrgStructuredTenant org(String repoId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    return new OrgStructuredTenant() {
      @Override public OrgHistoryActions history() { return new OrgHistoryActionsImpl(state, repoId); }
      @Override public OrgQueryActions find() { return new OrgQueryActionsImpl(state, repoId); }
      @Override public OrgCommitActions commit() { return new OrgCommitActionsImpl(state, repoId); }
      @Override public OrgProjectQuery project() { return new OrgProjectQueryImpl(state, repoId); }
    };
  }
  @Override
  public GitStructuredTenant git(RepoResult repo) {
    return git(repo.getRepo().getId());
  }
  @Override
  public GitStructuredTenant git(Repo repo) {
    return git(repo.getId());
  }
  @Override
  public DocStructuredTenant doc(RepoResult repo) {
    return doc(repo.getRepo().getId());
  }
  @Override
  public DocStructuredTenant doc(Repo repo) {
    return doc(repo.getId());
  }
  @Override
  public OrgStructuredTenant org(RepoResult repo) {
    return org(repo.getRepo().getId());
  }
  @Override
  public OrgStructuredTenant org(Repo repo) {
    return this.org(repo.getId());
  }
}
