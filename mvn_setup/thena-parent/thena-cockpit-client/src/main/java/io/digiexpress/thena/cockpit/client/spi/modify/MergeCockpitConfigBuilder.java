package io.digiexpress.thena.cockpit.client.spi.modify;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import io.digiexpress.thena.cockpit.client.api.CockpitContainer;
import io.digiexpress.thena.cockpit.client.api.CockpitMergeObject.MergeCockpitConfig;
import io.digiexpress.thena.cockpit.client.api.CockpitMergeObject.MergeCockpitProps;
import io.digiexpress.thena.cockpit.client.api.CockpitMergeObject.MergeCockpitTenant;
import io.digiexpress.thena.cockpit.client.api.CockpitNewObject.NewCockpitConfigProps;
import io.digiexpress.thena.cockpit.client.api.CockpitNewObject.NewCockpitConfigTenant;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfigProps;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfigTenant;
import io.digiexpress.thena.cockpit.client.api.entities.ImmutableCockpitConfig;
import io.digiexpress.thena.cockpit.client.spi.commitlog.CockpitCommitBuilder;
import io.digiexpress.thena.cockpit.client.spi.create.NewCockpitConfigPropsBuilder;
import io.digiexpress.thena.cockpit.client.spi.create.NewCockpitConfigTenantBuilder;
import io.digiexpress.thena.cockpit.client.tables.CockpitDbBuilder.PersistenceUnit;
import io.digiexpress.thena.cockpit.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class MergeCockpitConfigBuilder implements MergeCockpitConfig {
  private final CockpitContainer currentState;
  private final CockpitCommitBuilder logger;
  private final ImmutableCockpitConfig.Builder nextConfig;
  private final ImmutablePersistenceUnit.Builder next;
  
  private final Map<String, CockpitConfigTenant> existingTenants = new HashMap<>();
  private final Map<String, CockpitConfigProps> existingProps = new HashMap<>();
  
  private Consumer<CockpitContainer> handleCurrentState;
  private boolean built;
  
  public MergeCockpitConfigBuilder(CockpitContainer currentState, CockpitCommitBuilder logger) {
    super();
    this.currentState = currentState;
    this.logger = logger;
    
    // Initialize maps with existing entities
    currentState.getTenants().forEach(tenant -> existingTenants.put(tenant.getId(), tenant));
    currentState.getProps().forEach(props -> existingProps.put(props.getId(), props));
    
    this.nextConfig = ImmutableCockpitConfig.builder()
        .from(currentState.getConfig())
        .commitId(logger.getCommitId())
        .updatedTreeCommitId(logger.getCommitId());
        
    this.next = ImmutablePersistenceUnit.builder()
        .tenantId(logger.getTenantId())
        .status(BatchStatus.OK)
        .log("");
  }
  
  @Override
  public MergeCockpitConfig onCurrentState(Consumer<CockpitContainer> handleCurrentState) {
    this.handleCurrentState = handleCurrentState;
    return this;
  }
  
  @Override
  public CockpitContainer getCurrentState() {
    return currentState;
  }
  
  @Override
  public MergeCockpitConfig externalId(@Nullable String externalId) {
    this.nextConfig.externalId(Optional.ofNullable(externalId));
    return this;
  }
  
  @Override
  public <T> MergeCockpitConfig setAllProps(String propsType, List<T> replacements, Function<T, Consumer<NewCockpitConfigProps>> props) {
    // Remove existing props of this type
    existingProps.values().stream()
        .filter(p -> propsType.equals(p.getCockpitConfigPropsType()))
        .forEach(p -> {
          logger.rm(p);
          next.addCockpitConfigPropsDeletes(p);
        });
    
    // Add new props
    replacements.forEach(replacement -> {
      final var batch = next.build();
      final var builder = new NewCockpitConfigPropsBuilder(logger, batch);
      props.apply(replacement).accept(builder);
      final var built = builder.close();
      next.addCockpitConfigPropsInserts(built);
    });
    
    return this;
  }
  
  @Override
  public <T> MergeCockpitConfig setAllTenants(List<T> replacements, Function<T, Consumer<NewCockpitConfigTenant>> tenants) {
    // Remove all existing tenants
    existingTenants.values().forEach(tenant -> {
      logger.rm(tenant);
      next.addCockpitConfigTenantDeletes(tenant);
    });
    
    // Add new tenants
    replacements.forEach(replacement -> {
      final var batch = next.build();
      final var builder = new NewCockpitConfigTenantBuilder(logger, currentState.getConfig().getId(), batch);
      tenants.apply(replacement).accept(builder);
      final var built = builder.close();
      next.addCockpitConfigTenantInserts(built);
    });
    
    return this;
  }
  
  @Override
  public MergeCockpitConfig addProps(Consumer<NewCockpitConfigProps> props) {
    final var batch = next.build();
    final var builder = new NewCockpitConfigPropsBuilder(logger, batch);
    props.accept(builder);
    final var built = builder.close();
    next.addCockpitConfigPropsInserts(built);
    return this;
  }
  
  @Override
  public MergeCockpitConfig addTenant(Consumer<NewCockpitConfigTenant> tenant) {
    final var batch = next.build();
    final var builder = new NewCockpitConfigTenantBuilder(logger, currentState.getConfig().getId(), batch);
    tenant.accept(builder);
    final var built = builder.close();
    next.addCockpitConfigTenantInserts(built);
    return this;
  }
  
  @Override
  public MergeCockpitConfig modifyProps(String propsId, Consumer<MergeCockpitProps> props) {
    final var existing = existingProps.get(propsId);
    RepoAssert.notNull(existing, () -> "Props with id '" + propsId + "' not found!");
    
    final var builder = new MergeCockpitPropsBuilder(existing, logger);
    props.accept(builder);
    final var updated = builder.close();
    
    if(updated != null) {
      next.addCockpitConfigPropsUpdates(updated);
    }
    
    return this;
  }
  
  @Override
  public MergeCockpitConfig modifyTenant(String tenantId, Consumer<MergeCockpitTenant> tenant) {
    final var existing = existingTenants.get(tenantId);
    RepoAssert.notNull(existing, () -> "Tenant with id '" + tenantId + "' not found!");
    
    final var builder = new MergeCockpitTenantBuilder(existing, logger);
    tenant.accept(builder);
    final var updated = builder.close();
    
    if(updated != null) {
      next.addCockpitConfigTenantUpdates(updated);
    }
    
    return this;
  }
  
  @Override
  public MergeCockpitConfig removeProps(String propsId) {
    final var existing = existingProps.get(propsId);
    RepoAssert.notNull(existing, () -> "Props with id '" + propsId + "' not found!");
    
    logger.rm(existing);
    next.addCockpitConfigPropsDeletes(existing);
    return this;
  }
  
  @Override
  public MergeCockpitConfig removeTenant(String tenantId) {
    final var existing = existingTenants.get(tenantId);
    RepoAssert.notNull(existing, () -> "Tenant with id '" + tenantId + "' not found!");
    
    logger.rm(existing);
    next.addCockpitConfigTenantDeletes(existing);
    return this;
  }
  
  @Override
  public void build() {
    this.built = true;
  }
  
  public PersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call MergeCockpitConfig.build() to finalize config MERGE!");
    
    if(handleCurrentState != null) {
      handleCurrentState.accept(currentState);
    }
    
    final var config = nextConfig.build();
    
    // Only log config change if it actually changed
    if(!currentState.getConfig().equals(config)) {
      logger.merge(currentState.getConfig(), config);
      next.addCockpitConfigUpdates(config);
    }
    
    return next.build();
  }
}