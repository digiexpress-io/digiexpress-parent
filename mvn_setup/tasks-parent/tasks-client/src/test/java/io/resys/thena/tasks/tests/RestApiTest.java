package io.resys.thena.tasks.tests;

/*-
 * #%L
 * thena-tasks-client
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
import io.resys.thena.tasks.client.api.model.ImmutableArchiveTask;
import io.resys.thena.tasks.client.api.model.ImmutableChangeTaskStatus;
import io.resys.thena.tasks.client.api.model.ImmutableCreateTask;
import io.resys.thena.tasks.client.api.model.Project;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.Task.Priority;
import io.resys.thena.tasks.tests.config.TaskTestCase;


//add this to vm args to run in IDE -Djava.util.logging.manager=org.jboss.logmanager.LogManager

@QuarkusTest
public class RestApiTest {

  
  @Test
  public void projects() throws JsonProcessingException {

    final Project[] response = RestAssured.given().when()
      .get("/q/digiexpress/api/projects").then()
      .statusCode(200).contentType("application/json")
      .extract().as(Project[].class);

    Assertions.assertEquals("project1", response[0].getId());
  }
  
  @Test
  public void getTasks() throws JsonProcessingException {
    final Task[] response = RestAssured.given().when()
      .get("/q/digiexpress/api/tasks").then()
      .statusCode(200)
      .contentType("application/json")
      .extract().as(Task[].class);
  
    Assertions.assertEquals("task1", response[0].getId());
  }
  
  @Test
  public void postOneTask() throws JsonProcessingException {
    final var body = ImmutableCreateTask.builder()
      .targetDate(TaskTestCase.getTargetDate())
      .title("very important title no: init")
      .description("first task ever no: init")
      .priority(Priority.LOW)
      .addRoles("admin-users", "view-only-users")
      .userId("user-1")
      .reporterId("reporter-1")
      .build();

    final Task[] response = RestAssured.given()
      .body(Arrays.asList(body)).accept("application/json").contentType("application/json")
      .when().post("/q/digiexpress/api/tasks").then()
      .statusCode(200).contentType("application/json")
      .extract().as(Task[].class);
  
    Assertions.assertEquals("task1", response[0].getId());
  }
  
  @Test
  public void postTwoTasks() throws JsonProcessingException {
    final var body = ImmutableCreateTask.builder()
        .targetDate(TaskTestCase.getTargetDate())
        .title("very important title no: init")
        .description("first task ever no: init")
        .priority(Priority.LOW)
        .addRoles("admin-users", "view-only-users")
        .userId("user-1")
        .reporterId("reporter-1")
        .build();

      final Task[] response = RestAssured.given()
        .body(Arrays.asList(body, body)).accept("application/json").contentType("application/json")
        .when().post("/q/digiexpress/api/tasks").then()
        .statusCode(200).contentType("application/json")
        .extract().as(Task[].class);
    
      Assertions.assertEquals(2, response.length);
  }
  
  @Test
  public void updateFourTasks() throws JsonProcessingException {
    final var command = ImmutableChangeTaskStatus.builder()
        .taskId("task1")
        .userId("user1")
        .targetDate(TaskTestCase.getTargetDate())
        .status(Task.Status.IN_PROGRESS)
        .build();
        

      final Task[] response = RestAssured.given()
        .body(Arrays.asList(command, command, command, command)).accept("application/json").contentType("application/json")
        .when().put("/q/digiexpress/api/tasks").then()
        .statusCode(200).contentType("application/json")
        .extract().as(Task[].class);
    
      Assertions.assertEquals(4, response.length);
  }
  
  
  @Test
  public void updateOneTask() throws JsonProcessingException {
    final var command = ImmutableChangeTaskStatus.builder()
        .taskId("task1")
        .userId("user1")
        .targetDate(TaskTestCase.getTargetDate())
        .status(Task.Status.IN_PROGRESS)
        .build();
        

      final Task response = RestAssured.given()
        .body(Arrays.asList(command, command, command, command)).accept("application/json").contentType("application/json")
        .when().put("/q/digiexpress/api/tasks/2").then()
        .statusCode(200).contentType("application/json")
        .extract().as(Task.class);
    
      Assertions.assertEquals("task1", response.getId());
  }
  
  @Test
  public void findArchivedTasks() throws JsonProcessingException {

      final Task[] response = RestAssured.given().when()
          .get("/q/digiexpress/api/archive/2022-11-09").then()
        .statusCode(200).contentType("application/json")
        .extract().as(Task[].class);
    
      Assertions.assertEquals(2, response.length);
  }
  
  @Test
  public void deleteOneTask() throws JsonProcessingException {
    final var command = ImmutableArchiveTask.builder()
        .taskId("task1")
        .userId("user1")
        .targetDate(TaskTestCase.getTargetDate())
        .build();
        
    
      final Task response = RestAssured.given()
          .body(Arrays.asList(command)).accept("application/json").contentType("application/json")
          .when().delete("/q/digiexpress/api/tasks/1").then()
        .statusCode(200).contentType("application/json")
        .extract().as(Task.class);
    
      Assertions.assertEquals("task1", response.getId());
  }  
  
  @Test
  public void deleteTasks() throws JsonProcessingException {
    final var command = ImmutableArchiveTask.builder()
        .taskId("task1")
        .userId("user1")
        .targetDate(TaskTestCase.getTargetDate())
        .build();
        
    
      final Task[] response = RestAssured.given()
          .body(Arrays.asList(command, command)).accept("application/json").contentType("application/json")
          .when().delete("/q/digiexpress/api/tasks").then()
        .statusCode(200).contentType("application/json")
        .extract().as(Task[].class);
    
      Assertions.assertEquals(2, response.length);
  }  
}
