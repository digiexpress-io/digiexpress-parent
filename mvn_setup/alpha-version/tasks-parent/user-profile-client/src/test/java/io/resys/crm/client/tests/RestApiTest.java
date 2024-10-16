package io.resys.crm.client.tests;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
import io.resys.userprofile.client.api.model.ImmutableCreateUserProfile;
import io.resys.userprofile.client.api.model.ImmutableNotificationSetting;
import io.resys.userprofile.client.api.model.UserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.UserProfileCommandType;


//add this to vm args to run in IDE -Djava.util.logging.manager=org.jboss.logmanager.LogManager

@QuarkusTest
public class RestApiTest {
  
  @Test
  public void getUserProfiles() throws JsonProcessingException {
    final UserProfile[] response = RestAssured.given().when()
      .get("/q/digiexpress/api/userprofiles").then()
      .statusCode(200)
      .contentType("application/json")
      .extract().as(UserProfile[].class);
  
    Assertions.assertEquals("id-1234", response[0].getId());
  }

  
  @Test
  public void postTwoUserProfiles() throws JsonProcessingException {
    final var body = ImmutableCreateUserProfile.builder()
        .id("personid")
        .firstName("user first name")
        .lastName("user last name")
        .username("firstAndLastName")
        .email("firstAndLastName@gmail.com")
        .notificationSettings(Arrays.asList(ImmutableNotificationSetting.builder()
          .type("NEW_MESSAGE_RECEIVED")
          .enabled(true)
        .build()))
    
        .commandType(UserProfileCommandType.CreateUserProfile)
        .build();

      final UserProfile response = RestAssured.given()
        .body(body).accept("application/json").contentType("application/json")
        .when().post("/q/digiexpress/api/userprofiles")
        .then().log().ifValidationFails(LogDetail.ALL)
        .statusCode(200).contentType("application/json")
        .extract().as(UserProfile.class);
    
      Assertions.assertNotNull(response);
  }
 
}
