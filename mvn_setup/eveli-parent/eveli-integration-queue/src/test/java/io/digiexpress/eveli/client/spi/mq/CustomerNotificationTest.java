package io.digiexpress.eveli.client.spi.mq;

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
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import io.digiexpress.eveli.client.api.CommsClient;
import io.digiexpress.eveli.client.api.ImmutableCreateTaskCommand;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.spi.comms.CustomerSmsBuilderImpl;
import io.digiexpress.eveli.client.spi.comms.EmailBuilderDummy;
import io.digiexpress.eveli.client.spi.mq.WrenchFlowCommand.TaskNotification.MessageType;
import io.digiexpress.eveli.client.test.task.TaskEnvirSetup;
import io.digiexpress.thena.mq.client.api.ThenaMqClient;
import io.digiexpress.thena.mq.client.api.ThenaMqConsumer;
import io.digiexpress.thena.mq.client.api.ThenaMqConsumer.MessageResponseStatus;
import io.digiexpress.thena.mq.client.api.entities.Binding.BindingStatus;
import io.digiexpress.thena.mq.client.api.entities.Delivery.DeliveryStatus;
import io.digiexpress.thena.mq.client.spi.persistence.ThenaMqChannelStateImpl;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class CustomerNotificationTest extends TaskEnvirSetup {
  @Container @ServiceConnection static PostgreSQLContainer<?> CONTAINER = new PostgreSQLContainer<>("postgres:17");
  private static final Duration AWAIT_TIME = Duration.ofMinutes(1L);
  private static ThenaMqClient CLIENT;

  @BeforeAll 
  static void beforeAll() { 
    start(CONTAINER); 
    CLIENT = ThenaMqChannelStateImpl.create().db("junit").client(PGPOOL).build();
  }
  @AfterAll static void afterAll() { end(); }
  
  @Autowired TaskClient taskClient;
  
  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }

  @Test
  public void createOneChannelWithOneQueue() {
    
    final var worker1 = createCustomerNotificationConsumer();
    final var user = "test_user";
    final var email = user+"@resys.io";
    
    final var task = taskClient.taskBuilder()
        .userId(user, email)
        .createTask(ImmutableCreateTaskCommand.builder()
        .subject("test")
        .build())
        .await().atMost(AWAIT_TIME);
    
    final var process = taskClient.createProcess()
        .questionnaireId("1")
        .userId(user)
        .workflowName("test")
        .anon(false)
        .taskId(task.getId())
        .formName("form1")
        .flowName("flow1")
        .commitAuthor(user)
        .commitMessage("test")
        .build();
    final var config = getClient()
      .channelBuilder()
      .channelName("test_1")
      .comment("channel for junit test")
      .appId("tester@tester")
      .addQueue(b -> b
          .queueName("super queue")
          .comment("queue for test case")
          .build())
      .addConsumer(worker -> worker
          .routingKey("super queue")
          .consumerName("consumer-1")
          .comment("customer notification consumer")
          .build(worker1))
      .build()
      .await().atMost(Duration.ofMinutes(1));    
    
    // publish things ... to the queue
    getClient().withChannel(config.getChannel())
      .messageBuilder()
      .routingKey("super queue")
      .comment("my first msg")
      .createdBy("test user")
      
      .bodyType("user-data")
      .bodyId("ssn1")
      .bodyValue(createTaskNotification(task.getId()))
      
      .build()
      .await().atMost(Duration.ofMinutes(1));
    
    
    // Route the message
    getClient().withChannel(config.getChannel())
      .bindingBuilder()
      .build()
      .await().atMost(Duration.ofMinutes(1));
    
    // Deliver the message to the consumers
    // Current wait time for process query is 10 seconds, wait here shorter time
    // to get timeout exception. For longer times test will fail with message response check 
    getClient().withChannel(config.getChannel())
      .deliveryBuilder()
      .config(config.getObject())
      .build()
      .await().atMost(Duration.ofSeconds(60));
  
    
    // Check for bindings
    final var binding = getClient().withChannel(config.getChannel())
        .bindingQuery()
        .findAll()
        .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(1, binding.size());
    Assertions.assertEquals(1, binding.stream().filter(e -> e.getStatus() == BindingStatus.COMPLETED).toList().size());
    
    // 1 delivery for each consumer
    final var deliveries = getClient().withChannel(config.getChannel())
      .deliveryQuery()
      .findAll()
      .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(1, deliveries.size());
    Assertions.assertEquals(1, deliveries.stream().filter(e -> e.getStatus() == DeliveryStatus.COMPLETED && e.getAttempts().stream().allMatch(a->a.getConsumerStatus() == MessageResponseStatus.OK)).toList().size());
    
  }
  
  private JsonObject createTaskNotification(String taskId) {
    WrenchFlowCommand.TaskNotification notification = ImmutableTaskNotification.builder()
        .changeType("test")
        .queue("")
        .customerLocale("fi")
        .customerId("010168-0011")
        .taskRef("TR-202601-1")
        .taskId(taskId)
        .taskGroupIds(List.of("group1"))
        .messageType(MessageType.WORKER_MSG)
        .message(Map.of("fi", "this is message"))
        .title(Map.of("fi", "this is title"))
        .email(Map.of())
        .build();
    return JsonObject.mapFrom(notification);
  }
  ThenaMqConsumer createCustomerNotificationConsumer() {
    CommsClient commsClient = new CommsClient() {

      @Override
      public CustomerMessageBuilder createCustomerSms() {
        return new CustomerSmsBuilderImpl();
      }

      @Override
      public EmailBuilder createEmail() {
        return new EmailBuilderDummy();
      }
      
    };
    ThenaMqConsumer result = new ConsumerForCustomerNotification(commsClient, taskClient, new DefaultTaskNotificationTransformer());
    return result;
  }
  
  ThenaMqClient getClient() {
    return CLIENT;
  }
}
