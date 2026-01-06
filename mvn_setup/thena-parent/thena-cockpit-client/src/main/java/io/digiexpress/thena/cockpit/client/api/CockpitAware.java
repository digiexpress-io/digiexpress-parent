package io.digiexpress.thena.cockpit.client.api;

import java.util.Optional;

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

import org.immutables.value.Value;

import io.smallrye.mutiny.Uni;

public interface CockpitAware<T extends CockpitAware<T>> {

  // load the configured cockpit
  Uni<T> withCockpit();

  // load based on default settings
  Uni<T> withCockpitAwareProps();

  // default settings, when not taking cockpit into account
  CockpitAwareProps getCockpitAwareProps();

  
  @Value.Immutable
  interface CockpitAwareProps {
    String getTenantName();
    CockpitAwareProvider getProvider();
  }
  
  
  @FunctionalInterface
  interface CockpitAwareProvider {
    Uni<Optional<CockpitContainer>> apply();
  }
  
  interface CockpitContainerCache {
    boolean contains(String anyId);
    Optional<CockpitContainer> get(String anyId);
    Optional<CockpitContainer> save(String anyId, Optional<CockpitContainer> container);
    void invalidateAll();
  }
  
}
