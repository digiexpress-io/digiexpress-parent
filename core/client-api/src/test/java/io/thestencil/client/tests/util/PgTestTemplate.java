package io.thestencil.client.tests.util;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

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

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.api.actions.RepoActions.RepoResult;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.DocDBPrettyPrinter;
import io.resys.thena.docdb.spi.pgsql.PgErrors;
import io.resys.thena.docdb.sql.DocDBFactorySql;
import io.thestencil.client.api.StencilComposer;
import io.thestencil.client.spi.StencilClientImpl;
import io.thestencil.client.spi.StencilComposerImpl;
import io.thestencil.client.spi.StencilStoreImpl;
import io.thestencil.client.spi.serializers.ZoeDeserializer;

public class PgTestTemplate {
  private DocDB client;
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
    this.client = DocDBFactorySql.create()
        .db("junit")
        .client(pgPool)
        .errorHandler(new PgErrors())
        .build();
    this.client.repo().create().name("junit").build();
  }
  
  @AfterEach
  public void tearDown() {
  }

  public DocDB getClient() {
    return client;
  }
  
  public ClientState createState() {
    final var ctx = ClientCollections.defaults("junit");
    return DocDBFactorySql.state(ctx, pgPool, new PgErrors());
  }
  
  public void printRepo(Repo repo) {
    final String result = new DocDBPrettyPrinter(createState()).print(repo);
    System.out.println(result);
  }
  
  public void prettyPrint(String repoId) {
    Repo repo = getClient().repo().query().id(repoId).get()
        .await().atMost(Duration.ofMinutes(1));
    
    printRepo(repo);
  }

  public String toRepoExport(String repoId) {
    Repo repo = getClient().repo().query().id(repoId).get()
        .await().atMost(Duration.ofMinutes(1));
    final String result = new TestExporter(createState()).print(repo);
    return result;
  }

  
  public StencilComposer getPersistence(String repoId) {
    final DocDB client = getClient();
    
    // create project
    RepoResult repo = getClient().repo().create()
        .name(repoId)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    
    final AtomicInteger gid = new AtomicInteger(0);
    
    ZoeDeserializer deserializer = new ZoeDeserializer(PgTestTemplate.objectMapper);
    
    final var store = StencilStoreImpl.builder()
        .config((builder) -> builder
            .client(client)
            .repoName(repoId)
            .headName("stencil-main")
            .deserializer(deserializer)
            .objectMapper(PgTestTemplate.objectMapper)
            .serializer((entity) -> {
              try {
                return PgTestTemplate.objectMapper.writeValueAsString(entity);
              } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
              }
            })
            .gidProvider(type -> {
               return type + "-" + gid.incrementAndGet();
            })
            .authorProvider(() -> "junit-test"))
            
        .build();
    
    
    return new StencilComposerImpl(new StencilClientImpl(store));
  }
  
}
