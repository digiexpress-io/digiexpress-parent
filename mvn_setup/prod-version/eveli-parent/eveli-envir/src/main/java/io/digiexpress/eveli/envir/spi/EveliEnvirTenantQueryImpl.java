package io.digiexpress.eveli.envir.spi;

/*-
 * #%L
 * eveli-envir
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

import java.util.Optional;

import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.envir.api.EveliEnvirTenantQuery;
import io.digiexpress.eveli.envir.spi.actions.EveliRuntimeCache;
import io.resys.hdes.client.spi.config.HdesClientConfig;
import io.resys.thena.spi.DocStore.StoreTenantQuery;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;

public class EveliEnvirTenantQueryImpl implements EveliEnvirTenantQuery {
  private final EveliEnvirStore ctx;
  private final StoreTenantQuery<EveliEnvirStore> store;
  private final HdesClientConfig hdesClientConfig;
  private final DialobClient dialobClient;
  private final EveliRuntimeCache cache;
  private String repoName;
  
  
  public EveliEnvirTenantQueryImpl(
      EveliEnvirStore ctx,
      HdesClientConfig hdesClientConfig,
      DialobClient dialobClient,
      EveliRuntimeCache cache) {
    
    this.store = ctx.query();
    this.ctx = ctx;
    this.hdesClientConfig = hdesClientConfig;
    this.dialobClient = dialobClient;
    this.cache = cache;
  }
  
  @Override 
  public Uni<EveliEnvirClient> createIfNot() { 
    return store.createIfNot().onItem().transform(doc -> new EveliEnvirClientImpl(doc, hdesClientConfig, dialobClient, cache)); 
  }
  @Override
  public Uni<EveliEnvirClient> create() {
    return store.create().onItem()
        .transform(doc -> new EveliEnvirClientImpl(doc, hdesClientConfig, dialobClient, cache));
  }
  @Override
  public EveliEnvirClientImpl build() {
    return new EveliEnvirClientImpl(store.build(), hdesClientConfig, dialobClient, cache);
  }
  @Override
  public Uni<EveliEnvirClient> delete() {
    return store.delete().onItem()
        .transform(doc -> new EveliEnvirClientImpl(doc, hdesClientConfig, dialobClient, cache));
  }
  @Override
  public Uni<EveliEnvirClient> deleteAll() {
    return store.deleteAll().onItem()
        .transform(doc -> new EveliEnvirClientImpl(ctx, hdesClientConfig, dialobClient, cache));
  }
  @Override
  public EveliEnvirTenantQueryImpl tenantName(String tenantName) {
    this.repoName = tenantName;
    store.repoName(tenantName);
    return this;
  }
  @Override
  public Uni<Optional<EveliEnvirClient>> get() {
    RepoAssert.notEmpty(repoName, () -> "tenantName must be defined!");
    
    final var client = ctx.getConfig().getClient();
    return client.tenants().find().id(repoName)
        .get().onItem().transform(existing -> {
          if(existing == null) {
            final Optional<EveliEnvirClient> result = Optional.empty();
            return result;
          }
          return Optional.of(new EveliEnvirClientImpl(store.build(), hdesClientConfig, dialobClient, cache));
        });
    
  }
}
