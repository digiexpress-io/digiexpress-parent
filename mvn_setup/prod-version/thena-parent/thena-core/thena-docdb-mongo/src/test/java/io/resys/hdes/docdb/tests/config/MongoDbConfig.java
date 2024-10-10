package io.resys.hdes.docdb.tests.config;

/*-
 * #%L
 * hdes-storage-mongodb
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
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
import java.util.Arrays;

import org.bson.codecs.DocumentCodecProvider;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.jsr310.Jsr310CodecProvider;
import org.bson.internal.ProvidersCodecRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import io.quarkus.mongodb.impl.ReactiveMongoClientImpl;
import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.api.models.Diff;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.DocDBCodecProvider;
import io.resys.thena.docdb.spi.DocDBFactory;
import io.resys.thena.docdb.spi.DocDBPrettyPrinter;

public abstract class MongoDbConfig {
  private static final MongodStarter starter = MongodStarter.getDefaultInstance();
  
  private MongodExecutable executable;
  private MongodProcess process;
  private ReactiveMongoClient mongo;
  private DocDB client;

  @BeforeEach
  void startDB() {
    this.setUp();
  }


  @AfterEach
  void stopDB() {
    this.tearDown();
  }

  private void setUp() {
    try {
      final int port = 12345;
      
      CodecRegistry codecRegistry = new ProvidersCodecRegistry(Arrays.asList(
          new DocDBCodecProvider(),
          new DocumentCodecProvider(),
          new Jsr310CodecProvider(),
          new ValueCodecProvider()
          ));
      
      executable = starter.prepare(MongodConfig.builder()
        .version(Version.Main.PRODUCTION)
        .net(new Net("localhost", port, Network.localhostIsIPv6()))
        .build());
      process = executable.start();
      
      MongoClient client = MongoClients.create(MongoClientSettings.builder()
          .codecRegistry(codecRegistry)
          .applyToConnectionPoolSettings(builder -> builder.build())
          .applyToClusterSettings(builder -> builder
              .hosts(Arrays.asList(new ServerAddress("localhost", port)))
              .build())
          .build());
      
        this.mongo = new ReactiveMongoClientImpl(client);
        this.client = DocDBFactory.create()
            .db("junit")
            .client(mongo)
            .build();
    } catch (IOException e) {
      tearDown();
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private void tearDown() {
    if (process != null) {
      process.stop();
    }
    if (executable != null) {
      executable.stop();
    }
  }

  public ClientState createState() {
    final var ctx = ClientCollections.defaults("junit");
    return DocDBFactory.state(ctx, mongo);
  }

  public void printDiff(Diff repo) {
    final String result = new DocDBPrettyPrinter(createState()).print(repo);
    System.out.println(result);
  }
  public void printRepo(Repo repo) {
    final String result = new DocDBPrettyPrinter(createState()).print(repo);
    System.out.println(result);
  }
  
  
  public DocDB getClient() {
    return client;
  }
}
