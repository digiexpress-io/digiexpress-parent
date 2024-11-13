package io.digiexpress.eveli.client.test;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import io.digiexpress.eveli.client.config.EveliAutoConfigDB;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.DriverManager;
import java.sql.SQLException;


@Testcontainers
@SpringBootTest
@EnableAutoConfiguration
@ContextConfiguration(classes = {EveliAutoConfigDB.class, IntegrationTest.IntegrationTestConfig.class})
public class IntegrationTest {

  @Autowired
  TaskRepository taskRepository;

  public static class IntegrationTestConfig {
    @Bean
    @ServiceConnection(name = "postgres")
    public PostgreSQLContainer<?> postgresContainer() {
      var container = new PostgreSQLContainer<>("postgres:17");
      container.start();
      for (int i = 0; i < 100; i++) {
        try (var c = DriverManager.getConnection(container.getJdbcUrl(), container.getUsername(), container.getPassword())) {
          break;
        } catch (SQLException e) {
          try {
            Thread.sleep(100);
          } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
          }
        }
      }
      return container;
    }
  }

  @Test
  void hbSchemaValidations() {
    Assertions.assertTrue(true, "Auto config test that loads JPA DB and repository queries for validations, if it starts then everything OK!");
  }
}
