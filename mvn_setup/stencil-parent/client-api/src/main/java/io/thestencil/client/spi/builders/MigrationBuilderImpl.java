package io.thestencil.client.spi.builders;

/*-
 * #%L
 * stencil-client-api
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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

import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.MigrationBuilder;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.api.StencilStore;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MigrationBuilderImpl implements MigrationBuilder {
  private final StencilStore store;

  @Override
  public Uni<SiteState> importData(Sites sites) {
    final Uni<SiteState> query = store.stencilQuery().head();
    
    return query.onItem().transformToUni(site -> {
      final var builder = new MigrationImportVisitorForStaticContent(site).visit(sites);
      return store.batch(builder).onItem().transformToUni(s -> store.stencilQuery().head());
    });
  }

  @Override
  public Uni<SiteState> importData(SiteState sites) {
    final Uni<SiteState> query = store.stencilQuery().head();
    
    return query.onItem().transformToUni(site -> {
      final var builder = new MigrationImportVisitorForSiteState(site).visit(sites);
      return store.batch(builder).onItem().transformToUni(s -> store.stencilQuery().head());
    })
    ;
  }
}
