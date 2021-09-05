package io.thestencil.client.spi.builders;

/*-
 * #%L
 * stencil-client
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

import io.resys.thena.docdb.api.actions.CommitActions.CommitStatus;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.MigrationBuilder;
import io.thestencil.client.api.StencilClient.SiteState;
import io.thestencil.client.spi.PersistenceConfig;
import io.thestencil.client.spi.exceptions.ImportException;

public class MigrationBuilderImpl implements MigrationBuilder {

  private final PersistenceConfig config;
  
  public MigrationBuilderImpl(PersistenceConfig config) {
    super();
    this.config = config;
  }

  @Override
  public Uni<SiteState> importData(Sites sites) {
    return new MigrationImportVisitor(config).visit(sites)
    .head(config.getRepoName(), config.getHeadName())
    .message("import-sites")
    .parentIsLatest()
    .author(config.getAuthorProvider().getAuthor())
    .build().onItem().transformToUni(commit -> {
      if(commit.getStatus() == CommitStatus.OK) {
        return new QueryBuilderImpl(config).head();
      }
      throw new ImportException(sites, commit);
    });
  }
}
