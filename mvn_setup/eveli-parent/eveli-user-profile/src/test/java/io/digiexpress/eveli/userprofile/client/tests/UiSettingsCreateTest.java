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

import io.digiexpress.eveli.userprofile.client.api.model.ImmutableCreateUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableNotificationSetting;
import io.digiexpress.eveli.userprofile.client.tests.config.UserProfileTestCase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UiSettingsCreateTest extends UserProfileTestCase {


  @org.junit.jupiter.api.Test
  public void createSettings_1() {
    final var repoName = UiSettingsCreateTest.class.getSimpleName();
    final var client = getClient().repoQuery().repoName(repoName).createIfNot().await().atMost(atMost);
    final var userProfile = client.createUserProfile()
        .createOne(ImmutableCreateUserProfile.builder()
            .id("jerry-id-1")
            .email("jerry@thejerryspringershow.com")
            .notificationSettings(Arrays.asList(ImmutableNotificationSetting.builder()
                .type("TASK_ASSIGNED")
                .enabled(true)
                .build()))
            
            .build())
          .await().atMost(atMost);

    Assertions.assertEquals("Jerry", userProfile.getDetails().getFirstName());
    Assertions.assertEquals("", userProfile.getDetails().getLastName());
    Assertions.assertEquals("jerry", userProfile.getDetails().getUsername());
  }
  
  @org.junit.jupiter.api.Test
  public void createSettings_2() {
    final var repoName = UiSettingsCreateTest.class.getSimpleName();
    final var client = getClient().repoQuery().repoName(repoName).createIfNot().await().atMost(atMost);
    final var userProfile = client.createUserProfile()
        .createOne(ImmutableCreateUserProfile.builder()
            .id("jerry-id-2")
            .email("jerry.springer@thejerryspringershow.com")
            .notificationSettings(Arrays.asList(ImmutableNotificationSetting.builder()
                .type("TASK_ASSIGNED")
                .enabled(true)
                .build()))
            
            .build())
          .await().atMost(atMost);

    Assertions.assertEquals("Jerry", userProfile.getDetails().getFirstName());
    Assertions.assertEquals("Springer", userProfile.getDetails().getLastName());
    Assertions.assertEquals("jerry.springer", userProfile.getDetails().getUsername());
  }
}
