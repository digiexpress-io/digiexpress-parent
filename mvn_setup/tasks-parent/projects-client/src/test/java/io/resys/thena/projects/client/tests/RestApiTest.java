package io.resys.thena.projects.client.tests;

/*-
 * #%L
 * thena-projects-client
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.resys.thena.projects.client.api.model.ImmutableArchiveTenantConfig;
import io.resys.thena.projects.client.api.model.ImmutableAssignProjectUsers;
import io.resys.thena.projects.client.api.model.ImmutableChangeTenantConfig;
import io.resys.thena.projects.client.api.model.ImmutableCreateTenantConfig;
import io.resys.thena.projects.client.api.model.Project;
import io.resys.thena.projects.client.api.model.Project.ProjectType;
import io.resys.thena.projects.client.tests.config.ProjectTestCase;


//add this to vm args to run in IDE -Djava.util.logging.manager=org.jboss.logmanager.LogManager

@QuarkusTest
public class RestApiTest {
  
  @Test
  public void getProjects() throws JsonProcessingException {
    final Project[] response = RestAssured.given().when()
      .get("/q/digiexpress/api/projects").then()
      .statusCode(200)
      .contentType("application/json")
      .extract().as(Project[].class);
  
    Assertions.assertEquals("project1", response[0].getId());
  }
  
  @Test
  public void postOneProject() throws JsonProcessingException {
    final var body = ImmutableCreateTenantConfig.builder()
      //.targetDate(ProjectTestCase.getTargetDate())
      .title("very important title no: init")
      .description("first project ever no: init")
      .repoId("repo-1")
      .repoType(ProjectType.TASKS)
      .addUsers("admin-users", "view-only-users")
      .userId("user-1")
      .build();

    final Project[] response = RestAssured.given()
      .body(Arrays.asList(body)).accept("application/json").contentType("application/json")
      .when().post("/q/digiexpress/api/projects").then()
      .statusCode(200).contentType("application/json")
      
      .extract().as(Project[].class);
  
    Assertions.assertEquals("project1", response[0].getId());
  }
  
  @Test
  public void postTwoProjects() throws JsonProcessingException {
    final var body = ImmutableCreateTenantConfig.builder()
//        .targetDate(ProjectTestCase.getTargetDate())
        .title("very important title no: init")
        .description("first project ever no: init")
        .repoId("repo-1")
        .repoType(ProjectType.TASKS)
        .addUsers("admin-users", "view-only-users")
        .userId("user-1")
        .build();

      final Project[] response = RestAssured.given()
        .body(Arrays.asList(body, body)).accept("application/json").contentType("application/json")
        .when().post("/q/digiexpress/api/projects").then()
        .statusCode(200).contentType("application/json")
        .extract().as(Project[].class);
    
      Assertions.assertEquals(2, response.length);
  }
  
  @Test
  public void updateFourProjects() throws JsonProcessingException {
    final var command = ImmutableChangeTenantConfig.builder()
        .projectId("project1")
        .userId("user1")
        //.targetDate(ProjectTestCase.getTargetDate())
        .title("very important title no: init")
        .description("first project ever no: init")
        .build();
        

      final Project[] response = RestAssured.given()
        .body(Arrays.asList(command, command, command, command)).accept("application/json").contentType("application/json")
        .when().put("/q/digiexpress/api/projects").then()
        .statusCode(200).contentType("application/json")
        .extract().as(Project[].class);
    
      Assertions.assertEquals(4, response.length);
  }
  
  
  @Test
  public void updateOneProject() throws JsonProcessingException {
    final var command = ImmutableAssignProjectUsers.builder()
        .projectId("project1")
        .userId("user1")
        //.targetDate(ProjectTestCase.getTargetDate())
        .addUsers("admin-users", "view-only-users")
        .build();
        

      final Project response = RestAssured.given()
        .body(Arrays.asList(command, command, command, command)).accept("application/json").contentType("application/json")
        .when().put("/q/digiexpress/api/projects/2").then()
        .statusCode(200).contentType("application/json")
        .extract().as(Project.class);
    
      Assertions.assertEquals("project1", response.getId());
  }
  
  @Test
  public void deleteProjects() throws JsonProcessingException {
    final var command = ImmutableArchiveTenantConfig.builder()
        .projectId("project1")
        .userId("user1")
        //.targetDate(ProjectTestCase.getTargetDate())
        .build();
        
    
      final Project[] response = RestAssured.given()
          .body(Arrays.asList(command, command)).accept("application/json").contentType("application/json")
          .when().delete("/q/digiexpress/api/projects").then()
        .statusCode(200).contentType("application/json")
        .extract().as(Project[].class);
    
      Assertions.assertEquals(2, response.length);
  }  
}
