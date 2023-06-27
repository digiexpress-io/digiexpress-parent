package io.resys.thena.tasks.tests;

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
      .get("/q/tasks/api/projects").then()
      .statusCode(200).contentType("application/json")
      .extract().as(Project[].class);

    Assertions.assertEquals("project1", response[0].getId());
  }
  
  @Test
  public void getTasks() throws JsonProcessingException {
    final Task[] response = RestAssured.given().when()
      .get("/q/tasks/api/projects/1/tasks/").then()
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
      .when().post("/q/tasks/api/projects/1/tasks").then()
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
        .when().post("/q/tasks/api/projects/1/tasks").then()
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
        .when().put("/q/tasks/api/projects/1/tasks").then()
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
        .when().put("/q/tasks/api/projects/1/tasks/2").then()
        .statusCode(200).contentType("application/json")
        .extract().as(Task.class);
    
      Assertions.assertEquals("task1", response.getId());
  }
  
  @Test
  public void findArchivedTasks() throws JsonProcessingException {

      final Task[] response = RestAssured.given().when()
          .get("/q/tasks/api/projects/1/archive/2022-11-09/tasks").then()
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
          .body(command).accept("application/json").contentType("application/json")
          .when().delete("/q/tasks/api/projects/1/tasks/1").then()
        .statusCode(200).contentType("application/json")
        .extract().as(Task.class);
    
      Assertions.assertEquals("task1", response.getId());
  }  
}
