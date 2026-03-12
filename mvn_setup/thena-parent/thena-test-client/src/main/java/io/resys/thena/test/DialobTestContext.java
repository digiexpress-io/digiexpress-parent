package io.resys.thena.test;

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
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class DialobTestContext {
  static Network network;
  static PostgreSQLContainer<?> postgres;
  static GenericContainer<?> redis;
  static GenericContainer<?> dialob;

  static String dialobBaseUrl;

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
    
    log.info("Dialob Forms are running at: " + dialobBaseUrl);
  }

  public void cleanup() {
    dialob.stop();
    redis.stop();
    postgres.stop();
    network.close();
  }
  
  public FormUrl getFormUrl() {
    return new FormUrl(dialobBaseUrl);
  }
}
