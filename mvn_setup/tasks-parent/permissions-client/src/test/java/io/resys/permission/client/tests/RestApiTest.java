package io.resys.permission.client.tests;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.resys.permission.client.api.model.ImmutableCreatePermission;
import io.resys.permission.client.api.model.Principal.Permission;

@QuarkusTest
public class RestApiTest {

  @Test
  public void findAllPermissions() throws JsonProcessingException {
    
    final Permission[] response = RestAssured.given().when()
      .get("/q/digiexpress/api/permissions").then()
      .statusCode(200)
      .contentType("application/json")
      .extract().as(Permission[].class);
  
    Assertions.assertEquals("permissionId-1", response[0].getId());
  }
  
  
  @Test
  public void postOnePermission() throws JsonProcessingException {    
    
    final var body = ImmutableCreatePermission.builder()
      .name("FIRST permission")
      .description("my first permission")
      .comment("created first permission")
      .roles(Arrays.asList("role1", "role2"))
      .build();

    final Permission response = RestAssured.given()
      .body(body).accept("application/json").contentType("application/json")
      .when().post("/q/digiexpress/api/permissions").then()
      .statusCode(200).contentType("application/json")
      .extract().as(Permission.class);
  
    Assertions.assertEquals("permissionId-1", response.getId());
  }
  
  
    
}
