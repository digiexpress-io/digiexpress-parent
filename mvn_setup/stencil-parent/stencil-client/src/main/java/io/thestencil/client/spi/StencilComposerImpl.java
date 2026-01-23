package io.thestencil.client.spi;

import java.util.Optional;

import io.smallrye.mutiny.Uni;
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
import io.thestencil.client.api.CreateBuilder;
import io.thestencil.client.api.DeleteBuilder;
import io.thestencil.client.api.MigrationBuilder;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.api.StencilClient.MarkdownBuilder;
import io.thestencil.client.api.StencilClient.SitesBuilder;
import io.thestencil.client.api.StencilComposer;
import io.thestencil.client.api.StencilStore.QueryBuilder;
import io.thestencil.client.api.UpdateBuilder;
import io.thestencil.client.api.VersionBuilder;
import io.thestencil.client.spi.builders.CreateBuilderImpl;
import io.thestencil.client.spi.builders.DeleteBuilderImpl;
import io.thestencil.client.spi.builders.MigrationBuilderImpl;
import io.thestencil.client.spi.builders.UpdateBuilderImpl;
import io.thestencil.client.spi.builders.VersionBuilderImpl;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class StencilComposerImpl implements StencilComposer {
  private final StencilClient client;
  
  @Override
  public CreateBuilder create() {
    return new CreateBuilderImpl(client);
  }
  @Override
  public UpdateBuilder update() {
    return new UpdateBuilderImpl(client);
  }
  @Override
  public DeleteBuilder delete() {
    return new DeleteBuilderImpl(client);
  }
  @Override
  public QueryBuilder query() {
    return client.getStore().query();
  }
  @Override
  public MigrationBuilder migration() {
    return new MigrationBuilderImpl(client);
  }
  @Override
  public VersionBuilder version() {
    return new VersionBuilderImpl();
  }
  @Override
  public MarkdownBuilder markdown() {
    return client.markdown();
  }
  @Override
  public SitesBuilder sites() {
    return client.sites();
  }
  public StencilClient getClient() {
    return client;
  }
  
  @Override
  public Uni<StencilComposer> withCockpit() {
    return this.client.withCockpit().onItem().transform(client -> new StencilComposerImpl(client));
  }
  @Override
  public Uni<StencilComposer> withCockpit(Optional<String> id) {
    return this.client.withCockpit(id).onItem().transform(client -> new StencilComposerImpl(client));
  }
  @Override
  public Uni<StencilComposer> withCockpitAwareProps() {
    return this.client.withCockpitAwareProps().onItem().transform(client -> new StencilComposerImpl(client));
  }
  @Override
  public CockpitAwareProps getCockpitAwareProps() {
    return this.client.getCockpitAwareProps();
  }
  @Override
  public Uni<StencilComposer> withCockpitFromProvider() {
    return this.client.withCockpitFromProvider().onItem().transform(client -> new StencilComposerImpl(client));
  }
}
