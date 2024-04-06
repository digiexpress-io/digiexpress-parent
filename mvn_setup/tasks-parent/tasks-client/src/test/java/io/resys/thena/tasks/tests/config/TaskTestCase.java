package io.resys.thena.tasks.tests.config;

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
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.resys.thena.jackson.VertexExtModule;
import io.resys.thena.spi.ThenaClientPgSql;
import io.resys.thena.structures.git.GitPrinter;
import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.client.api.model.Export;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.thenagit.DocumentStoreImpl;
import io.resys.thena.tasks.client.thenagit.TaskClientImpl;
import io.resys.thena.tasks.client.thenamission.TaskStoreImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.json.jackson.VertxModule;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskTestCase {
  private boolean STORE_TO_DEBUG_DB = false;
  @Inject io.vertx.mutiny.pgclient.PgPool pgPool;
  @Inject io.vertx.mutiny.core.Vertx vertx;
  public final Duration atMost = Duration.ofMinutes(5);
  
  private DocumentStoreImpl store;
  private TaskClient client;
  private static final String DB = "junit-tasks-"; 
  private static final AtomicInteger DB_ID = new AtomicInteger();
  private static final Instant targetDate = LocalDateTime.of(2023, 1, 1, 1, 1).toInstant(ZoneOffset.UTC);
  private final AtomicInteger id_provider = new AtomicInteger();
  

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
  
  
  @BeforeEach
  public void setUp() {
    connectToDebugDb();
    waitUntilPostgresqlAcceptsConnections(pgPool);
    final var db = DB + DB_ID.getAndIncrement();
    /*store = DocumentStoreImpl.builder()
        .repoName(db).pgPool(pgPool).pgDb(db)
        .gidProvider(new DocumentGidProvider() {
          @Override
          public String getNextVersion(DocumentType entity) {
            return id_provider.incrementAndGet() + "_" + entity.name();
          }
          
          @Override
          public String getNextId(DocumentType entity) {
            return id_provider.incrementAndGet() + "_" + entity.name();
          }
        })
        .build();
        */
    //client = new io.resys.thena.tasks.client.thenagit.TaskClientImpl(store);
    
    client = new io.resys.thena.tasks.client.thenamission.TaskClientImpl(TaskStoreImpl.builder()
        .repoName(db).pgPool(pgPool).pgDb(db)
        .build());
    
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

  public DocumentStoreImpl getStore() {
    return store;
  }

  public TaskClient getClient() {
    return client;
  }

  public static Instant getTargetDate() {
    return targetDate;
  }

  public String printRepo(TaskClient client) {
    final var config = ((TaskClientImpl) client).getCtx().getConfig();
    final var state = ((ThenaClientPgSql) config.getClient()).getState();
    final var repo = client.repo().getRepo().await().atMost(Duration.ofMinutes(1));
    final String result = new GitPrinter(state).printWithStaticIds(repo);
    return result;
  }
  
  public String toStaticData(TaskClient client) {
    final var config = ((TaskClientImpl) client).getCtx().getConfig();
    final var state = ((ThenaClientPgSql) config.getClient()).getState();
    final var repo = client.repo().getRepo().await().atMost(Duration.ofMinutes(1));
    return new RepositoryToStaticData(state).print(repo);
  }
  
  public static String toExpectedFile(String fileName) {
    return RepositoryToStaticData.toString(TaskTestCase.class, fileName);
  }
  
  public void assertRepo(TaskClient client, String expectedFileName) {
    final var expected = toExpectedFile(expectedFileName);
    final var actual = toStaticData(client);
    Assertions.assertLinesMatch(expected.lines(), actual.lines());
    
  }
  public void assertEquals(String expectedFileName, Task actual) {
    final var task_index = new AtomicInteger(0);
    final Map<String, String> replacements = new HashMap<>();
    final Function<Object, String> add = (input) -> {
      if(input == null) {
        return null;
      }
      
      if(input instanceof Instant) {
        final var value = JsonObject.of("targetDate", input).getString("targetDate");
        replacements.put(value, JsonObject.of("targetDate", targetDate).getString("targetDate"));
        return value;
      }
      final var value = task_index.incrementAndGet() + "_TASK";
      replacements.put(input+ "", value);
      return value;
    };
    
    
    add.apply(actual.getId());
    add.apply(actual.getVersion());
    add.apply(actual.getCreated());
    add.apply(actual.getUpdated());
    actual.getChecklist().forEach(e -> {
      add.apply(e.getId());  
      e.getItems().forEach(i -> add.apply(i.getId()));
    });

    actual.getTransactions().forEach(e -> {
      add.apply(e.getId());
    });
    
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
