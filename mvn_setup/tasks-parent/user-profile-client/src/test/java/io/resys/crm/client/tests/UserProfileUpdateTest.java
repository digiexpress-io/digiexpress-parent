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
import io.resys.userprofile.client.api.model.ImmutableUserDetails;
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
        .targetDate(getTargetDate())
        .userId("userId1234")
        .details(ImmutableUserDetails.builder()
            .firstName("Jerry")
            .lastName("Springer")
            .username("jerryspringer")
            .email("jerry@thejerryspringershow.com")
            .build())

        .notificationSettings(Arrays.asList(ImmutableNotificationSetting.builder()
            .type("TASK_ASSIGNED")
            .enabled(true)
            .build()))
        
        .build())
      .await().atMost(atMost);
  }
  
  
  @org.junit.jupiter.api.Test
  public void changeUserDetailsFirstName() {
    final var repoName = UserProfileUpdateTest.class.getSimpleName() + "ChangeUserDetailsFirstName";
    final var client = getClient().repoQuery().repoName(repoName).createIfNot().await().atMost(atMost);
    final var userProfile = createUserProfileForUpdating(client);
    
    client.updateUserProfile().updateOne(ImmutableChangeUserDetailsFirstName.builder()
        .userId("userId1234")
        .id(userProfile.getId())
        .firstName("Jack")
        .targetDate(getTargetDate())
        .build())
    .await().atMost(atMost);

    assertRepo(client, "update-test-cases/create-userprofile-change-first-name.txt");
  }
  
  /*
  @org.junit.jupiter.api.Test
  public void upsertSuomiFiPerson() {
    final var repoName = UserProfileUpdateTest.class.getSimpleName() + "UpsertSuomiFiPerson";
    final var client = getClient().repoQuery().repoName(repoName).createIfNot().await().atMost(atMost);
    final var customer = createUserProfileForUpdating(client);
    
    client.updateUserProfile().updateOne(ImmutableUpsertSuomiFiPerson.builder()
        .userId("tester-bob")
        .userName("Jack Brachus")
        .customerId(customer.getExternalId())
        .protectionOrder(true)
        .firstName("Jack")
        .lastName("Brachus")
        .contact(ImmutableCustomerContact.builder()
            .address(ImmutableCustomerAddress.builder()
                .country("FI")
                .locality("Helsinki")
                .street("1234 My street")
                .postalCode("12345")
                .build())
            .addressValue("1234 My street, Helsinki, FI, 12345")
            .email("suomi-fi-customer@gmail.com")
            .build())
        .targetDate(getTargetDate())
        .build())
    .await().atMost(atMost);

    assertRepo(client, "update-test-cases/upsert-suomi-fi-person.txt");
  }

  @org.junit.jupiter.api.Test
  public void upsertSuomiFiPersonChangeAddress() {
    final var repoName = UserProfileUpdateTest.class.getSimpleName() + "UpsertSuomiFiPersonChangeAddress";
    final var client = getClient().repoQuery().repoName(repoName).createIfNot().await().atMost(atMost);
    final var customer = createUserProfileForUpdating(client);
    
    client.updateUserProfile().updateOne(ImmutableUpsertSuomiFiPerson.builder()
        .userId("tester-bob")
        .userName("Jack Brachus")
        .customerId(customer.getExternalId())
        .protectionOrder(true)
        .firstName("Jack")
        .lastName("Brachus")
        .contact(ImmutableCustomerContact.builder()
            .address(ImmutableCustomerAddress.builder()
                .country("FI")
                .locality("Sipoo")
                .street("35 Lake Avenue")
                .postalCode("85477")
                .build())
            .addressValue("35 Lake Avenue, Sipoo, FI, 85477")
            .email("suomi-fi-customer@gmail.com")
            .build())
        .targetDate(getTargetDate())
        .build())
    .await().atMost(atMost);

    assertRepo(client, "update-test-cases/upsert-suomi-fi-person-change-address.txt");
  }
*/

}
