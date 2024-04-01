package io.resys.permission.client.tests;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.resys.permission.client.api.model.ImmutableChangePermissionName;
import io.resys.permission.client.api.model.ImmutableChangePrincipalStatus;
import io.resys.permission.client.api.model.ImmutableChangeRoleName;
import io.resys.permission.client.api.model.ImmutableCreatePermission;
import io.resys.permission.client.api.model.ImmutableCreateRole;
import io.resys.permission.client.api.model.PermissionCommand.PermissionUpdateCommand;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.thena.api.entities.org.OrgActorStatus.OrgActorStatusType;
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
  
  @Test
  public void findAllRoles() throws JsonProcessingException {
    
    final Role[] response = RestAssured.given().when()
      .get("/q/digiexpress/api/roles").then()
      .statusCode(200)
      .contentType("application/json")
      .extract().as(Role[].class);
  
    Assertions.assertEquals("roleId-1", response[0].getId());
  }
  
  @Test
  public void getOneRole() throws JsonProcessingException {
    
    final Role response = RestAssured.given()
      .get("/q/digiexpress/api/roles/roleId-1").then()
      .statusCode(200).contentType("application/json")
      .extract().as(Role.class);
  
    Assertions.assertEquals("roleId-1", response.getId());
  }
  
  @Test
  public void updateOneRole() throws JsonProcessingException {    

    final var body = ImmutableChangeRoleName.builder()
      .id("roleId-1")
      .name("Awesome role name")
      .comment("User-updated role name")
      .build();
    
    final Role response = RestAssured.given()
      .body(Arrays.asList(body, body)).accept("application/json").contentType("application/json")
      .when().put("/q/digiexpress/api/roles/roleId-1").then()
      .log().body()
      .statusCode(200).contentType("application/json")
      .extract().as(Role.class);
    Assertions.assertEquals("roleId-1", response.getId());
  }

  @Test
  public void postOneRole() throws JsonProcessingException {    
    
    final var body = ImmutableCreateRole.builder()
      .name("ROLE ONE")
      .description("my first role")
      .comment("I created my first role")
      .build();

    final Role response = RestAssured.given()
      .body(body).accept("application/json").contentType("application/json")
      .when().post("/q/digiexpress/api/roles").then()
      .statusCode(200).contentType("application/json")
      .extract().as(Role.class);
  
    Assertions.assertEquals("roleId-1", response.getId());
  }

  @Test
  public void findAllPrincipals() {
    
    final Principal[] response = RestAssured.given()
        .get("/q/digiexpress/api/principals").then()
        .contentType("application/json")
        .statusCode(200).contentType("application/json")
        .extract().as(Principal[].class);
    
    Assertions.assertEquals("principalId-1", response[0].getId());
  }
  
  public void getOnePrincipal() {
    
    final Principal response = RestAssured.given()
        .get("/q/digiexpress/api/principals/principalId-1").then()
        .contentType("application/json")
        .statusCode(200).contentType("application/json")
        .extract().as(Principal.class);
    
    Assertions.assertEquals("principalId-1", response.getId());

  }
  
  public void updateOnePrincipal() {
    
    final var body = ImmutableChangePrincipalStatus.builder()
        .id("principalId-1")
        .comment("User is no longer active in the system")
        .status(OrgActorStatusType.DISABLED)
        .build();
    
    final Principal response = RestAssured.given()
        .body(body).accept("application/json").contentType("application/json")
        .get("q/digiexpress/api/principals/principalId-1").then()
        .statusCode(200).contentType("application/json")
        .extract().as(Principal.class);
    
    Assertions.assertEquals("DISABLED", response.getStatus());
  }
  
}









