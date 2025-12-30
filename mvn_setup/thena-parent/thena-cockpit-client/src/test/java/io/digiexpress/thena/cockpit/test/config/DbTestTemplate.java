package io.digiexpress.thena.cockpit.test.config;

/*-
 * #%L
 * thena-contract-client
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

import io.digiexpress.thena.cockpit.client.api.CockpitClient;
import io.digiexpress.thena.cockpit.client.spi.CockpitClientImpl;
import io.digiexpress.thena.cockpit.client.spi.CockpitPrinter;
import io.digiexpress.thena.cockpit.client.tables.CockpitDb;
import io.resys.thena.api.actions.TenantActions.TenantOperationStatus;
import io.resys.thena.api.actions.TenantActions.CreatedTenant;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.TenantCacheImpl;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.storesql.PgErrors;
import io.resys.thena.test.ThenaTest;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ThenaTest
public class DbTestTemplate {

  private CockpitClient client;
  protected io.vertx.mutiny.sqlclient.Pool pgPool;

  protected static Duration atMost = Duration.ofMinutes(1);
  
  private static AtomicInteger index = new AtomicInteger(1);
  private BiConsumer<CockpitClient, Tenant> callback;
  private String db;
  private Tenant repo;
  private final Map<String, String> replacements = new HashMap<>();

  
  public DbTestTemplate() {
  }
  public DbTestTemplate(BiConsumer<CockpitClient, Tenant> callback) {
    this.callback = callback;
  }  

  
  @BeforeEach
  public void setUp(io.vertx.mutiny.sqlclient.Pool pgPool) throws InterruptedException {
    this.pgPool = pgPool;
    this.replacements.clear();
    this.client = CockpitClientImpl.create().tenantName("junit").client(pgPool)
        .errorHandler(new PgErrors())
        .build();
    if(callback != null) {
      repo = this.client.tenants().createOneTenant()
          .name("junit" + index.incrementAndGet(), StructureType.cockpit)
          .build()
          .await().atMost(Duration.ofSeconds(10)).getRepo();
      callback.accept(client, repo);
    }
  }


  @AfterEach
  public void tearDown() {
  }

  public CockpitClient getClient() {
    return client;
  }
  
  public CockpitDb createState() {
    final var ctx = TenantContext.defaults(db);
    return CockpitClientImpl.create(ctx, pgPool, new TenantCacheImpl(), new PgErrors());
  }
  
  public void printRepo(Tenant repo) {
    final String result = new CockpitPrinter(createState()).print(repo);
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
    return new CockpitPrinter(createState()).printWithStaticIds(client, replacements);
  }
  
  
  public CockpitClient createClient(String tenantId) {
    // create project
    CreatedTenant repo = getClient().tenants().createOneTenant()
        .name(tenantId)
        .build()
        .await().atMost(atMost);
    log.debug("created repo {}", repo);
    Assertions.assertEquals(TenantOperationStatus.OK, repo.getStatus());
    
    final var tenant = repo.getRepo();
    
    return CockpitClientImpl.create()
        .client(pgPool)
        .tenantName(tenant.getName())
        .errorHandler(new PgErrors())
        .build();
    
  }
}
