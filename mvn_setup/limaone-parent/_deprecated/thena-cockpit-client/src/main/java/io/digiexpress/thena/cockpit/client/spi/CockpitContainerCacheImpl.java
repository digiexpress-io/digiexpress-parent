package io.digiexpress.thena.cockpit.client.spi;

/*-
 * #%L
 * thena-cockpit-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import io.digiexpress.thena.cockpit.client.api.CockpitAware.CockpitContainerCache;
import lombok.RequiredArgsConstructor;
import io.digiexpress.thena.cockpit.client.api.CockpitContainer;



@RequiredArgsConstructor
public class CockpitContainerCacheImpl implements CockpitContainerCache {
  private final Cache<String, CockpitContainer> cache;
  
  @Override
  public Optional<CockpitContainer> get(String userId) {
    return Optional.ofNullable(cache.getIfPresent(userId));
  }

  @Override
  public Optional<CockpitContainer> save(String userId, Optional<CockpitContainer> container) {
    
    if(container.isEmpty()) {
      cache.invalidate(userId);
    } else {
      cache.put(userId, container.get());
    }
    
    return container;
  }

  @Override
  public void invalidateAll() {
    cache.invalidateAll();
  }

  @Override
  public boolean contains(String anyId) {
    return cache.getIfPresent(anyId) != null;
  }
}
