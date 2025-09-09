package io.digiexpress.eveli.client.web.resources.worker;

import java.util.Arrays;

/*-
 * #%L
 * eveli-user-profile
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

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.eveli.client.config.EveliWrenchOnlyTenantAutoConfig;
import io.digiexpress.eveli.userprofile.client.api.UserProfileClient;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableCreateUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UiSettings;
import io.digiexpress.eveli.userprofile.client.api.model.UiSettingsCommand.UiSettingsUpdateCommand;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.CreateUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.UserProfileUpdateCommand;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/worker/rest/api/userprofiles")
@RequiredArgsConstructor
public class UserProfileController {

  private final UserProfileClient userProfileClient;
  private final WorkerAuthClient workerAuthClient;
  
  @GetMapping
  public Uni<List<UserProfile>> findAllUserProfiles() {
    return userProfileClient.userProfileQuery().findAll();
  }
  @GetMapping("/{id}")
  public Uni<UserProfile> getUserProfileById(
      @PathVariable("id") String profileId,
      @RequestParam(required = false, name = "create", defaultValue = "false") boolean create
  ) {
    assertAccess(profileId);
    final var id = getUserId(profileId);
    return userProfileClient.userProfileQuery().findByIds(Arrays.asList(id))
        .onItem().transformToUni(items -> {
          
          if(items.isEmpty() && !create) {
            return Uni.createFrom().nullItem();
          }
          
          // create profile if non
          if(items.isEmpty()) {
            final var principal = workerAuthClient.getUser().getPrincipal();
            final var command = ImmutableCreateUserProfile.builder()
                .id(principal.getSub())
                .email(principal.getEmail())
                .addTenantFeatures(EveliWrenchOnlyTenantAutoConfig.FEATURE_USER_PROFILE)
                .build();
            return userProfileClient.createUserProfile().createOne(command);
          } 
          
          // Profile exists
          return Uni.createFrom().item(items.iterator().next());
        });
  }
  @PostMapping
  public Uni<UserProfile> createUserProfile(@RequestBody CreateUserProfile command) {
    assertAccess(command.getUsername());
    return userProfileClient.createUserProfile().createOne(command);
  }
  @PutMapping("/{id}")
  public Uni<UserProfile> updateUserProfile(@PathVariable("id") String profileId, @RequestBody List<UserProfileUpdateCommand> commands) {
    assertAccess(profileId);
    return userProfileClient.updateUserProfile().updateOne(commands);
  }
  @DeleteMapping("/{id}")
  public Uni<UserProfile> deleteUserProfile(@PathVariable("id") String profileId, @RequestBody UserProfileUpdateCommand command) {
    assertAccess(profileId);
    return userProfileClient.updateUserProfile().updateOne(command);
  }
  @PutMapping("/{id}/ui-settings")
  public Uni<UiSettings> uiSettings(@PathVariable("id") String profileId, @RequestBody UiSettingsUpdateCommand commands) {
    assertAccess(profileId);
    assertAccess(commands.getUserId());
    return userProfileClient.updateUiSettings().updateOne(commands);
  }
  
  @GetMapping("/{id}/ui-settings/{settingsId}")
  public Uni<UiSettings> getUISettings(@PathVariable("id") String profileId, @PathVariable("settingsId") String settingsId) {
    assertAccess(profileId);
    return userProfileClient.uiSettingsQuery().findOne(getUserId(profileId), settingsId)
        .onItem().transformToUni(result -> {
          if(result.isPresent()) {
            return Uni.createFrom().item(result.get());
          }
          return Uni.createFrom().nullItem();
        });
  }
  public String getUserId(String init) {
    if("current".equals(init)) {
      return workerAuthClient.getUser().getPrincipal().getSub();
    }
    return init;
  }

  private void assertAccess(String id) {
    if(workerAuthClient.getUser().getPrincipal().getSub().equals(id) || "current".equals(id)) {
      return;
    }
    
    // todo fix this later
    throw new RuntimeException("not allowed!");
  }
}
