package io.digiexpress.eveli.envir.spi;

import java.time.OffsetDateTime;

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

import com.github.benmanes.caffeine.cache.Cache;

import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliRuntime;
import io.digiexpress.eveli.envir.spi.actions.EveliRuntimeCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class EveliRuntimeCacheInMemory implements EveliRuntimeCache {
  private final Cache<String, EveliDeployment> short_deployment_cache;
  private final Cache<String, EveliRuntime> long_runtime_cache;

  @Override
  public Optional<EveliDeployment> getDeployment() {
    
    final OffsetDateTime now = OffsetDateTime.now();
    final var result = short_deployment_cache.asMap().values().stream()
      .filter((a) -> a.getStartsAt().isBefore(now))
      .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
      .findFirst();
    log.debug("Resolving deployment: {} from cache", result.map(e -> e.getId()));
    return result;
  }
  @Override
  public Optional<EveliRuntime> getRuntime(String runtimeId) {
    final var runtime = Optional.ofNullable(long_runtime_cache.getIfPresent(runtimeId));
    log.debug("Resolving runtime: {} from cache", runtime.map(e -> e.getDeploymentId()));
    return runtime;
  }
  @Override
  public EveliDeployment save(EveliDeployment deployment) {
    this.short_deployment_cache.put(deployment.getId(), deployment);
    log.debug("Saving deployment: {} to cache", deployment.getId());
    return deployment;
  }
  @Override
  public EveliRuntime save(EveliRuntime runtime) {
    this.long_runtime_cache.put(runtime.getDeploymentId(), runtime);
    log.debug("Saving runtime: {} from cache", runtime.getDeploymentId());
    return runtime;
  }
}
