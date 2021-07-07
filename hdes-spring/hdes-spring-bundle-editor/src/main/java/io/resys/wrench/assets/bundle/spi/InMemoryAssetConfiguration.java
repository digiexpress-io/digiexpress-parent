package io.resys.wrench.assets.bundle.spi;

/*-
 * #%L
 * wrench-component-assets
 * %%
 * Copyright (C) 2016 - 2017 Copyright 2016 ReSys OÜ
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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.resys.wrench.assets.bundle.api.repositories.AssetServiceRepository.ServiceStore;
import io.resys.wrench.assets.bundle.spi.store.AssetLocation;
import io.resys.wrench.assets.bundle.spi.store.InMemoryAssetStore;

@ConditionalOnProperty(name = "wrench.assets.git.enabled", havingValue = "false", matchIfMissing = true)
@Configuration
public class InMemoryAssetConfiguration {

  @Bean
  public AssetLocation classpathAssetLocation() {
    return new AssetLocation("classpath*:", "assets/", false);
  }

  @Bean
  public ServiceStore serviceStore() {
    return new InMemoryAssetStore();
  }
}
