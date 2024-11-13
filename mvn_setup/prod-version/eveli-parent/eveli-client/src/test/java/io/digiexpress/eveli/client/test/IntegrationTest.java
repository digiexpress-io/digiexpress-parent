package io.digiexpress.eveli.client.test;

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
