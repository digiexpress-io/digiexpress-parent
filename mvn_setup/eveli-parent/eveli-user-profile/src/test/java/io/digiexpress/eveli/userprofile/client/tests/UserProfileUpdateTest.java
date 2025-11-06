package io.digiexpress.eveli.userprofile.client.tests;

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

import io.digiexpress.eveli.userprofile.client.api.UserProfileClient;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableChangeUserDetailsFirstName;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableCreateUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableNotificationSetting;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableUpsertUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfile;
import io.digiexpress.eveli.userprofile.client.tests.config.UserProfileTestCase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserProfileUpdateTest extends UserProfileTestCase {

  private UserProfile createUserProfileForUpdating(UserProfileClient client) {
    return client.createUserProfile()
      .createOne(ImmutableCreateUserProfile.builder()
        .id("jerry-id-1")
        .firstName("Jerry")
        .lastName("Springer")
        .username("jerryspringer")
        .email("jerry@thejerryspringershow.com")
        .notificationSettings(Arrays.asList(ImmutableNotificationSetting.builder()
            .type("TASK_ASSIGNED")
            .enabled(true)
            .build()))
        
        .build())
      .await().atMost(atMost);
  }
  
  
  @SuppressWarnings("unused")
  @org.junit.jupiter.api.Test
  public void changeUserDetailsFirstName() {
    final var repoName = UserProfileUpdateTest.class.getSimpleName() + "ChangeUserDetailsFirstName";
    final var client = getClient().repoQuery().repoName(repoName).createIfNot().await().atMost(atMost);
    final var userProfile = createUserProfileForUpdating(client);
    
    final var updated = client.updateUserProfile().updateOne(ImmutableChangeUserDetailsFirstName.builder()
        .id(userProfile.getId())
        .firstName("Jack")
        .build())
    .await().atMost(atMost);

    assertRepo(client, "update-test-cases/create-userprofile-change-first-name.txt");
  }
  
  
  @org.junit.jupiter.api.Test
  public void upsertUserProfile() {
    final var repoName = UserProfileUpdateTest.class.getSimpleName() + "UpsertUserProfile";
    final var client = getClient().repoQuery().repoName(repoName).createIfNot().await().atMost(atMost);
    final var userProfile = createUserProfileForUpdating(client);
    
    final var existingUserProfile = client.createUserProfile().createOne(ImmutableUpsertUserProfile.builder()
        .id(userProfile.getId())
        .email(userProfile.getDetails().getEmail())
        .build())
    .await().atMost(atMost);
    
    log.debug("existing profile: {}", existingUserProfile);

    assertRepo(client, "update-test-cases/upsert-user-profile.txt");
  }
  

}
