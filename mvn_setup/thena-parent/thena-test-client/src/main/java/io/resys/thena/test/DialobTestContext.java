package io.resys.thena.test;

import java.time.Duration;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
/*-
 * #%L
 * thena-test-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import io.resys.thena.test.DialobTest.FormUrl;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.pgclient.PgBuilder;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class DialobTestContext {
  private Network network;
  private PostgreSQLContainer<?> postgres;
  private GenericContainer<?> redis;
  private GenericContainer<?> dialobForm;
  private GenericContainer<?> dialobSession;
  private String dialobBaseUrl;
  private String dialobSessionUrl;
  
  private Vertx vertx;
  private Pool pool;
  
  @SuppressWarnings("unused")
  private static ValidatorFactory factory = Validation.byDefaultProvider()
      .configure()
      .messageInterpolator(new ParameterMessageInterpolator())
      .buildValidatorFactory();
  
  @SuppressWarnings("resource")
  public void initialize(DialobTest data) {
    if(!data.enabled()) {
      return;
    }
    
    
    
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

    dialobForm = new GenericContainer<>("resys/dialob-boot:2.4.0")
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

    dialobSession = new GenericContainer<>("resys/dialob-session-boot:2.4.0")
        .withNetwork(network)
        .withExposedPorts(8082)
        .withEnv("SERVER_PORT", "8082")
        .withEnv("SPRING_REDIS_HOST", "redis")
        .withEnv("SPRING_PROFILES_ACTIVE", "jdbc,cors")
        .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgresql0dialob/dialob")
        .withEnv("SPRING_DATASOURCE_USERNAME", "dialob")
        .withEnv("SPRING_DATASOURCE_PASSWORD", "dialob123")
        .withEnv("DIALOB_SESSION_POSTSUBMITHANDLER_ENABLED", "true")
        .withEnv("SPRING_CLOUD_GCP_CORE_ENABLED", "false")
        .waitingFor(Wait.forHttp("/session/actuator/health").forPort(8082).forStatusCode(200));
    
    // explicit ordered startup
    postgres.start();
    redis.start();
    dialobForm.start();
    dialobSession.start();
    dialobBaseUrl = "http://" + dialobForm.getHost() + ":" + dialobForm.getMappedPort(8081);
    dialobSessionUrl = "http://" + dialobSession.getHost() + ":" + dialobSession.getMappedPort(8082);    
    
    this.vertx = Vertx.vertx(new VertxOptions());
    this.pool = PgBuilder.pool()
        .with(new PoolOptions().setMaxSize(6))
        .connectingTo(new PgConnectOptions()
          .setDatabase(postgres.getDatabaseName())
          .setHost(postgres.getHost())
          .setPort(postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT))
          .setUser(postgres.getUsername())
          .setPassword(postgres.getPassword()))
        .using(vertx)
        .build();
    
    // some hackaruu
    waitUntilPostgresqlAcceptsConnections(pool);
    
    log.info("Dialob Forms are running at: " + dialobBaseUrl);
    log.info("Dialob Session is running at: " + dialobSessionUrl);
    
    // Print container logs for debugging
    log.info("=== DIALOB FORM LOGS ===");
    log.info(dialobForm.getLogs());
    log.info("=== DIALOB SESSION LOGS ===");  
    log.info(dialobSession.getLogs());
    printEndpoints();
  }

  public void cleanup() {
    
    vertx.close()
    .onFailure()
    .retry().withBackOff(Duration.ofMillis(10), Duration.ofSeconds(3)).atMost(20)
    .await().atMost(Duration.ofSeconds(60));
    
    dialobSession.stop();
    dialobForm.stop();
    redis.stop();
    postgres.stop();
    network.close();
  }
  
  private void waitUntilPostgresqlAcceptsConnections(Pool pool) {
    // On some platforms there may be some delay before postgresql starts to respond.
    // Try until postgresql connection is successfully opened.
    final var connection = pool.getConnection()
      .onFailure()
      .retry().withBackOff(Duration.ofMillis(10), Duration.ofSeconds(3)).atMost(20)
      .await().atMost(Duration.ofSeconds(60));
    connection.closeAndForget();
  }
  
  public FormUrl getFormUrl() {
    return new FormUrl(dialobBaseUrl, dialobSessionUrl);
  }
  
  public void printEndpoints() {
    try {
      String mappings = dialobSession.execInContainer("curl", "-s", "http://localhost:8082/session/actuator/mappings")
          .getStdout();
      log.info("Session endpoints: {}", new JsonObject(mappings).encodePrettily());
    } catch (Exception e) {
      log.warn("Failed to get endpoints", e);
    }
  }
  
  public void clearTestData() {
    pool.withTransaction(tx -> {
      return tx.query("delete from form").execute()
          .flatMap(r -> tx.query("delete from form_archive").execute())
          .flatMap(r -> tx.query("delete from form_rev").execute())
          .flatMap(r -> tx.query("delete from form_rev_archive").execute())
          .flatMap(r -> tx.query("delete from questionnaire").execute())
          .flatMap(r -> tx.query("delete from form_document").execute());
    }).await().indefinitely();
  }
}
