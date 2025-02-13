package io.resys.crm.client.tests;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.crm.client.tests.config.UserProfilePgProfile;
import io.resys.crm.client.tests.config.UserProfileTestCase;
import io.resys.userprofile.client.api.UserProfileClient;
import io.resys.userprofile.client.api.model.ImmutableCreateUserProfile;
import io.resys.userprofile.client.api.model.ImmutableNotificationSetting;
import io.resys.userprofile.client.api.model.ImmutableUiSettingForConfig;
import io.resys.userprofile.client.api.model.ImmutableUpsertUiSettings;
import io.resys.userprofile.client.api.model.UserProfile;
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
