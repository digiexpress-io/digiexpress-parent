package io.resys.thena.grim.test.config;

/*-
 * #%L
 * thena-grim-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.TenantCacheImpl;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.grim.api.GrimClient;
import io.resys.thena.grim.spi.GrimClientImpl;
import io.resys.thena.grim.spi.GrimDataSource;
import io.resys.thena.grim.spi.GrimPrinter;
import io.resys.thena.test.ThenaTest;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Pool;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ThenaTest
public class DbTestTemplate {
  private GrimClient client;
  private io.vertx.mutiny.sqlclient.Pool pgPool;
  protected static Duration atMost = Duration.ofMinutes(1);
  
  private static AtomicInteger index = new AtomicInteger(1);
  private BiConsumer<GrimClient, Tenant> callback;
  private String db;
  private Tenant repo;
  private final Map<String, String> replacements = new HashMap<>();

  
  public DbTestTemplate() {
  }
  public DbTestTemplate(BiConsumer<GrimClient, Tenant> callback) {
    this.callback = callback;
  }  
  

  
  @BeforeEach
  public void setUp(io.vertx.mutiny.sqlclient.Pool pgPool) throws InterruptedException {
    this.pgPool = pgPool;
    this.replacements.clear();
    waitUntilPostgresqlAcceptsConnections(pgPool);

    this.client = GrimClientImpl.create().db("junit").client(pgPool).build();
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

  public GrimClient getClient() {
    return client;
  }
  
  public GrimDataSource createState() {
    final var ctx = TenantContext.defaults(db);
    return GrimClientImpl.create(ctx, pgPool, new TenantCacheImpl());
  }
  
  public void printRepo(Tenant repo) {
    final String result = new GrimPrinter(createState()).print(repo);
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
    return new GrimPrinter(createState()).printWithStaticIds(client, replacements);
  }
  
}
