package io.resys.permission.client.tests;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.resys.permission.client.api.model.ImmutableChangePermissionName;
import io.resys.permission.client.api.model.ImmutableChangeRoleName;
import io.resys.permission.client.api.model.ImmutableCreatePermission;
import io.resys.permission.client.api.model.PermissionCommand.PermissionUpdateCommand;
import io.resys.permission.client.api.model.Principal.Permission;
import jakarta.inject.Inject;

@QuarkusTest
public class RestApiTest {

  @Inject
  private ObjectMapper om;
  
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
  public void getOnePermission() throws JsonProcessingException {
    
    final Permission response = RestAssured.given()
      .get("/q/digiexpress/api/permissions/permissionId-1").then()
      .statusCode(200).contentType("application/json")
      .extract().as(Permission.class);
  
    Assertions.assertEquals("permissionId-1", response.getId());
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
  
  @Test
  public void updateOnePermission() throws JsonProcessingException {    

    final var body = ImmutableChangePermissionName.builder()
      .id("permissionId-1")
      .name("Changed permission name")
      .comment("updated permission name")
      .build();
    
    // crash test JSON
    final var bodyAsString = om.writeValueAsString(Arrays.asList(body));
    om.readValue(bodyAsString, new TypeReference<List<PermissionUpdateCommand>>(){});

    
    final Permission response = RestAssured.given()
      .body(Arrays.asList(body, body)).accept("application/json").contentType("application/json")
      .when().put("/q/digiexpress/api/permissions/permissionId-1").then()
      .statusCode(200).contentType("application/json")
      .extract().as(Permission.class);
    Assertions.assertEquals("permissionId-1", response.getId());
    
  } 
  
  @Disabled
  @Test
  public void updateOneRole() throws JsonProcessingException {    

    final var body = ImmutableChangeRoleName.builder()
      .id("permissionId-1")
      .name("Changed permission name")
      .comment("updated permission name")
      .build();
    
  
    final Permission response = RestAssured.given()
      .body(Arrays.asList(body, body)).accept("application/json").contentType("application/json")
      .when().put("/q/digiexpress/api/roles/permissionId-1").then()
      .log().body()
      .statusCode(200).contentType("application/json")
      .extract().as(Permission.class);
    Assertions.assertEquals("permissionId-1", response.getId());
    
  }  
}
