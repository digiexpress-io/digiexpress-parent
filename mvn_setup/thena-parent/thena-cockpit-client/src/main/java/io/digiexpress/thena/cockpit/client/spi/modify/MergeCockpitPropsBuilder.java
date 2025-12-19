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

import io.digiexpress.thena.cockpit.client.api.CockpitMergeObject.MergeCockpitProps;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfigProps;
import io.digiexpress.thena.cockpit.client.api.entities.ImmutableCockpitConfigProps;
import io.digiexpress.thena.cockpit.client.spi.commitlog.CockpitCommitBuilder;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public class MergeCockpitPropsBuilder implements MergeCockpitProps {
  private final CockpitConfigProps original;
  private final CockpitCommitBuilder logger;
  private final ImmutableCockpitConfigProps.Builder next;
  private boolean built;
  
  public MergeCockpitPropsBuilder(CockpitConfigProps original, CockpitCommitBuilder logger) {
    super();
    this.original = original;
    this.logger = logger;
    this.next = ImmutableCockpitConfigProps.builder()
        .from(original)
        .commitId(logger.getCommitId());
  }
  
  @Override
  public MergeCockpitProps externalId(String externalId) {
    // Note: CockpitConfigProps doesn't have externalId field based on our entities
    // This method is from the interface but doesn't map to the entity
    return this;
  }

  @Override
  public MergeCockpitProps propsType(String propsType) {
    this.next.cockpitConfigPropsType(RepoAssert.notEmpty(propsType, () -> "propsType can't be empty!"));
    return this;
  }

  @Override
  public MergeCockpitProps propsExtension(@Nullable JsonObject propsExtension) {
    this.next.cockpitConfigPropsExtension(Optional.ofNullable(propsExtension));
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }
  
  public CockpitConfigProps close() {
    RepoAssert.isTrue(built, () -> "you must call MergeCockpitProps.build() to finalize props MERGE!");
    
    final var updated = next.build();
    
    // Only return updated entity if it actually changed
    if(original.equals(updated)) {
      return null;
    }
    
    logger.merge(original, updated);
    return updated;
  }
}