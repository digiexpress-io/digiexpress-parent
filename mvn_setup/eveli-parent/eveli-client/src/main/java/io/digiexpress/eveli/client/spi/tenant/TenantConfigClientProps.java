package io.digiexpress.eveli.client.spi.tenant;

/*-
 * #%L
 * eveli-client
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import io.digiexpress.eveli.client.api.ImmutableTenantConfig;
import io.digiexpress.eveli.client.api.TenantConfigClient;
import io.digiexpress.eveli.client.config.EveliProps;
import io.smallrye.mutiny.Uni;

public class TenantConfigClientProps implements TenantConfigClient {
  
  private final TenantConfig config;

  private final static String WRENCH_DISABLED = "wrench-disabled";
  private final static String STENCIL_DISABLED = "stencil-disabled";
  private final static String EXTERNAL_DEPLOYMENT = "external-deployment";
  
  public TenantConfigClientProps(EveliProps config) {
    final var features = Optional
      .ofNullable(config.getTenantFeatures())
      .orElse(new ArrayList<>())
      .stream().flatMap(item -> Arrays.asList(item.split(",")).stream())
      .map(e -> e.trim().toLowerCase())
      .toList();
    
    this.config = ImmutableTenantConfig.builder()
      .features(features)
      .isExternalDeployment(features.contains(EXTERNAL_DEPLOYMENT))
      .isStencilDisabled(features.contains(STENCIL_DISABLED))
      .isWrenchDisabled(features.contains(WRENCH_DISABLED))
      .build();
  }
  
  @Override
  public TenantConfigClientConfigQuery createConfigQuery() {
    return new TenantConfigClientConfigQuery() {
      @Override
      public Uni<TenantConfig> getOne() {
        return Uni.createFrom().item(config);
      }
    };
  }

}
