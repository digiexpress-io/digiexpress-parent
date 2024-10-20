package io.resys.thena.projects.client.tests.config;

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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.resys.thena.jackson.VertexExtModule;
import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.spi.ProjectStore;
import io.resys.thena.projects.client.spi.ProjectsClientImpl;
import io.resys.thena.spi.ThenaClientPgSql;
import io.resys.thena.structures.git.GitPrinter;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.json.jackson.VertxModule;
import io.vertx.mutiny.sqlclient.Pool;
import jakarta.inject.Inject;

public class ProjectTestCase {
  @Inject io.vertx.mutiny.pgclient.PgPool pgPool;
  public final Duration atMost = Duration.ofMinutes(5);
  
  private ProjectStore store;
  private ProjectsClientImpl client;
  private static final String DB = "junit-tasks-"; 
  private static final AtomicInteger DB_ID = new AtomicInteger();
  private static final Instant targetDate = LocalDateTime.of(2023, 1, 1, 1, 1).toInstant(ZoneOffset.UTC);
  
  @BeforeEach
  public void setUp() {
    waitUntilPostgresqlAcceptsConnections(pgPool);
    final var db = DB + DB_ID.getAndIncrement();
    store = ProjectStore.builder()
        .repoName(db).pgPool(pgPool).pgDb(db)
        .build();
    client = new ProjectsClientImpl(store);
    objectMapper();
    
  }

  private void waitUntilPostgresqlAcceptsConnections(Pool pool) {
    // On some platforms there may be some delay before postgresql starts to respond.
    // Try until postgresql connection is successfully opened.
    var connection = pool.getConnection()
      .onFailure()
      .retry().withBackOff(Duration.ofMillis(10), Duration.ofSeconds(3)).atMost(20)
      .await().atMost(Duration.ofSeconds(60));
    connection.closeAndForget();
  }

  public static ObjectMapper objectMapper() {
    final var modules = new com.fasterxml.jackson.databind.Module[] {
        new JavaTimeModule(), 
        new Jdk8Module(), 
        new GuavaModule(),
        new VertxModule(),
        new VertexExtModule()
        };
      DatabindCodec.mapper().registerModules(modules);
      DatabindCodec.prettyMapper().registerModules(modules);
      
    return DatabindCodec.mapper(); 
  }

  @AfterEach
  public void tearDown() {
    store = null;
  }

  public ProjectStore getStore() {
    return store;
  }

  public ProjectsClientImpl getClient() {
    return client;
  }

  public static Instant getTargetDate() {
    return targetDate;
  }

  public String printRepo(ProjectClient client) {
    final var config = ((ProjectsClientImpl) client).getCtx().getConfig();
    final var state = ((ThenaClientPgSql) config.getClient()).getState();
    final var repo = client.getRepo().await().atMost(Duration.ofMinutes(1));
    final String result = new GitPrinter(state).printWithStaticIds(repo);
    return result;
  }
  
  public String toStaticData(ProjectClient client) {
    final var config = ((ProjectsClientImpl) client).getCtx().getConfig();
    final var state = ((ThenaClientPgSql) config.getClient()).getState();
    final var repo = client.getRepo().await().atMost(Duration.ofMinutes(1));
    return new RepositoryToStaticData(state).print(repo);
  }
  
  public static String toExpectedFile(String fileName) {
    return RepositoryToStaticData.toString(ProjectTestCase.class, fileName);
  }
  
  public void assertRepo(ProjectClient client, String expectedFileName) {
    final var expected = toExpectedFile(expectedFileName);
    final var actual = toStaticData(client);
    Assertions.assertLinesMatch(expected.lines(), actual.lines());
    
  }
  public void assertEquals(String expectedFileName, Object actual) {
    final var expected = toExpectedFile(expectedFileName);
    final var actualJson = JsonObject.mapFrom(actual).encodePrettily();
    Assertions.assertLinesMatch(expected.lines(), actualJson.lines());
    
  }
}
