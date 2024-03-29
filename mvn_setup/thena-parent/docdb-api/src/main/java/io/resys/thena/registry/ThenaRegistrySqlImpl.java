package io.resys.thena.registry;

import io.resys.thena.api.registry.DocRegistry;
import io.resys.thena.api.registry.GitRegistry;
import io.resys.thena.api.registry.OrgRegistry;
import io.resys.thena.api.registry.ThenaRegistry;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.registry.doc.DocRegistrySqlImpl;
import io.resys.thena.registry.git.GitRegistrySqlImpl;


public class ThenaRegistrySqlImpl implements ThenaRegistry {
  @SuppressWarnings("unused")
  private final TenantTableNames ctx;
  private final OrgRegistry org;
  private final GitRegistry git;
  private final DocRegistry doc;
  
  public ThenaRegistrySqlImpl(TenantTableNames ctx) {
    super();
    this.ctx = ctx;
    this.org = null;
    this.doc = new DocRegistrySqlImpl(ctx);
    this.git = new GitRegistrySqlImpl(ctx);
  }
  
  @Override
  public ThenaRegistry withTenant(TenantTableNames options) {
    return new ThenaRegistrySqlImpl(options);
  }
  @Override
  public OrgRegistry org() {
    return org;
  }
  @Override
  public GitRegistry git() {
    return git;
  }
  @Override
  public DocRegistry doc() {
    return doc;
  }
}
