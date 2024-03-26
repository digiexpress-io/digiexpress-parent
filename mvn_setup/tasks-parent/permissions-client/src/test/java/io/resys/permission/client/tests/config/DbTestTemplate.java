package io.resys.permission.client.tests.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.spi.PermissionClientImpl;
import io.resys.permission.client.spi.PermissionStore;
import io.resys.permission.client.spi.PermissionStoreImpl;
import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.jackson.VertexExtModule;
import io.resys.thena.spi.DbCollections;
import io.resys.thena.spi.DbState;
import io.resys.thena.storesql.DbStateSqlImpl;
import io.resys.thena.storesql.PgErrors;
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
  private ThenaClient docDb;
  @Inject io.vertx.mutiny.pgclient.PgPool pgPool;
  @Inject io.vertx.mutiny.core.Vertx vertx;
  
  private String db;
  private static final String DB = "junit-perm-"; 
  private static final AtomicInteger DB_ID = new AtomicInteger();
  private Tenant repo;
  private PermissionClient client;
  private PermissionStore store;


  private void connectToDebugDb() {
  	if(!STORE_TO_DEBUG_DB) {
  		return;
  	}
  	
  	final var connectOptions = new PgConnectOptions()
  			.setDatabase("debug_org_db")
        .setHost("localhost")
        .setPort(5432)
        .setUser("postgres")
        .setPassword("postgres");
    final var poolOptions = new PoolOptions().setMaxSize(6);
    this.pgPool = io.vertx.mutiny.pgclient.PgPool.pool(vertx, connectOptions, poolOptions);
  }
  
  @BeforeEach
  public void setUp(TestInfo testInfo) throws InterruptedException {
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

    this.db = DB + DB_ID.getAndIncrement();    
    this.docDb = DbStateSqlImpl.create()
      .db(db)
      .client(pgPool)
      .errorHandler(new PgErrors())
      .build();

    store = PermissionStoreImpl.builder()
      .repoName("")
      .pgPool(pgPool).pgDb(db)
      .build();
    client = new PermissionClientImpl(store);
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

  public ThenaClient getDocDb() {
    return docDb;
  }
  public PermissionClient getClient() {
    return client;
  }
  
  public DbState createState() {
    final var ctx = DbCollections.defaults(db);
    return DbStateSqlImpl.state(ctx, pgPool, new PgErrors());
  }
  
  public void printRepo(Tenant repo) {
    final String result = new OrgDbPrinter(createState()).print(repo);
    log.debug(result);

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
    Assertions.assertLinesMatch(expected.lines(), actualJson.lines());  
  }
  
  public static String toString(Class<?> type, String resource) {
    try {
      return new String(type.getClassLoader().getResourceAsStream(resource).readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  public String toStaticData(Tenant client) {    
    return new OrgDbPrinter(createState()).printWithStaticIds(client);
  }
  
}
