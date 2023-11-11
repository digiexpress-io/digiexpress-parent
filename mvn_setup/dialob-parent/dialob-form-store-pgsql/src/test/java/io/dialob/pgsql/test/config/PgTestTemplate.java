package io.dialob.pgsql.test.config;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.dialob.client.api.DialobStore.StoreRepoBuilder;
import io.dialob.client.pgsql.PgSqlDialobStore;
import io.dialob.client.spi.DialobStoreTemplate;
import io.dialob.client.spi.support.RepositoryToStaticData;
import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.models.git.GitPrinter;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.store.sql.DbStateSqlImpl;
import io.resys.thena.docdb.store.sql.PgErrors;
import io.vertx.core.VertxOptions;
import io.vertx.mutiny.sqlclient.Pool;

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

public class PgTestTemplate {
  private DialobStoreTemplate store;
  public static VertxOptions options = new VertxOptions();
  {
    options.setBlockedThreadCheckInterval(1000*60*60);
  }

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
    final AtomicInteger gid = new AtomicInteger(0);
    waitUntilPostgresqlAcceptsConnections(pgPool);
    this.store = PgSqlDialobStore.builder()
        .repoName("")
        .pgPool(pgPool)
        .objectMapper(objectMapper)
        .gidProvider((type) -> {

          return type + "-" + gid.incrementAndGet();
        })
        .build();
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

  private DbState createState(String repoName) {
    final var ctx = DbCollections.defaults(repoName);
    return DbStateSqlImpl.state(ctx, pgPool, new PgErrors());
  }

  public void printRepo(Repo repo) {
    final String result = new GitPrinter(createState(repo.getName())).print(repo);
    System.out.println(result);
  }

  public void prettyPrint(String repoId) {
    Repo repo = getThena().repo().projectsQuery().id(repoId).get()
        .await().atMost(Duration.ofMinutes(1));
    printRepo(repo);
  }

  public String toRepoExport(String repoName) {
    Repo repo = getThena().repo().projectsQuery().id(repoName).get()
        .await().atMost(Duration.ofMinutes(1));
    final String result = new RepositoryToStaticData(createState(repo.getName())).print(repo);
    return result;
  }

  public DocDB getThena() {
    return store.getConfig().getClient();
  }

  public StoreRepoBuilder repo() {
    return store.repo();
  }

}
