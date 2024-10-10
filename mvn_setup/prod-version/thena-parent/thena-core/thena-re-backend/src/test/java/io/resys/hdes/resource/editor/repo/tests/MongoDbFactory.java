package io.resys.hdes.resource.editor.repo.tests;

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
import java.util.function.Consumer;
import java.util.function.Function;

import org.bson.codecs.DocumentCodecProvider;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.jsr310.Jsr310CodecProvider;
import org.bson.internal.ProvidersCodecRegistry;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import io.resys.hdes.resource.editor.spi.support.MongoWrapper.MongoTransaction;

public class MongoDbFactory {
  private static final MongodStarter starter = MongodStarter.getDefaultInstance();
  private MongodExecutable executable;
  private MongodProcess process;
  private MongoClient client;
  
  
  public static class MockMongoTransaction implements MongoTransaction {
    private final MongoClient client;
    public MockMongoTransaction(MongoClient client) {
      super();
      this.client = client;
    }
    @Override
    public <T> T accept(Function<MongoClient, T> action) {
      return action.apply(client);
    }
    
  }

  public static void instance(Consumer<MongoTransaction> consumer) {
    MongoDbFactory config = new MongoDbFactory();
    try {
      config.setUp();
      consumer.accept(new MockMongoTransaction(config.client));
    } finally {
      config.tearDown();
    }
  }

  public void setUp() {
    try {
      final int port = 12345;
      
      CodecRegistry codecRegistry = new ProvidersCodecRegistry(Arrays.asList(
          //new PMCodecProvider(),
          new DocumentCodecProvider(),
          new Jsr310CodecProvider(),
          new ValueCodecProvider()
          ));
      
      executable = starter.prepare(MongodConfig.builder()
        .version(Version.Main.PRODUCTION)
          .net(new Net("localhost", port, Network.localhostIsIPv6()))
          .build());
      process = executable.start();
      
      client = MongoClients.create(
          MongoClientSettings.builder()
          .codecRegistry(codecRegistry)
          .applyToConnectionPoolSettings(builder -> builder.build())
          .applyToClusterSettings(builder -> builder
              .hosts(Arrays.asList(new ServerAddress("localhost", port)))
              .build() )
          .build());
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public void tearDown() {
    if (process != null) {
      process.stop();
    }
    if (executable != null) {
      executable.stop();
    }
  }

  public MongoClient getClient() {
    return client;
  }
}
