package io.digiexpress.eveli.userprofile.client.tests.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/*-
 * #%L
 * thena-tasks-client
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.userprofile.client.api.UserProfileClient;
import io.digiexpress.eveli.userprofile.client.spi.UserProfileClientImpl;
import io.digiexpress.eveli.userprofile.client.spi.UserProfileStore;
import io.resys.thena.doc.spi.support.DocDbPrinter;
import io.resys.thena.test.ThenaTest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ThenaTest
public class UserProfileTestCase {
  io.vertx.mutiny.sqlclient.Pool pgPool;
  public final Duration atMost = Duration.ofMinutes(5);
  
  private UserProfileStore store;
  private UserProfileClientImpl client;
  private static final String DB = "junit-crm-"; 
  private static final AtomicInteger DB_ID = new AtomicInteger();
  private static final Instant targetDate = LocalDateTime.of(2023, 1, 1, 1, 1).toInstant(ZoneOffset.UTC);
  
  @BeforeEach
  public void setUp(io.vertx.mutiny.sqlclient.Pool pgPool) {
    this.pgPool = pgPool;
    final var db = DB + DB_ID.getAndIncrement();
    store = UserProfileStore.builder()
        .repoName(db).pgPool(pgPool).pgDb(db)
        .build();
    client = new UserProfileClientImpl(store);
    objectMapper();
    
  }

  public static ObjectMapper objectMapper() {
    return DatabindCodec.mapper(); 
  }
  
  @AfterEach
  public void tearDown() {
    store = null;
  }

  public UserProfileStore getStore() {
    return store;
  }

  public UserProfileClientImpl getClient() {
    return client;
  }

  public static Instant getTargetDate() {
    return targetDate;
  }


  public String toStaticData(UserProfileClient client) {
    final var config = ((UserProfileClientImpl) client).getCtx().getConfig();
    final var state = config.getClient().unwrap();
    final var repo = client.getRepo().await().atMost(Duration.ofMinutes(1));
    return new DocDbPrinter(state).printWithStaticIds(repo);
  }
  
  public static String toExpectedFile(String fileName) {
    return toString(UserProfileTestCase.class, fileName);
  }
  
  public void assertRepo(UserProfileClient client, String expectedFileName) {
    final var expected = toExpectedFile(expectedFileName);
    final var actual = toStaticData(client);
    Assertions.assertLinesMatch(expected.lines(), actual.lines(), actual);
    
  }
  public void assertEquals(String expectedFileName, Object actual) {
    final var expected = toExpectedFile(expectedFileName);
    final var actualJson = JsonObject.mapFrom(actual).encodePrettily();
    Assertions.assertLinesMatch(expected.lines(), actualJson.lines());
    
  }
  
  public static String toString(Class<?> type, String resource) {
    try {
      return IOUtils.toString(type.getClassLoader().getResource(resource), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
