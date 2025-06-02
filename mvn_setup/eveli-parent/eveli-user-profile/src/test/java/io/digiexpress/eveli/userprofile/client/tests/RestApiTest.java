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
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.digiexpress.eveli.userprofile.client.api.model.ImmutableCreateUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableNotificationSetting;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfileCommand.UserProfileCommandType;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;


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
