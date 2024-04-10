package io.resys.thena.tasks.tests.config;

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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.graalvm.polyglot.HostAccess.Export;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.jackson.VertexExtModule;
import io.resys.thena.spi.DbState;
import io.resys.thena.storesql.DbStateSqlImpl;
import io.resys.thena.structures.grim.GrimPrinter;
import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.thenamission.TaskStoreImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.json.jackson.VertxModule;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import jakarta.inject.Inject;

public class TaskTestCase {
  private boolean STORE_TO_DEBUG_DB = false;
  @Inject io.vertx.mutiny.pgclient.PgPool pgPool;
  @Inject io.vertx.mutiny.core.Vertx vertx;
  public final Duration atMost = Duration.ofSeconds(20);
  
  private TaskClient client;
  private TaskStoreImpl store;
  private AtomicInteger task_index;
  private static final String DB = "junit-tasks-"; 
  private static final AtomicInteger DB_ID = new AtomicInteger();
  private static final Instant targetDate = LocalDateTime.of(2023, 1, 1, 1, 1).toInstant(ZoneOffset.UTC);  
  private final Map<String, String> replacements = new HashMap<>();

  private void connectToDebugDb() {
    if(!STORE_TO_DEBUG_DB) {
      return;
    }
    
    final var connectOptions = new PgConnectOptions()
        .setDatabase("debug_task_db")
        .setHost("localhost")
        .setPort(5432)
        .setUser("postgres")
        .setPassword("postgres");
    final var poolOptions = new PoolOptions().setMaxSize(6);
    this.pgPool = io.vertx.mutiny.pgclient.PgPool.pool(vertx, connectOptions, poolOptions);
  }

  public void replaceWithStatic(Object input) {
    if(input == null) {
      return;
    }
    
    if(input instanceof Instant) {
      final var value = JsonObject.of("targetDate", input).getString("targetDate");
      replacements.put(value, JsonObject.of("targetDate", targetDate).getString("targetDate"));
      return;
    }
    final var value = task_index.incrementAndGet() + "_TASK";
    replacements.put(input+ "", value);

  }
  
  
  
  @BeforeEach
  public void setUp() {
    task_index = new AtomicInteger(0);
    replacements.clear();
    connectToDebugDb();
    waitUntilPostgresqlAcceptsConnections(pgPool);
    final var db = DB + DB_ID.getAndIncrement();
    store = TaskStoreImpl.builder()
        .repoName(db).pgPool(pgPool).pgDb(db)
        .build();
    client = new io.resys.thena.tasks.client.thenamission.TaskClientImpl(store);
    
    objectMapper();
    
  }

  private void waitUntilPostgresqlAcceptsConnections(Pool pool) {
    // On some platforms there may be some delay before postgresql starts to respond.
    // Try until postgresql connection is successfully opened.
    var connection = pool.getConnection()
      .onFailure()
      .retry().withBackOff(Duration.ofMillis(10), Duration.ofSeconds(3)).atMost(20)
      .await().atMost(Duration.ofSeconds(10));
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
      DatabindCodec.mapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      //DatabindCodec.prettyMapper().registerModules(modules);
      
    return DatabindCodec.mapper(); 
  }
  
  @AfterEach
  public void tearDown() {
    store = null;
  }

  public TaskClient getClient() {
    return client;
  }

  public static Instant getTargetDate() {
    return targetDate;
  }
  public DbState createState() {
    final var ctx = TenantTableNames.defaults("");
    return DbStateSqlImpl.create(ctx, pgPool);
  }
  
  public String toStaticData(TaskClient client) {
    final var repo = client.repo().getRepo().await().atMost(Duration.ofMinutes(1));
    return new GrimPrinter(createState()).printWithStaticIds(repo, replacements);
  }
  
  public static String toExpectedFile(String fileName) {
    return toString(TaskTestCase.class, fileName);
  }
 
  private static String toString(Class<?> type, String resource) {
    try {
      return IOUtils.toString(type.getClassLoader().getResource(resource), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  public void assertTenant(TaskClient client, String expectedFileName) {
    final var expected = toExpectedFile(expectedFileName);
    final var actual = toStaticData(client);
    Assertions.assertEquals(expected, actual);
    
  }
  public void assertTaskJson(String expectedFileName, Task actual) {
    replaceWithStatic(actual.getId());
    replaceWithStatic(actual.getParentId());
    replaceWithStatic(actual.getVersion());
    replaceWithStatic(actual.getCreated());
    replaceWithStatic(actual.getUpdated());
    actual.getChecklist().forEach(e -> {
      replaceWithStatic(e.getId());  
      e.getItems().forEach(i -> replaceWithStatic(i.getId()));
    });
    actual.getComments().forEach(e -> {
      replaceWithStatic(e.getId());
      replaceWithStatic(e.getCreated());
    });
    actual.getExtensions().forEach(e -> {
      replaceWithStatic(e.getId());
      replaceWithStatic(e.getCreated());
      replaceWithStatic(e.getUpdated());
      
    });
    actual.getTransactions().forEach(e -> {
      replaceWithStatic(e.getId());
      for(final var c : e.getCommands()) {
        replaceWithStatic(JsonObject.mapFrom(c).getString("checklistId"));
        replaceWithStatic(JsonObject.mapFrom(c).getString("checklistItemId"));
      }
    });
    replaceWithStatic(actual.getTreeVersion());
    var actualJson = JsonObject.mapFrom(actual).encodePrettily();
    
    for(final var entry : replacements.entrySet()) {
      actualJson = actualJson.replace(entry.getKey(), entry.getValue());
    }
    final var expected = toExpectedFile(expectedFileName);
    Assertions.assertEquals(expected, actualJson);
    
  }
  public void assertEquals(String expectedFileName, Export actual) {
    final var expected = toExpectedFile(expectedFileName);
    final var actualJson = JsonObject.mapFrom(actual).encodePrettily();
    Assertions.assertEquals(expected, actualJson);
  }
}
