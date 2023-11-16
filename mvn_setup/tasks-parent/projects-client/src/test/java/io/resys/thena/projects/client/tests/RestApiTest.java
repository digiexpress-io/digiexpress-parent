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
import io.restassured.filter.log.LogDetail;
import io.resys.thena.projects.client.api.model.ImmutableArchiveTenantConfig;
import io.resys.thena.projects.client.api.model.ImmutableChangeTenantConfigInfo;
import io.resys.thena.projects.client.api.model.ImmutableCreateTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.TenantConfigCommandType;


//add this to vm args to run in IDE -Djava.util.logging.manager=org.jboss.logmanager.LogManager

@QuarkusTest
public class RestApiTest {
  
  @Test
  public void getTenants() throws JsonProcessingException {
    final TenantConfig[] response = RestAssured.given().when()
      .get("/q/digiexpress/api/tenants").then()
      .statusCode(200)
      .contentType("application/json")
      .extract().as(TenantConfig[].class);
  
    Assertions.assertEquals("tenant-1", response[0].getId());
  }
  
  @Test
  public void postOneTenant() throws JsonProcessingException {
    final var body = ImmutableCreateTenantConfig.builder()
      .userId("user-1")
      //.targetDate(ProjectTestCase.getTargetDate())
      .commandType(TenantConfigCommandType.CreateTenantConfig)
      .name("name x")
      .repoId("repo-1")
      
      .build();

    final TenantConfig[] response = RestAssured.given()
      .body(Arrays.asList(body)).accept("application/json").contentType("application/json")
      .when().post("/q/digiexpress/api/tenants").then()
      .statusCode(200).contentType("application/json")
      
      .extract().as(TenantConfig[].class);
  
    Assertions.assertEquals("tenant-1", response[0].getId());
  }
  
  @Test
  public void postTwoTenants() throws JsonProcessingException {
    final var body = ImmutableCreateTenantConfig.builder()
        //.targetDate(ProjectTestCase.getTargetDate())
        .name("very important title no: init")
        .repoId("repo-1")
        .userId("user-1")
        .commandType(TenantConfigCommandType.CreateTenantConfig)
        .build();

      final TenantConfig[] response = RestAssured.given()
        .body(Arrays.asList(body, body)).accept("application/json").contentType("application/json")
        .when().post("/q/digiexpress/api/tenants").then()
        .statusCode(200).contentType("application/json")
        .extract().as(TenantConfig[].class);
    
      Assertions.assertEquals(2, response.length);
  }
  
  @Test
  public void updateFourTenants() throws JsonProcessingException {
    final var command = ImmutableChangeTenantConfigInfo.builder()
        //.targetDate(ProjectTestCase.getTargetDate())
        .name("very important title no: init")
        .tenantConfigId("tenant-1")
        .userId("user1")
        .build();
        

      final TenantConfig[] response = RestAssured.given()
        .body(Arrays.asList(command, command, command, command)).accept("application/json").contentType("application/json")
        .when().put("/q/digiexpress/api/tenants").then()
        .statusCode(200).contentType("application/json")
        .extract().as(TenantConfig[].class);
    
      Assertions.assertEquals(4, response.length);
  }
  
  @Test
  public void deleteTenants() throws JsonProcessingException {
    final var command = ImmutableArchiveTenantConfig.builder()
        .tenantConfigId("tenant-1")
        .userId("user1")
        //.targetDate(ProjectTestCase.getTargetDate())
        .build();
        
    
      final TenantConfig[] response = RestAssured.given()
        .body(Arrays.asList(command, command)).accept("application/json").contentType("application/json")
        .when().delete("/q/digiexpress/api/tenants")

        .then().log().ifValidationFails(LogDetail.BODY)
        .statusCode(200).contentType("application/json")
        .extract().as(TenantConfig[].class);
    
      Assertions.assertEquals(2, response.length);
  }  
}
