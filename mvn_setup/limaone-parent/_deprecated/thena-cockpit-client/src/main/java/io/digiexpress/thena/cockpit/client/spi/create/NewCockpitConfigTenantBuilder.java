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

import io.digiexpress.thena.cockpit.client.api.CockpitNewObject.NewCockpitConfigTenant;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfigTenant;
import io.digiexpress.thena.cockpit.client.api.entities.ImmutableCockpitConfigTenant;
import io.digiexpress.thena.cockpit.client.spi.commitlog.CockpitCommitBuilder;
import io.digiexpress.thena.cockpit.client.tables.CockpitDbBuilder.PersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public class NewCockpitConfigTenantBuilder implements NewCockpitConfigTenant {
  private final ImmutableCockpitConfigTenant.Builder tenant;
  private boolean built;
  
  public NewCockpitConfigTenantBuilder(CockpitCommitBuilder logger, String configId, PersistenceUnit batch) {
    super();
    this.tenant = ImmutableCockpitConfigTenant.builder()
        .id(OidUtils.genUUID())
        .cockpitConfigId(logger.getConfigId())
        .commitId(logger.getCommitId())
        .createdCommitId(logger.getCommitId())
        .cockpitConfigTenantExtension(Optional.empty());
    externalBranch(null);
  }
  
  @Override
  public NewCockpitConfigTenant externalId(String externalId) {
    this.tenant.externalId(RepoAssert.notEmpty(externalId, () -> "externalId can't be empty!"));
    return this;
  }

  @Override
  public NewCockpitConfigTenant externalBranch(@Nullable String externalBranch) {
    this.tenant.externalBranch(externalBranch != null ? externalBranch : "main");
    return this;
  }

  @Override
  public NewCockpitConfigTenant tenantDescription(String tenantDescription) {
    this.tenant.cockpitConfigTenantDesc(RepoAssert.notEmpty(tenantDescription, () -> "tenantDescription can't be empty!"));
    return this;
  }
  @Override
  public NewCockpitConfigTenant tenantType(String tenantType) {
    this.tenant.cockpitConfigTenantType(RepoAssert.notEmpty(tenantType, () -> "tenantType can't be empty!"));
    return this;
  }

  @Override
  public NewCockpitConfigTenant tenantExtension(@Nullable JsonObject tenantExtension) {
    this.tenant.cockpitConfigTenantExtension(Optional.ofNullable(tenantExtension));
    return this;
  }

  @Override
  public CockpitConfigTenant build() {
    this.built = true;
    return tenant.build();
  }
  
  public CockpitConfigTenant close() {
    RepoAssert.isTrue(built, () -> "you must call NewCockpitConfigTenant.build() to finalize tenant CREATE!");
    
    final var entity = tenant.build();
    return entity;
  }
}