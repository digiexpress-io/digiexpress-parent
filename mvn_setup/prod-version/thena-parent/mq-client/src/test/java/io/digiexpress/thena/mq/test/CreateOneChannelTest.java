package io.digiexpress.thena.mq.test;

/*-
 * #%L
 * thena-docdb-api
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

import java.io.Serializable;
import java.time.Duration;

import org.immutables.value.Value;
import org.junit.jupiter.api.Test;

import io.digiexpress.thena.mq.client.api.ThenaMqConsumer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class CreateOneChannelTest extends DbTestTemplate {

  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }

  @Test
  public void createOneChannelWithOneQueue() {
    
    final var worker1 = new ThenaMqConsumer() {
      @Override
      public MessageResponse accept(MessageHeader header, MessageBody body) {
        // TODO Auto-generated method stub
        return null;
      }
    };
    
    final var worker2 = new ThenaMqConsumer() {
      @Override
      public MessageResponse accept(MessageHeader header, MessageBody body) {
        // TODO Auto-generated method stub
        return null;
      }
    };
    
    
    final var channel = getClient()
      .channelBuilder()
      .channelName("test_1")
      .comment("channel for junit test")
      .createdBy("quarrkus test runner")
      .build()
      .await().atMost(Duration.ofMinutes(1))
      .getChannel();
  
    // create queue with 2 consumers
    final var queue = getClient().withChannel(channel)
      .queueBuilder()
      .appId("test-app")
      .queueName("super queue")
      .createdBy("tester@tester")
      .comment("queue for test case")
      .addConsumer(worker -> worker.consumerName("consumer-1").comment("test consumer").build(worker1))
      .addConsumer(worker -> worker.consumerName("consumer-2").comment("test consumer").build(worker2))
      .build()
      .await().atMost(Duration.ofMinutes(1));
    
    // publish things ... to the queue
    getClient().withChannel(channel)
      .messageBuilder()
      .comment("my first msg")
      .createdBy("test user")
      .appId("test-app")
      .queueIdOrName("super queue")
      
      .bodyType("user-data")
      .bodyId("ssn1")
      .bodyValue(new JsonObject())
      
      .build()
      .await().atMost(Duration.ofMinutes(1));
    
    
    // Route the message
    getClient().withChannel(channel)
      .bindingBuilder()
      .build()
      .await().atMost(Duration.ofMinutes(1));
    // get hanging messages
    // get available routes
    // match
    //.build();
    
    // Deliver the message to the consumers
  }
}
