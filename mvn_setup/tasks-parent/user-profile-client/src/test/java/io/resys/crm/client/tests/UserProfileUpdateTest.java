package io.resys.crm.client.tests;

import java.util.Arrays;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.crm.client.tests.config.UserProfilePgProfile;
import io.resys.crm.client.tests.config.UserProfileTestCase;
import io.resys.userprofile.client.api.UserProfileClient;
import io.resys.userprofile.client.api.model.ImmutableChangeUserDetailsFirstName;
import io.resys.userprofile.client.api.model.ImmutableCreateUserProfile;
import io.resys.userprofile.client.api.model.ImmutableNotificationSetting;
import io.resys.userprofile.client.api.model.ImmutableUpsertUserProfile;
import io.resys.userprofile.client.api.model.UserProfile;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(UserProfilePgProfile.class)
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
