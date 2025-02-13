package io.resys.thena.registry;

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
