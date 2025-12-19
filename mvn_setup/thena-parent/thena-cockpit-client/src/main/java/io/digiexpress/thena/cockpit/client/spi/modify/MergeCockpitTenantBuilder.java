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

import java.util.Optional;

import io.digiexpress.thena.cockpit.client.api.CockpitMergeObject.MergeCockpitTenant;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfigTenant;
import io.digiexpress.thena.cockpit.client.api.entities.ImmutableCockpitConfigTenant;
import io.digiexpress.thena.cockpit.client.spi.commitlog.CockpitCommitBuilder;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public class MergeCockpitTenantBuilder implements MergeCockpitTenant {
  private final CockpitConfigTenant original;
  private final CockpitCommitBuilder logger;
  private final ImmutableCockpitConfigTenant.Builder next;
  private boolean built;
  
  public MergeCockpitTenantBuilder(CockpitConfigTenant original, CockpitCommitBuilder logger) {
    super();
    this.original = original;
    this.logger = logger;
    this.next = ImmutableCockpitConfigTenant.builder()
        .from(original)
        .commitId(logger.getCommitId());
  }
  
  @Override
  public MergeCockpitTenant externalId(String externalId) {
    this.next.externalId(RepoAssert.notEmpty(externalId, () -> "externalId can't be empty!"));
    return this;
  }

  @Override
  public MergeCockpitTenant externalBranch(@Nullable String externalBranch) {
    this.next.externalBranch(externalBranch != null ? externalBranch : "main");
    return this;
  }

  @Override
  public MergeCockpitTenant tenantDescription(String tenantDescription) {
    this.next.cockpitConfigTenantDesc(RepoAssert.notEmpty(tenantDescription, () -> "tenantDescription can't be empty!"));
    return this;
  }

  @Override
  public MergeCockpitTenant tenantExtension(@Nullable JsonObject tenantExtension) {
    this.next.cockpitConfigTenantExtension(Optional.ofNullable(tenantExtension));
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }
  
  public CockpitConfigTenant close() {
    RepoAssert.isTrue(built, () -> "you must call MergeCockpitTenant.build() to finalize tenant MERGE!");
    
    final var updated = next.build();
    
    // Only return updated entity if it actually changed
    if(original.equals(updated)) {
      return null;
    }
    
    logger.merge(original, updated);
    return updated;
  }
}