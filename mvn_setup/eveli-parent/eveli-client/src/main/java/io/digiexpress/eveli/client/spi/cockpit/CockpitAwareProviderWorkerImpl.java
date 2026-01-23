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
import java.util.function.Supplier;

import org.springframework.context.ApplicationEventPublisher;

import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.eveli.client.api.WorkerAuthClient.User;
import io.digiexpress.eveli.envir.spi.actions.ImmutableEveliEnvirInvalidateCache;
import io.digiexpress.eveli.userprofile.client.api.UserProfileClient;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableUiSettingForConfig;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableUpsertUiSettings;
import io.digiexpress.thena.cockpit.client.api.CockpitAware.CockpitAwareProvider;
import io.digiexpress.thena.cockpit.client.api.CockpitAware.CockpitContainerCache;
import io.digiexpress.thena.cockpit.client.api.CockpitClient;
import io.digiexpress.thena.cockpit.client.api.CockpitContainer;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CockpitAwareProviderWorkerImpl implements CockpitAwareProvider {
  private static final String ACTIVE_COCKPIT = "ACTIVE_COCKPIT_ID";
  
  private final Supplier<CockpitClient> cockpitClient;
  private final WorkerAuthClient auth;
  private final UserProfileClient userProfileClient;
  private final CockpitContainerCache cockpitCache;
  private final ApplicationEventPublisher publisher;
  
  @Override
  public Uni<Optional<CockpitContainer>> get() {
    return getUser()
      .onItem().transformToUni(user -> {
        
        if(!user.isAuthenticated()) {
          return Uni.createFrom().item(() -> Optional.<CockpitContainer>empty());
        }
        
        final var principal = user.getPrincipal();
        final var sub = principal.getSub();
        
        if(cockpitCache.contains(sub)) {
          return Uni.createFrom().item(() -> cockpitCache.get(sub));
        }
        return getAndCacheByUser(sub);
      });
  }
  
  @Override
  public Uni<Optional<CockpitContainer>> get(Optional<String> cockpitId) {
    
    if(cockpitId.isEmpty()) {
      return Uni.createFrom().item(Optional.<CockpitContainer>empty());
    }
    
    if(cockpitCache.contains(cockpitId.get())) {
      return Uni.createFrom().item(() -> cockpitCache.get(cockpitId.get()));
    }
    return cockpitClient.get().queries().cockpitQuery().getOne(cockpitId.get())
        .onItem().transform(env -> Optional.ofNullable(env))
        .onItem().invoke(env -> cockpitCache.save(cockpitId.get(), env));
  }
  

  
  private Uni<Optional<CockpitContainer>> getAndCacheByUser(String userSub) {
    
    // user based query
    final Uni<Optional<String>> byUser = userProfileClient.uiSettingsQuery().findOne(userSub, ACTIVE_COCKPIT)
      .onItem().transform(uiSettings -> {
        if(uiSettings.isEmpty()) {
          return Optional.<String>ofNullable(userSub);
        }
        return uiSettings.get().getConfig().stream().map(e -> e.getDataId()).findFirst();
      });
    
    return getUser().onItem().transformToUni(user -> {
        if(user.isAuthenticated()) {
          return byUser;
        }
        return Uni.createFrom().item(Optional.ofNullable(userSub));
      })
      .onItem().transformToUni(cockpitId -> {
        if(cockpitId.isEmpty()) {
          return Uni.createFrom().item(Optional.<CockpitContainer>empty());
        }
        
        return cockpitClient.get().queries().cockpitQuery().addCockpitId(cockpitId.get())
            .findOne()
            .onItem().invoke(env -> cockpitCache.save(userSub, env));
      });
  }

  private Uni<User> getUser() {
    return Uni.createFrom().item(() -> auth.getUser());
  }

  private Uni<CockpitContainer> getCockpit(String cockpitId) {
    return cockpitClient.get().queries().cockpitQuery().getOne(cockpitId);
  }

  
  @Override
  public Uni<Optional<CockpitContainer>> set(Optional<String> cockpitId) {
    if(cockpitId.isEmpty()) {
      return getUser()
          .onItem().transformToUni(user -> {
            
            if(user.isAuthenticated()) {
              final var principal = user.getPrincipal();
              final var sub = principal.getSub();
              return userProfileClient.uiSettingsQuery().deleteOne(sub, ACTIVE_COCKPIT);
            }
            
            return Uni.createFrom().item(Optional.empty());
          })
          .onItem().transform(junk -> Optional.<CockpitContainer>empty())
          .onItem().invoke(junk -> cockpitCache.invalidateAll())
          .onItem().invoke(junk -> publisher.publishEvent(ImmutableEveliEnvirInvalidateCache.builder().build()));
    }
    
    return Uni.combine().all().unis(getCockpit(cockpitId.get()), getUser())
        .asTuple()
        .onItem().transformToUni(tuple -> {
          final var env = tuple.getItem1();
          final var userId = tuple.getItem2().getPrincipal().getSub();
          
          final var command = ImmutableUpsertUiSettings.builder()
              .settingsId(ACTIVE_COCKPIT)
              .userId(userId)
              .addConfig(ImmutableUiSettingForConfig.builder()
                  .dataId(cockpitId.get())
                  .value("")
                  .build())
              .build();
          
          return userProfileClient.updateUiSettings().updateOne(command).onItem().transform((ignore) -> env);
        })
        .onItem().transform(env -> Optional.ofNullable(env))
        .onItem().invoke(env -> cockpitCache.invalidateAll())
        .onItem().invoke(env -> publisher.publishEvent(ImmutableEveliEnvirInvalidateCache.builder().build()));

  }
}
