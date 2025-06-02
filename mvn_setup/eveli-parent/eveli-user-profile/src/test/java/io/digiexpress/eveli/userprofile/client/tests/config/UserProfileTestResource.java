package io.digiexpress.eveli.userprofile.client.tests.config;

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

import java.util.Arrays;
import java.util.List;

import io.digiexpress.eveli.userprofile.client.api.UserProfileRestApi;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableNotificationSetting;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableUserDetails;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.CreateUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.UserProfileUpdateCommand;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Path;

@Path("q/digiexpress/api")
@ApplicationScoped
public class UserProfileTestResource implements UserProfileRestApi {

  private final ImmutableUserProfile mockUserProfile = getProfile();

  @Override
  public Uni<List<UserProfile>> findAllUserProfiles() {
    return Uni.createFrom().item(Arrays.asList(mockUserProfile));
  }
  
  @Override
  public Uni<UserProfile> createUserProfile(CreateUserProfile command) {
    return Uni.createFrom().item(mockUserProfile);
  }

  @Override
  public Uni<UserProfile> getUserProfileById(String profileId) {
    return Uni.createFrom().item(mockUserProfile);
  }

  @Override
  public Uni<UserProfile> updateUserProfile(String profileId, List<UserProfileUpdateCommand> commands) {
    return Uni.createFrom().item(mockUserProfile);
  }

  @Override
  public Uni<UserProfile> deleteUserProfile(String profileId, UserProfileUpdateCommand command) {
    return Uni.createFrom().item(mockUserProfile);
  }
  
  private static ImmutableUserProfile getProfile() {
    return ImmutableUserProfile.builder()
    .id("id-1234")
    .version("v1.0")
    .created(UserProfileTestCase.getTargetDate())
    .updated(UserProfileTestCase.getTargetDate())
    .details(ImmutableUserDetails.builder()
        .firstName("Ron")
        .lastName("Howard")
        .email("ron_howard@gmail.com")
        .username("ronhoward")
        .build())

    .notificationSettings(Arrays.asList(ImmutableNotificationSetting.builder()
        .type("TASK_ASSIGNED")
        .enabled(true)
        .build()))
    .build();
  }
}
