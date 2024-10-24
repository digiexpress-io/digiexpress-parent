package io.resys.thena.spi;

import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.actions.DocCommitActions;
import io.resys.thena.api.actions.DocQueryActions;
import io.resys.thena.api.actions.GitBranchActions;
import io.resys.thena.api.actions.GitCommitActions;
import io.resys.thena.api.actions.GitDiffActions;
import io.resys.thena.api.actions.GitHistoryActions;
import io.resys.thena.api.actions.GitPullActions;
import io.resys.thena.api.actions.GitTagActions;
import io.resys.thena.api.actions.GrimCommitActions;
import io.resys.thena.api.actions.GrimQueryActions;
import io.resys.thena.api.actions.OrgCommitActions;
import io.resys.thena.api.actions.OrgHistoryActions;
import io.resys.thena.api.actions.OrgQueryActions;
import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.structures.doc.actions.DocAppendActionsImpl;
import io.resys.thena.structures.doc.actions.DocQueryActionsImpl;
import io.resys.thena.structures.git.GitRepoQueryImpl;
import io.resys.thena.structures.git.commits.CommitActionsImpl;
import io.resys.thena.structures.git.diff.DiffActionsImpl;
import io.resys.thena.structures.git.history.HistoryActionsDefault;
import io.resys.thena.structures.git.objects.BranchActionsImpl;
import io.resys.thena.structures.git.objects.ObjectsActionsImpl;
import io.resys.thena.structures.git.tags.TagActionsDefault;
import io.resys.thena.structures.grim.actions.GrimCommitActionsImpl;
import io.resys.thena.structures.grim.actions.GrimQueryActionsImpl;
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
      @Override public OrgProjectQuery tenants() { return new OrgProjectQueryImpl(state, repoId); }
    };
  }
  @Override
  public GrimStructuredTenant grim(String repoId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    return new GrimStructuredTenant() {
      @Override public GrimQueryActions find() { return new GrimQueryActionsImpl(state, repoId); }
      @Override public GrimCommitActions commit() { return new GrimCommitActionsImpl(state, repoId); }
      @Override public GrimProjectQuery tenants() { return null; }
      @Override public String getTenantId() { return repoId; }
    };
  }

  @Override
  public GitStructuredTenant git(TenantCommitResult repo) {
    return git(repo.getRepo().getId());
  }
  @Override
  public GitStructuredTenant git(Tenant repo) {
    return git(repo.getId());
  }
  @Override
  public DocStructuredTenant doc(TenantCommitResult repo) {
    return doc(repo.getRepo().getId());
  }
  @Override
  public DocStructuredTenant doc(Tenant repo) {
    return doc(repo.getId());
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
  public GrimStructuredTenant grim(TenantCommitResult repo) {
    return grim(repo.getRepo().getId());
  }
  @Override
  public GrimStructuredTenant grim(Tenant repo) {
    return this.grim(repo.getId());
  }
}
