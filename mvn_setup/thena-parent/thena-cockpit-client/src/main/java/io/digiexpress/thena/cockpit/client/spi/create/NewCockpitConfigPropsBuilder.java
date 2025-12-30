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

import io.digiexpress.thena.cockpit.client.api.CockpitNewObject.NewCockpitConfigProps;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfigProps;
import io.digiexpress.thena.cockpit.client.api.entities.ImmutableCockpitConfigProps;
import io.digiexpress.thena.cockpit.client.spi.commitlog.CockpitCommitBuilder;
import io.digiexpress.thena.cockpit.client.tables.CockpitDbBuilder.PersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public class NewCockpitConfigPropsBuilder implements NewCockpitConfigProps {
  private final CockpitCommitBuilder logger;
  private final ImmutableCockpitConfigProps.Builder props;
  private boolean built;
  
  public NewCockpitConfigPropsBuilder(CockpitCommitBuilder logger, PersistenceUnit batch) {
    super();
    this.logger = logger;
    this.props = ImmutableCockpitConfigProps.builder()
        .id(OidUtils.genUUID())
        .cockpitConfigId(logger.getConfigId())
        .commitId(logger.getCommitId())
        .createdCommitId(logger.getCommitId())
        .cockpitConfigPropsExtension(Optional.empty());
  }

  @Override
  public NewCockpitConfigProps propsType(String propsType) {
    this.props.cockpitConfigPropsType(RepoAssert.notEmpty(propsType, () -> "propsType can't be empty!"));
    return this;
  }

  @Override
  public NewCockpitConfigProps propsExtension(@Nullable JsonObject propsExtension) {
    this.props.cockpitConfigPropsExtension(Optional.ofNullable(propsExtension));
    return this;
  }

  @Override
  public CockpitConfigProps build() {
    this.built = true;
    return props.build();
  }
  
  public CockpitConfigProps close() {
    RepoAssert.isTrue(built, () -> "you must call NewCockpitConfigProps.build() to finalize props CREATE!");
    
    final var entity = props.build();
    logger.add(entity);
    return entity;
  }
}