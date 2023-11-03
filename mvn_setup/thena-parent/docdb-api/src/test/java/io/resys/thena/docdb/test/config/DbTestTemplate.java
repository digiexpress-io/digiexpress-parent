package io.resys.thena.docdb.test.config;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

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

import io.vertx.mutiny.sqlclient.Pool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.DocDBPrettyPrinter;
import io.resys.thena.docdb.spi.jackson.VertexExtModule;
import io.resys.thena.docdb.sql.DocDBFactorySql;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.json.jackson.VertxModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInfo;


@Slf4j
public class DbTestTemplate {
  private DocDB client;
  @Inject
  io.vertx.mutiny.pgclient.PgPool pgPool;
  
  private static AtomicInteger index = new AtomicInteger(1);
  private String db;
  private BiConsumer<DocDB, Repo> callback;
  private Repo repo;
  
  public DbTestTemplate() {
  }
  public DbTestTemplate(BiConsumer<DocDB, Repo> callback) {
    this.callback = callback;
  }  
  
  @BeforeEach
  public void setUp(TestInfo testInfo) throws InterruptedException {
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

    this.client = DocDBFactorySql.create()
        .db("junit")
        .client(pgPool)
        .errorHandler(new PgTestErrors())
        .build();

    repo = this.client.project().projectBuilder()
      .name("junit" + index.incrementAndGet())
      .build()
      .await().atMost(Duration.ofSeconds(10)).getRepo();
    if(callback != null) {
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

  public DocDB getClient() {
    return client;
  }
  
  public ClientState createState() {
    final var ctx = ClientCollections.defaults(db);
    return DocDBFactorySql.state(ctx, pgPool, new PgTestErrors());
  }
  
  public void printRepo(Repo repo) {
    final String result = new DocDBPrettyPrinter(createState()).print(repo);
    log.debug(result);
  }
  public Repo getRepo() {
    return repo;
  }

}
