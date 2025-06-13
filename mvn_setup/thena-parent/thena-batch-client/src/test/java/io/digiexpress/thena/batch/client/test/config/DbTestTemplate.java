package io.digiexpress.thena.batch.client.test.config;

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

import io.digiexpress.thena.batch.client.api.BatchClient;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.digiexpress.thena.batch.client.api.persistence.BatchPrinter;
import io.digiexpress.thena.batch.client.spi.BatchClientImpl;
import io.digiexpress.thena.batch.client.spi.persistence.sql.BatchDbImpl;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.TenantCacheImpl;
import io.resys.thena.datasource.TenantContext;
import io.vertx.core.json.JsonObject;
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
  private BatchClient client;
  @Inject io.vertx.mutiny.pgclient.PgPool pgPool;
  @Inject io.vertx.mutiny.core.Vertx vertx;
  protected static final Duration atMost = Duration.ofMinutes(2);
  
  private static AtomicInteger index = new AtomicInteger(1);
  private BiConsumer<BatchClient, Tenant> callback;
  private String db;
  private Tenant repo;
  private static volatile boolean init_performed;
  private final Map<String, String> replacements = new HashMap<>();

  
  public DbTestTemplate() {
  }
  public DbTestTemplate(BiConsumer<BatchClient, Tenant> callback) {
    this.callback = callback;
  }  
  
  private void connectToDebugDb() {
  	if(!STORE_TO_DEBUG_DB) {
  		return;
  	}
  	
  	final var connectOptions = new PgConnectOptions()
  			.setDatabase("eveli-app")
        .setHost("localhost")
        .setPort(5433)
        .setUser("eveli-app")
        .setPassword("password123");
    final var poolOptions = new PoolOptions().setMaxSize(6);
    this.pgPool = io.vertx.mutiny.pgclient.PgPool.pool(vertx, connectOptions, poolOptions);
  }
  
  @BeforeEach
  public void setUp(TestInfo testInfo) throws InterruptedException {
    replacements.clear();
  	connectToDebugDb();
    waitUntilPostgresqlAcceptsConnections(pgPool);

    this.client = new BatchClientImpl(BatchDbImpl.create().tenant("junit").client(pgPool).build());
    if(callback != null && !init_performed) {
      init_performed = true;
      repo = this.client.manageTenants().commit()
          .name("junit" + index.incrementAndGet(), StructureType.batch)
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

  public BatchClient getClient() {
    return client;
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
  
  
  public BatchDb createState() {
    final var ctx = TenantContext.defaults(db);
    return BatchDbImpl.create(ctx, pgPool, new TenantCacheImpl());
  }
  public void printRepo(Tenant repo) {
    final String result = new BatchPrinter(createState()).print(repo);
    log.debug(result); 
  }
  public String toStaticData(Tenant client) {    
    return new BatchPrinter(createState()).print(client);
  }
}
