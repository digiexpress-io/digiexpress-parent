package io.resys.avatar.client.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.resys.avatar.client.api.Avatar;


@QuarkusTest
public class RestApiTest {
  
  @Test
  public void getAvatars() throws JsonProcessingException {
    final Avatar[] response = RestAssured.given().when()
      .get("/q/digiexpress/api/avatars").then()
      .statusCode(200)
      .contentType("application/json")
      .extract().as(Avatar[].class);
  
    Assertions.assertEquals("id-1234", response[0].getId());
  }

  
  @Test
  public void getAvatar() throws JsonProcessingException {
    final Avatar response = RestAssured.given().when()
      .get("/q/digiexpress/api/avatars/1").then()
      .statusCode(200)
      .contentType("application/json")
      .extract().as(Avatar.class);
  
    Assertions.assertEquals("id-1234", response.getId());
  }

   
}
