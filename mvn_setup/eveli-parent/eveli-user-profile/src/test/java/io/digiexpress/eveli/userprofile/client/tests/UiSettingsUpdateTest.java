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

import org.junit.jupiter.api.Assertions;

import io.digiexpress.eveli.userprofile.client.api.UserProfileClient;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableCreateUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableNotificationSetting;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableUiSettingForConfig;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableUpsertUiSettings;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfile;
import io.digiexpress.eveli.userprofile.client.tests.config.UserProfilePgProfile;
import io.digiexpress.eveli.userprofile.client.tests.config.UserProfileTestCase;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(UserProfilePgProfile.class)
public class UiSettingsUpdateTest extends UserProfileTestCase {

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
  public void createSettings() {
    final var repoName = UiSettingsUpdateTest.class.getSimpleName() + "update";
    final var client = getClient().repoQuery().repoName(repoName).createIfNot().await().atMost(atMost);
    final var userProfile = createUserProfileForUpdating(client);

    
    final var inserted = client.updateUiSettings().updateOne(ImmutableUpsertUiSettings.builder()
        .userId(userProfile.getId())
        .settingsId("super-config")
        .addConfig(ImmutableUiSettingForConfig.builder().dataId("xxx").value("yyy").build())
        .build())
    .await().atMost(atMost);
    

    final var updated = client.updateUiSettings().updateOne(ImmutableUpsertUiSettings.builder()
        .userId(userProfile.getId())
        .settingsId("super-config")
        .addConfig(ImmutableUiSettingForConfig.builder().dataId("xxxx").value("yyy").build())
        .build())
    .await().atMost(atMost);
    
    final var allProfiles = client.userProfileQuery().findAll().await().atMost(atMost);
    Assertions.assertEquals(1, allProfiles.size());
    
    final var allSettings = client.uiSettingsQuery().findAll(allProfiles.get(0).getId()).await().atMost(atMost);
    Assertions.assertEquals(1, allSettings.size());
    
    
    
  }
}
