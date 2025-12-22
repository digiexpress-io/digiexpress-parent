package io.resys.thena.docdb.test.config;

/*-
 * #%L
 * thena-db-client
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

import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.TenantCacheImpl;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.spi.DbState;
import io.resys.thena.storesql.DbStateSqlImpl;
import io.resys.thena.structures.fs.FsPrinter;
import io.resys.thena.support.OrgDbPrinter;
import io.resys.thena.test.ThenaTest;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ThenaTest
public class DbTestTemplate {
  private ThenaClient client;
  io.vertx.mutiny.sqlclient.Pool pgPool;

  protected static Duration atMost = Duration.ofMinutes(1);
  
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
  

  @BeforeEach
  public void setUp(io.vertx.mutiny.sqlclient.Pool pgPool) throws InterruptedException {
    this.replacements.clear();
    this.pgPool = pgPool;

    this.client = DbStateSqlImpl.create().db("junit").client(pgPool).build();
    if(callback != null) {
      repo = this.client.tenants().createOneTenant()
          .name("junit" + index.incrementAndGet(), StructureType.git)
          .build()
          .await().atMost(Duration.ofSeconds(10)).getRepo();
      callback.accept(client, repo);
    }
  }

  @AfterEach
  public void tearDown() {
  }

  public ThenaClient getClient() {
    return client;
  }
  
  public DbState createState() {
    final var ctx = TenantContext.defaults(db);
    return DbStateSqlImpl.create(ctx, pgPool, new TenantCacheImpl());
  }
  
  public void printRepo(Tenant repo) {
    if(repo.getType() == StructureType.doc) {

    } else if(repo.getType() == StructureType.org) {
      final String result = new OrgDbPrinter(createState()).print(repo);
      log.debug(result);
      
    } else if(repo.getType() == StructureType.git) {
      
    } else if(repo.getType() == StructureType.grim) {

    } else if(repo.getType() == StructureType.fs) {
      final String result = new FsPrinter(createState()).print(repo);
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

    } else if(client.getType() == StructureType.org) {
      return new OrgDbPrinter(createState()).printWithStaticIds(client, replacements);
    } else if(client.getType() == StructureType.grim) {

    } else if(client.getType() == StructureType.fs) {
      return new FsPrinter(createState()).printWithStaticIds(client, replacements);
    }

    return "";
  }
  
}
