package io.digiexpress.thena.mq.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import io.digiexpress.thena.mq.client.api.ThenaMqClient;
import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqTableNames;
import io.digiexpress.thena.mq.client.spi.persistence.ThenaMqChannelStateImpl;
import io.digiexpress.thena.mq.client.spi.visitors.ChannelPrinterVisitor;
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
  @Inject io.vertx.mutiny.pgclient.PgPool pgPool;
  @Inject io.vertx.mutiny.core.Vertx vertx;

  private final Map<String, String> replacements = new HashMap<>();
  
  private boolean STORE_TO_DEBUG_DB = true;
  private ThenaMqClient client;
  private String db;
  private Channel channel;

  
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
    this.client = ThenaMqChannelStateImpl.create().db("junit").client(pgPool).build();

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

  public ThenaMqClient getClient() {
    return client;
  }
  
  public ThenaMqChannelState createState() {
    final var ctx = ThenaMqTableNames.defaults(db);
    return ThenaMqChannelStateImpl.create(ctx, pgPool);
  }
  
  public void printRepo(Channel repo) {
    final String result = new ChannelPrinterVisitor(createState()).print(repo);
    log.debug(result);
  }
  public Channel getRepo() {
    return channel;
  }

  
  public static String toExpectedFile(String fileName) {
    return toString(DbTestTemplate.class, fileName);
  }
  
  public void assertRepo(Channel client, String expectedFileName) {
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
  
  public String toStaticData(Channel client) {    
    return new ChannelPrinterVisitor(createState()).printWithStaticIds(client, replacements);
  }
  
}
