package io.digiexpress.eveli.assets.tests.util;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

/*-
 * #%L
 * eveli-assets
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.dialob.api.form.Form;
import io.digiexpress.eveli.assets.spi.EveliAssetsClientImpl;
import io.digiexpress.eveli.assets.spi.EveliAssetsComposerImpl;
import io.digiexpress.eveli.assets.spi.EveliAssetsDeserializer;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.spi.DbState;
import io.resys.thena.storesql.DbStateSqlImpl;
import io.resys.thena.structures.git.GitPrinter;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Pool;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class PgTestTemplate {
  private ThenaClient client;
  
  @Inject
  io.vertx.mutiny.pgclient.PgPool pgPool;
  
  public static ObjectMapper objectMapper = new ObjectMapper();
  static {
    objectMapper.registerModule(new GuavaModule());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.registerModule(new Jdk8Module());
  }
  
  @BeforeEach
  public void setUp() {
    waitUntilPostgresqlAcceptsConnections(pgPool);
    this.client = DbStateSqlImpl.create()
        .db("junit")
        .client(pgPool)
        .build();
    this.client.tenants().commit().name("junit", StructureType.git).build();
  }
  
  @AfterEach
  public void tearDown() {
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

  public ThenaClient getClient() {
    return client;
  }
  
  public DbState createState() {
    final var ctx = TenantTableNames.defaults("junit");
    return DbStateSqlImpl.create(ctx, pgPool);
  }
  
  public void printRepo(Tenant repo) {
    final String result = new GitPrinter(createState()).print(repo);
    log.debug(result);
  }
  
  public void prettyPrint(String repoId) {
    Tenant repo = getClient().git(repoId).tenants().get()
        .await().atMost(Duration.ofMinutes(1)).getRepo();
    
    printRepo(repo);
  }

  public String toRepoExport(String repoId) {
    Tenant repo = getClient().git(repoId).tenants().get()
        .await().atMost(Duration.ofMinutes(1)).getRepo();
    final String result = new TestExporter(createState()).print(repo);
    return result;
  }

  
  public EveliAssetsComposerImpl getPersistence(String repoId) {
    final ThenaClient client = getClient();
    
    // create project
    TenantCommitResult repo = getClient().tenants().commit()
        .name(repoId, StructureType.git)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.info("Repo created: " + repo);
    
    final AtomicInteger gid = new AtomicInteger(0);
    
    EveliAssetsDeserializer deserializer = new EveliAssetsDeserializer(PgTestTemplate.objectMapper);
    
    final var store = EveliAssetsClientImpl.builder()
        .config((builder) -> builder
            .client(client)
            .repoName(repoId)
            .headName("assets-main")
            .deserializer(deserializer)
            .objectMapper(PgTestTemplate.objectMapper)
            .serializer((entity) -> {
              try {
                return new JsonObject(PgTestTemplate.objectMapper.writeValueAsString(entity));
              } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
              }
            })
            .gidProvider(() -> gid.incrementAndGet() + "")
            .authorProvider(() -> "junit-test"))
        .build();
    

    final var dialobClient = Mockito.mock(DialobClient.class);
    final var form = Mockito.mock(Form.class);
    
    Mockito.when(form.getId()).thenReturn("mock-form");
    Mockito.when(dialobClient.getFormByNameAndTag(Mockito.anyString(), Mockito.anyString())).thenReturn(form);    
    
    return new EveliAssetsComposerImpl(store, null, null, dialobClient);
  }
  
}
