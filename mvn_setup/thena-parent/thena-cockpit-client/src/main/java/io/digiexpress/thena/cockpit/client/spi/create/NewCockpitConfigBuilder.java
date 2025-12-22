package io.digiexpress.thena.cockpit.client.spi.create;

/*-
 * #%L
 * thena-cockpit-client
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
import java.util.function.Consumer;

import io.digiexpress.thena.cockpit.client.api.CockpitContainer;
import io.digiexpress.thena.cockpit.client.api.CockpitNewObject.NewCockpitConfig;
import io.digiexpress.thena.cockpit.client.api.CockpitNewObject.NewCockpitConfigProps;
import io.digiexpress.thena.cockpit.client.api.CockpitNewObject.NewCockpitConfigTenant;
import io.digiexpress.thena.cockpit.client.api.ImmutableCockpitContainer;
import io.digiexpress.thena.cockpit.client.api.entities.ImmutableCockpitConfig;
import io.digiexpress.thena.cockpit.client.spi.commitlog.CockpitCommitBuilder;
import io.digiexpress.thena.cockpit.client.tables.CockpitDbBuilder.PersistenceUnit;
import io.digiexpress.thena.cockpit.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class NewCockpitConfigBuilder implements NewCockpitConfig {
  private final CockpitCommitBuilder logger;
  private final ImmutableCockpitConfig.Builder config;
  private final String configId;
  private final String commitId;
  
  private ImmutablePersistenceUnit.Builder next;
  private Consumer<CockpitContainer> handleNewState;
  private boolean built;
  
  public NewCockpitConfigBuilder(CockpitCommitBuilder logger) {
    super();
    this.next = ImmutablePersistenceUnit.builder()
        .tenantId(logger.getTenantId())
        .status(BatchStatus.OK)
        .log("");
    
    this.commitId = logger.getCommitId();
    this.configId = logger.getConfigId();
    this.config = ImmutableCockpitConfig.builder()
        .id(configId)
        .commitId(commitId)
        .updatedTreeCommitId(commitId)
        .createdCommitId(commitId)
        .externalId(Optional.empty());
        
    this.logger = logger;
  }
  
  @Override
  public NewCockpitConfig externalId(@Nullable String externalId) {
    this.config.externalId(Optional.ofNullable(externalId));
    return this;
  }
  
  @Override
  public NewCockpitConfig configName(String configName) {
    this.config.cockpitConfigName(configName);
    return this;
  }
  @Override
  public NewCockpitConfig configDesc(String configDesc) {
    this.config.cockpitConfigDesc(configDesc);
    return this;
  }
  @Override
  public NewCockpitConfig addTenant(Consumer<NewCockpitConfigTenant> tenant) {
    final var allTenants = this.next.build();
    final var builder = new NewCockpitConfigTenantBuilder(logger, configId, allTenants);
    tenant.accept(builder);
    final var built = builder.close();
    this.next.addCockpitConfigTenantInserts(built);
    return this;
  }

  @Override
  public NewCockpitConfig addProps(Consumer<NewCockpitConfigProps> props) {
    final var allProps = this.next.build();
    final var builder = new NewCockpitConfigPropsBuilder(logger, allProps);
    props.accept(builder);
    final var built = builder.close();
    this.next.addCockpitConfigPropsInserts(built);
    return this;
  }

  @Override
  public NewCockpitConfig onNewState(Consumer<CockpitContainer> handleNewState) {
    this.handleNewState = handleNewState;
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public PersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call NewCockpitConfig.build() to finalize config CREATE!");

    final var config = this.config.build();
    
    logger.add(config);
    
    next.addCockpitConfigInserts(config);
    final var batch = next.build();
    
    onNewState(batch);
    
    return batch;
  }
  
  private void onNewState(PersistenceUnit batch) {
    if(handleNewState == null) {
      return;
    }
    final var config = batch.getCockpitConfigInserts().iterator().next();
    final var container = ImmutableCockpitContainer.builder()
        .config(config)
        .commits(batch.getCockpitCommitInserts())
        .commitTrees(batch.getCockpitCommitTreeInserts())
        .props(batch.getCockpitConfigPropsInserts())
        .tenants(batch.getCockpitConfigTenantInserts())
        .build();
    
    handleNewState.accept(container);
  }
}
