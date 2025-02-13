package io.digiexpress.thena.mq.client.spi.persistence;

import java.time.OffsetDateTime;

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

import java.util.Optional;

import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.ImmutableChannel;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqDataSource;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqTableNames;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqTableRegistry;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.ThenaSqlPool;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;

public class ThenaMqDataSourceImpl implements ThenaMqDataSource {
  private final Channel tenant;
  private final ThenaMqTableNames tenantTableNames;
  private final ThenaSqlPool pool;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  private final Optional<ThenaSqlClient> tx;
  private final ThenaMqTableRegistry registry;
  private final boolean isTenantLoaded;
  
  public ThenaMqDataSourceImpl(
      Channel tenant, 
      ThenaMqTableNames tenantTableNames, 
      ThenaSqlPool pool,
      ThenaSqlDataSourceErrorHandler errorHandler, 
      Optional<ThenaSqlClient> tx,
      ThenaMqTableRegistry registry) {
    super();
    this.tenant = tenant;
    this.tenantTableNames = tenantTableNames.toChannel(tenant);
    this.registry = registry.withChannel(this.tenantTableNames);
    this.errorHandler = errorHandler;
    this.pool = pool;
    this.tx = tx;
    this.isTenantLoaded = !tenant.getId().equals("") && !tenant.getPrefix().equals("");
  }
  
  public ThenaMqDataSourceImpl(
      String tenant, 
      ThenaMqTableNames tenantTableNames, 
      ThenaSqlPool pool,
      ThenaSqlDataSourceErrorHandler errorHandler, 
      Optional<ThenaSqlClient> tx, 
      ThenaMqTableRegistry registry) {
    super();
    this.isTenantLoaded = false;
    this.tenant = ImmutableChannel.builder()
        .channelName(tenant)
        .createdAt(OffsetDateTime.now())
        .createdBy("")
        .comment("")
        .id("")
        .prefix("")
        .build();
    this.tenantTableNames = tenantTableNames.toChannel(this.tenant);
    this.errorHandler = errorHandler;
    this.registry = registry.withChannel(this.tenantTableNames);
    this.pool = pool;
    this.tx = tx;
  }
  
  @Override
  public Channel getChannel() {
    return tenant;
  }
  @Override
  public ThenaMqTableNames getChannelTableNames() {
    return tenantTableNames;
  }
  @Override
  public ThenaSqlPool getPool() {
    return pool;
  }
  @Override
  public ThenaSqlDataSourceErrorHandler getErrorHandler() {
    return errorHandler;
  }
  @Override
  public Optional<ThenaSqlClient> getTx() {
    return tx;
  }
  @Override
  public ThenaMqDataSourceImpl withChannel(Channel tenant) {
    return new ThenaMqDataSourceImpl(tenant, tenantTableNames, pool, errorHandler, tx, registry);
  }

  @Override
  public boolean isLocked(Throwable t) {
    return this.errorHandler.isLocked(t);
  }

  @Override
  public ThenaMqDataSourceImpl withTx(ThenaSqlClient tx) {
    return new ThenaMqDataSourceImpl(tenant, tenantTableNames, pool, errorHandler, Optional.of(tx), registry);
  }

  @Override
  public boolean isChannelLoaded() {
    return isTenantLoaded;
  }
  @Override
  public ThenaMqTableRegistry getRegistry() {
    return registry;
  }
}
