package io.resys.thena.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*-
 * #%L
 * thena-db-client
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

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;

public interface ThenaAware {
  void register(Class<?> caller, Uni<?> registeration);
  Multi<TenantAwareRegistration> getOrCreateAll();
  
  @Value.Immutable
  interface TenantAwareRegistration {
    // TODO:: add proper registrtion props
    // Tenant getTenant();
    // boolean isCreated();
  }
  
  
  
  public static class ThenaAware_Default implements ThenaAware {

    private final List<Tuple2<Class<?>, Uni<?>>> items = Collections.synchronizedList(new ArrayList<>());
    
    @Override
    public void register(Class<?> caller, Uni<?> registeration) {
      items.add(Tuple2.of(caller, registeration));
    }

    @Override
    public Multi<TenantAwareRegistration> getOrCreateAll() {
      return Multi.createFrom().iterable(items)
        .onItem().transformToUniAndConcatenate(tuple -> tuple.getItem2())
        .onItem().transform(item -> ImmutableTenantAwareRegistration.builder().build());
    }
    
  }
}
