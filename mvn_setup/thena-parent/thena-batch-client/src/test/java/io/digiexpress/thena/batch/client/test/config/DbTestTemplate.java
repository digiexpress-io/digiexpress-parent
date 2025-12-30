package io.digiexpress.thena.batch.client.test.config;

/*-
 * #%L
 * thena-batch-client
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
import org.junit.jupiter.api.TestInfo;

import io.digiexpress.thena.batch.client.api.BatchClient;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.digiexpress.thena.batch.client.api.persistence.BatchPrinter;
import io.digiexpress.thena.batch.client.spi.BatchClientImpl;
import io.digiexpress.thena.batch.client.spi.persistence.sql.BatchDbImpl;
import io.resys.thena.api.actions.TenantActions.TenantOperationStatus;
import io.resys.thena.api.actions.TenantActions.CreatedTenant;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.TenantCacheImpl;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.test.ThenaTest;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ThenaTest
public class DbTestTemplate {

  private BatchClient client;
  private io.vertx.mutiny.sqlclient.Pool pgPool;
  
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
  
  
  protected BatchClient getOrCreateTenant(String defaultName) {
    return getOrCreateTenant(getClient(), defaultName); 
  }
  
  
  protected static BatchClient getOrCreateTenant(BatchClient client, String defaultName) {
    final CreatedTenant repo = client.manageTenants().createOneTenant()
        .name(defaultName)
        .build()
        .await().atMost(atMost);
    Assertions.assertEquals(TenantOperationStatus.OK, repo.getStatus());
    return client.withTenant(repo.getRepo().getId()); 
  }
  
  @BeforeEach
  public void setUp(io.vertx.mutiny.sqlclient.Pool pgPool) throws InterruptedException {
    this.pgPool = pgPool;
    replacements.clear();

    this.client = new BatchClientImpl(BatchDbImpl.create().tenant("junit").client(pgPool).build());
    if(callback != null && !init_performed) {
      init_performed = true;
      repo = this.client.manageTenants().createOneTenant()
          .name("junit" + index.incrementAndGet(), StructureType.batch)
          .build()
          .await().atMost(Duration.ofSeconds(10)).getRepo();
      callback.accept(client, repo);
    }
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
