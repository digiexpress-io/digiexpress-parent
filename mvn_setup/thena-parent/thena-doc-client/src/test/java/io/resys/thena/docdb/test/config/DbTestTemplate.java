package io.resys.thena.docdb.test.config;

/*-
 * #%L
 * thena-doc-client
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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.TenantCacheImpl;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.datasource.ThenaSqlDataSourceImpl;
import io.resys.thena.datasource.vertx.ThenaSqlPoolVertx;
import io.resys.thena.doc.api.DocClient;
import io.resys.thena.doc.api.DocDataSource;
import io.resys.thena.doc.spi.DocClientImpl;
import io.resys.thena.doc.spi.DocDataSourceImpl;
import io.resys.thena.doc.spi.support.DocDbPrinter;
import io.resys.thena.storesql.PgErrors;
import io.resys.thena.test.ThenaTest;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ThenaTest
public class DbTestTemplate {
  private DocClient client;
  io.vertx.mutiny.sqlclient.Pool pgPool;
  protected static Duration atMost = Duration.ofMinutes(1);
  
  private static AtomicInteger index = new AtomicInteger(1);
  private BiConsumer<DocClient, Tenant> callback;
  private Tenant repo;
  private final Map<String, String> replacements = new HashMap<>();

  
  public DbTestTemplate() {
  }
  public DbTestTemplate(BiConsumer<DocClient, Tenant> callback) {
    this.callback = callback;
  }  
  

  
  @BeforeEach
  public void setUp(io.vertx.mutiny.sqlclient.Pool pgPool) throws InterruptedException {
    this.pgPool = pgPool;
    this.replacements.clear();

    final var repoName = "junit";
    final var tenantCache = new TenantCacheImpl();
    final var errorHandler = new PgErrors();
    final var ctx = TenantContext.defaults(repoName);
    final var pool = new ThenaSqlPoolVertx(this.pgPool);
    final var dataSource = new ThenaSqlDataSourceImpl(
        repoName, ctx, pool, errorHandler, 
        Optional.empty(),
        tenantCache
    );
    
    this.client = new DocClientImpl(new DocDataSourceImpl(dataSource)); 
    if(callback != null) {
      repo = this.client.tenants().createOneTenant()
          .name("junit" + index.incrementAndGet(), StructureType.doc)
          .build()
          .await().atMost(Duration.ofSeconds(10)).getRepo();
      callback.accept(client, repo);
    }
  }


  @AfterEach
  public void tearDown() {
  }

  public DocClient getClient() {
    return client;
  }
  
  public DocDataSource createState() {
    final var ctx = TenantContext.defaults("");
    
    final var tenantCache = new TenantCacheImpl();
    final var errorHandler = new PgErrors();
    
    final var pool = new ThenaSqlPoolVertx(this.pgPool);
    final var dataSource = new ThenaSqlDataSourceImpl(
        "", ctx, pool, errorHandler, 
        Optional.empty(),
        tenantCache
    );
    
    return new DocDataSourceImpl(dataSource); 
  }
  
  public void printRepo(Tenant repo) {
    final String result = new DocDbPrinter(createState()).print(repo);
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
    return new DocDbPrinter(createState()).printWithStaticIds(client);
  }
  
}
