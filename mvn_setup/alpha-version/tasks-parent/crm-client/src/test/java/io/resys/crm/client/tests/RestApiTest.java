package io.resys.crm.client.tests;

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
import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.CustomerCommand.CustomerCommandType;
import io.resys.crm.client.api.model.ImmutableChangeCustomerFirstName;
import io.resys.crm.client.api.model.ImmutableCreateCustomer;
import io.resys.crm.client.api.model.ImmutablePerson;


//add this to vm args to run in IDE -Djava.util.logging.manager=org.jboss.logmanager.LogManager

@QuarkusTest
public class RestApiTest {
  
  @Test
  public void getCustomers() throws JsonProcessingException {
    final Customer[] response = RestAssured.given().when()
      .get("/q/digiexpress/api/customers").then()
      .statusCode(200)
      .contentType("application/json")
      .extract().as(Customer[].class);
  
    Assertions.assertEquals("id-1234", response[0].getId());
  }

  
  @Test
  public void postTwoCustomers() throws JsonProcessingException {
    final var body = ImmutableCreateCustomer.builder()
        .body(ImmutablePerson.builder()
            .firstName("Waldorf")
            .lastName("SaladsMacgoo")
            .username("Waldorf SaladsMacGoo")
            .build())
        .externalId("ssn")
        .commandType(CustomerCommandType.CreateCustomer)
        .build();

      final Customer response = RestAssured.given()
        .body(body).accept("application/json").contentType("application/json")
        .when().post("/q/digiexpress/api/customers")
        .then().log().ifValidationFails(LogDetail.ALL)
        .statusCode(200).contentType("application/json")
        .extract().as(Customer.class);
    
      Assertions.assertNotNull(response);
  }
  
  @Test
  public void updateFourCustomers() throws JsonProcessingException {
    final var command = ImmutableChangeCustomerFirstName.builder()
        .firstName("John")
        .customerId("customer-id-1")
        .build();

      final Customer response = RestAssured.given()
        .body(Arrays.asList(command, command, command, command)).accept("application/json").contentType("application/json")
        .when().put("/q/digiexpress/api/customers/customer-id-1")
        .then().log().ifValidationFails(LogDetail.BODY)
        .statusCode(200).contentType("application/json")
        .extract().as(Customer.class);
    
      Assertions.assertNotNull(response);
  }
 
}
