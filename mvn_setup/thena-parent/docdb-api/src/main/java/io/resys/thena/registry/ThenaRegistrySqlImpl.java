package io.resys.thena.registry;

import io.resys.thena.api.registry.DocRegistry;
import io.resys.thena.api.registry.GitRegistry;
import io.resys.thena.api.registry.GrimRegistry;
import io.resys.thena.api.registry.OrgRegistry;
import io.resys.thena.api.registry.TenantRegistry;
import io.resys.thena.api.registry.ThenaRegistry;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.registry.doc.DocRegistrySqlImpl;
import io.resys.thena.registry.git.GitRegistrySqlImpl;
import io.resys.thena.registry.grim.GrimRegistrySqlImpl;
import io.resys.thena.registry.org.OrgRegistrySqlImpl;


public class ThenaRegistrySqlImpl implements ThenaRegistry {
  @SuppressWarnings("unused")
  private final TenantTableNames ctx;
  private final OrgRegistry org;
  private final GitRegistry git;
  private final DocRegistry doc;
  private final TenantRegistry tenant;
  private final GrimRegistry grim;
  
  public ThenaRegistrySqlImpl(TenantTableNames ctx) {
    super();
    this.ctx = ctx;
    this.org = new OrgRegistrySqlImpl(ctx);
    this.doc = new DocRegistrySqlImpl(ctx);
    this.git = new GitRegistrySqlImpl(ctx);
    this.grim = new GrimRegistrySqlImpl(ctx);
    this.tenant = new TenantRegistrySqlImpl(ctx);
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
  @Override
  public TenantRegistry tenant() {
    return tenant;
  }

  @Override
  public GrimRegistry grim() {
    return grim;
  }
}
