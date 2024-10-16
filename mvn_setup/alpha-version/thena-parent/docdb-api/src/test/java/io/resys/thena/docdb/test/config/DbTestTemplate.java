package io.resys.thena.docdb.test.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.jackson.VertexExtModule;
import io.resys.thena.spi.DbState;
import io.resys.thena.storesql.DbStateSqlImpl;
import io.resys.thena.structures.git.GitPrinter;
import io.resys.thena.structures.grim.GrimPrinter;
import io.resys.thena.support.DocDbPrinter;
import io.resys.thena.support.OrgDbPrinter;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.json.jackson.VertxModule;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;

/*-
 * #%L
 * thena-docdb-pgsql
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class DbTestTemplate {
	private boolean STORE_TO_DEBUG_DB = false;
  private ThenaClient client;
  @Inject io.vertx.mutiny.pgclient.PgPool pgPool;
  @Inject io.vertx.mutiny.core.Vertx vertx;
  
  private static AtomicInteger index = new AtomicInteger(1);
  private BiConsumer<ThenaClient, Tenant> callback;
  private String db;
  private Tenant repo;
  private final Map<String, String> replacements = new HashMap<>();

  
  public DbTestTemplate() {
  }
  public DbTestTemplate(BiConsumer<ThenaClient, Tenant> callback) {
    this.callback = callback;
  }  
  
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
  public void setUp(TestInfo testInfo) throws InterruptedException {
    replacements.clear();
  	connectToDebugDb();
    waitUntilPostgresqlAcceptsConnections(pgPool);

    final var modules = new com.fasterxml.jackson.databind.Module[] {
      new JavaTimeModule(), 
      new Jdk8Module(), 
      new GuavaModule(),
      new VertxModule(),
      new VertexExtModule()
    };
    DatabindCodec.mapper().registerModules(modules);
    DatabindCodec.prettyMapper().registerModules(modules);

    this.client = DbStateSqlImpl.create().db("junit").client(pgPool).build();
    if(callback != null) {
      repo = this.client.tenants().commit()
          .name("junit" + index.incrementAndGet(), StructureType.git)
          .build()
          .await().atMost(Duration.ofSeconds(10)).getRepo();
      callback.accept(client, repo);
    }
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

  @AfterEach
  public void tearDown() {
  }

  public ThenaClient getClient() {
    return client;
  }
  
  public DbState createState() {
    final var ctx = TenantTableNames.defaults(db);
    return DbStateSqlImpl.create(ctx, pgPool);
  }
  
  public void printRepo(Tenant repo) {
    if(repo.getType() == StructureType.doc) {
      final String result = new DocDbPrinter(createState()).print(repo);
      log.debug(result);
    } else if(repo.getType() == StructureType.org) {
      final String result = new OrgDbPrinter(createState()).print(repo);
      log.debug(result);
      
    } else if(repo.getType() == StructureType.git) {
      final String result = new GitPrinter(createState()).print(repo);
      log.debug(result);
      
    } else if(repo.getType() == StructureType.grim) {
      final String result = new GrimPrinter(createState()).print(repo);
      log.debug(result);
    }
  }
  public Tenant getRepo() {
    return repo;
  }

  
  public static String toExpectedFile(String fileName) {
    return toString(DbTestTemplate.class, fileName);
  }
  
  public void assertRepo(Tenant client, String expectedFileName) {
    final var expected = toExpectedFile(expectedFileName);
    final var actual = toStaticData(client);
    Assertions.assertLinesMatch(expected.lines(), actual.lines(), actual);
    
  }
  public void assertEquals(String expectedFileName, Object actual) {
    final var expected = toExpectedFile(expectedFileName);
    final var actualJson = JsonObject.mapFrom(actual).encodePrettily();
    Assertions.assertLinesMatch(expected.lines(), actualJson.lines(), actualJson);  
  }
  
  public static String toString(Class<?> type, String resource) {
    try {
      return new String(type.getClassLoader().getResourceAsStream(resource).readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  public String toStaticData(Tenant client) {    
    if(client.getType() == StructureType.doc) {
      return new DocDbPrinter(createState()).printWithStaticIds(client);
    } else if(client.getType() == StructureType.org) {
      return new OrgDbPrinter(createState()).printWithStaticIds(client, replacements);
    } else if(client.getType() == StructureType.grim) {
      return new GrimPrinter(createState()).printWithStaticIds(client, replacements);
    }
    return new GitPrinter(createState()).printWithStaticIds(client);
  }
  
}
