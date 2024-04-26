package io.resys.avatar.client.tests;

import org.junit.jupiter.api.Assertions;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.avatar.client.api.Avatar;
import io.resys.avatar.client.api.AvatarClient;
import io.resys.avatar.client.api.ImmutableChangeAvatarColorCode;
import io.resys.avatar.client.api.ImmutableCreateAvatar;
import io.resys.avatar.client.tests.config.AvatarPgProfile;
import io.resys.avatar.client.tests.config.AvatarTestCase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusTest
@TestProfile(AvatarPgProfile.class)
public class AvatarTest extends AvatarTestCase {

  private Avatar createUserProfileForUpdating(AvatarClient client) {
    return client.createAvatar()
      .createOne(ImmutableCreateAvatar.builder()
        .id("jerry-id-1")
        .seedData("jerry@thejerryspringershow.com")
        .externalId("xyz")
        .avatarType("system_user")
        .build())
      .await().atMost(atMost);
  }
  
  
  @SuppressWarnings("unused")
  @org.junit.jupiter.api.Test
  public void changeUserDetailsFirstName() {
    final var repoName = AvatarTest.class.getSimpleName() + "ChangeUserDetailsFirstName";
    final var client = getClient().repoQuery().repoName(repoName).createIfNot().await().atMost(atMost);
    final var userProfile = createUserProfileForUpdating(client);
    
    var updated = client.updateAvatar().updateOne(ImmutableChangeAvatarColorCode.builder()
        .id(userProfile.getId())
        .colorCode("xxxx")
        .build())
    .await().atMost(atMost);

    final var found = client.queryAvatars().get(userProfile.getId()).await().atMost(atMost);
    Assertions.assertEquals("xxxx", found.getColorCode());
    Assertions.assertNotNull(found.getCreated());
    Assertions.assertNotNull(found.getUpdated());
    Assertions.assertNotNull(found.getVersion());
    
  }
  
  

}
