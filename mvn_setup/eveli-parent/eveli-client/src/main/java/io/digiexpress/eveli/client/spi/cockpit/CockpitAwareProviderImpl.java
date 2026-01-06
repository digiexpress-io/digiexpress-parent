package io.digiexpress.eveli.client.spi.cockpit;

/*-
 * #%L
 * eveli-client
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

import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.eveli.client.api.WorkerAuthClient.User;
import io.digiexpress.eveli.userprofile.client.api.UserProfileClient;
import io.digiexpress.thena.cockpit.client.api.CockpitAware.CockpitAwareProvider;
import io.digiexpress.thena.cockpit.client.api.CockpitAware.CockpitContainerCache;
import io.digiexpress.thena.cockpit.client.api.CockpitClient;
import io.digiexpress.thena.cockpit.client.api.CockpitContainer;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CockpitAwareProviderImpl implements CockpitAwareProvider {
  private static final String ACTIVE_COCKPIT = "ACTIVE_COCKPIT_ID";
  private final CockpitClient cockpitClient;
  private final WorkerAuthClient auth;
  private final UserProfileClient userProfileClient;
  private final CockpitContainerCache cache;
  
  @Override
  public Uni<Optional<CockpitContainer>> apply() {
    return getUser()
      .onItem().transformToUni(user -> {
        final var principal = user.getPrincipal();
        final var sub = principal.getSub();
        
        if(cache.contains(sub)) {
          return Uni.createFrom().item(() -> cache.get(sub));
        }
        return getAndCache(sub);
      });
  }
  
  private Uni<Optional<CockpitContainer>> getAndCache(String userSub) {
    return userProfileClient.uiSettingsQuery().findOne(userSub, ACTIVE_COCKPIT)
      .onItem().transform(uiSettings -> {
        if(uiSettings.isEmpty()) {
          return Optional.<String>empty();
        }
        return uiSettings.get().getConfig().stream().map(e -> e.getDataId()).findFirst();
      })
      .onItem().transformToUni(cockpitId -> {
        if(cockpitId.isEmpty()) {
          return Uni.createFrom().item(Optional.<CockpitContainer>empty());
        }
        
        return cockpitClient.queries().cockpitQuery().getOne(ACTIVE_COCKPIT)
            .onItem().transform(env -> Optional.ofNullable(env.getObjects()))
            .onItem().invoke(env -> cache.save(userSub, env));
      });
  }

  private Uni<User> getUser() {
    return Uni.createFrom().item(() -> auth.getUser());
  }
}
