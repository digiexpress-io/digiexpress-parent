package io.resys.limaone.tests.support;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class DialobSupport {
  static Network network;
  static PostgreSQLContainer<?> postgres;
  static GenericContainer<?> redis;
  static GenericContainer<?> dialob;

  static String dialobBaseUrl;

  @SuppressWarnings("resource")
  @BeforeAll
  static void setup() {
    network = Network.newNetwork();
    postgres = new PostgreSQLContainer<>("postgres:13")
        .withNetwork(network)
        .withNetworkAliases("postgresql0dialob")
        .withDatabaseName("dialob")
        .withUsername("dialob")
        .withPassword("dialob123");

    redis = new GenericContainer<>("redis:4.0-alpine")
        .withNetwork(network)
        .withNetworkAliases("redis")
        .withExposedPorts(6379)
        .waitingFor(Wait.forListeningPort());

    dialob = new GenericContainer<>("resys/dialob-boot:2.2.9")
        .withNetwork(network)
        .withExposedPorts(8081)
        .withEnv("SPRING_REDIS_HOST", "redis")
        .withEnv("SPRING_PROFILES_ACTIVE", "ui,jdbc,cors")
        .withEnv("SPRING_SESSION_STORE_TYPE", "redis")
        .withEnv("SPRING_SESSION_REDIS_NAMESPACE", "dialobServiceSession")
        .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgresql0dialob/dialob")
        .withEnv("SPRING_DATASOURCE_USERNAME", "dialob")
        .withEnv("SPRING_DATASOURCE_PASSWORD", "dialob123")
        .withEnv("ADMIN_VERSIONING", "true")
        .withEnv("DIALOB_SESSION_POSTSUBMITHANDLER_ENABLED", "true")
        .withEnv("DIALOB_SECURITY_ENABLED", "false")
        .withEnv("SERVER_SERVLET_CONTEXTPATH", "/dialob")
        .withEnv("LOGGING_LEVEL_IO_DIALOB", "INFO")
        .withEnv("SPRING_CLOUD_GCP_CORE_ENABLED", "false")
        .withEnv("SPRING_CLOUD_GCP_LOGGING_ENABLED", "false")
        .withEnv("DIALOB_TENANT_MODE", "URL_PARAM")
        .withEnv("DIALOB_TENANT_FIXED_ID", "00000000-0000-0000-0000-000000000000")
        .waitingFor(Wait.forHttp("/dialob/actuator/health").forPort(8081).forStatusCode(200));

    // explicit ordered startup
    postgres.start();
    redis.start();
    dialob.start();
    dialobBaseUrl = "http://" + dialob.getHost() + ":" + dialob.getMappedPort(8081) + "/dialob";
  }

  @AfterAll
  static void teardown() {
    dialob.stop();
    redis.stop();
    postgres.stop();
    network.close();
  }

  @Test
  void dialobIsUp() {
    System.out.println("Dialob running at: " + dialobBaseUrl);
  }
}