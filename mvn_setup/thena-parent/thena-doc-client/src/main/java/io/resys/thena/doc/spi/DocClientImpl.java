package io.resys.thena.doc.spi;

/*-
 * #%L
 * thena-doc-client
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
import io.resys.thena.doc.api.DocClient;
import io.resys.thena.doc.api.DocCommitActions;
import io.resys.thena.doc.api.DocDataSource;
import io.resys.thena.doc.api.DocQueryActions;
import io.resys.thena.doc.spi.actions.DocAppendActionsImpl;
import io.resys.thena.doc.spi.actions.DocQueryActionsImpl;
import io.resys.thena.spi.TenantActionsImpl;
import io.resys.thena.support.RepoAssert;

public class DocClientImpl implements DocClient {
  private final DocDataSource state;
  
  public DocClientImpl(DocDataSource state) {
    super();
    this.state = state;
  }
  
  @Override
  public TenantActions tenants() {
    return new TenantActionsImpl(state);
  }
  public DocDataSource getState() {
    return state;
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
  public DocStructuredTenant doc(CreatedTenant repo) {
    return doc(repo.getRepo().getId());
  }
  @Override
  public DocStructuredTenant doc(Tenant repo) {
    return doc(repo.getId());
  }

  @Override
  public DocDataSource unwrap() {
    return state;
  }
}
