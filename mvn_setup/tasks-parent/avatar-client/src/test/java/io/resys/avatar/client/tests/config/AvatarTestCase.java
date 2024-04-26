package io.resys.avatar.client.tests.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.resys.avatar.client.api.AvatarClient;
import io.resys.avatar.client.spi.AvatarClientImpl;
import io.resys.avatar.client.spi.AvatarStore;
import io.resys.thena.jackson.VertexExtModule;
import io.resys.thena.spi.ThenaClientPgSql;
import io.resys.thena.support.DocDbPrinter;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.json.jackson.VertxModule;
import io.vertx.mutiny.sqlclient.Pool;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AvatarTestCase {
  @Inject io.vertx.mutiny.pgclient.PgPool pgPool;
  public final Duration atMost = Duration.ofMinutes(5);
  
  private AvatarStore store;
  private AvatarClientImpl client;
  private static final String DB = "junit-crm-"; 
  private static final AtomicInteger DB_ID = new AtomicInteger();
  private static final Instant targetDate = LocalDateTime.of(2023, 1, 1, 1, 1).toInstant(ZoneOffset.UTC);
  
  @BeforeEach
  public void setUp() {
    waitUntilPostgresqlAcceptsConnections(pgPool);
    final var db = DB + DB_ID.getAndIncrement();
    store = AvatarStore.builder()
        .repoName(db).pgPool(pgPool).pgDb(db)
      
        .build();
    client = new AvatarClientImpl(store);
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

  public void assertCommits(String repoName) {
    final var config = getStore().getConfig();
    final var commits = config.getClient().git(repoName).commit().findAllCommits().await().atMost(atMost);
    log.debug("Total commits: {}", commits.size());
    
  }
  
  @AfterEach
  public void tearDown() {
    store = null;
  }

  public AvatarStore getStore() {
    return store;
  }

  public AvatarClientImpl getClient() {
    return client;
  }

  public static Instant getTargetDate() {
    return targetDate;
  }

  public String printRepo(AvatarClient client) {
    final var config = ((AvatarClientImpl) client).getCtx().getConfig();
    final var state = ((ThenaClientPgSql) config.getClient()).getState();
    final var repo = client.getTenant().await().atMost(Duration.ofMinutes(1));
    final String result = new DocDbPrinter(state).printWithStaticIds(repo);
    return result;
  }
  
  public String toStaticData(AvatarClient client) {
    final var config = ((AvatarClientImpl) client).getCtx().getConfig();
    final var state = ((ThenaClientPgSql) config.getClient()).getState();
    final var repo = client.getTenant().await().atMost(Duration.ofMinutes(1));
    return new DocDbPrinter(state).printWithStaticIds(repo);
  }
  
  public static String toExpectedFile(String fileName) {
    return toString(AvatarTestCase.class, fileName);
  }
  
  public void assertRepo(AvatarClient client, String expectedFileName) {
    final var expected = toExpectedFile(expectedFileName);
    final var actual = toStaticData(client);
    Assertions.assertLinesMatch(expected.lines(), actual.lines(), actual);
    
  }
  public void assertEquals(String expectedFileName, Object actual) {
    final var expected = toExpectedFile(expectedFileName);
    final var actualJson = JsonObject.mapFrom(actual).encodePrettily();
    Assertions.assertLinesMatch(expected.lines(), actualJson.lines());
    
  }
  
  public static String toString(Class<?> type, String resource) {
    try {
      return IOUtils.toString(type.getClassLoader().getResource(resource), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
