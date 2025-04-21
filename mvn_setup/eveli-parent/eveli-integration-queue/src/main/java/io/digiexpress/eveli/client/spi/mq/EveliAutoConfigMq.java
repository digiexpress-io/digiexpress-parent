package io.digiexpress.eveli.client.spi.mq;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import io.digiexpress.eveli.client.api.CommsClient;
import io.digiexpress.eveli.client.api.OrgClient;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.config.EveliPropsMq;
import io.digiexpress.eveli.client.web.resources.worker.QueueApiController;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.thena.mq.client.api.ThenaMqAppConfig;
import io.digiexpress.thena.mq.client.api.ThenaMqClient;
import io.digiexpress.thena.mq.client.api.ThenaMqConsumer;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope.OperationStatus;
import io.digiexpress.thena.mq.client.spi.persistence.ThenaMqChannelStateImpl;
import lombok.extern.slf4j.Slf4j;



@Configuration
@Slf4j
public class EveliAutoConfigMq {
 
  @Bean 
  @ConditionalOnMissingBean(value = ThenaMqClient.class)
  public ThenaMqClient mqClient(io.vertx.mutiny.pgclient.PgPool pgPool, EveliPropsMq props) {
    return ThenaMqChannelStateImpl.create()
        .db(props.getChannelName()).client(pgPool)
        .build();
  }
  
  @Bean
  @ConditionalOnMissingBean
  public ThenaMqAppConfig mqAppConfig(ThenaMqClient client, EveliPropsMq props, List<ThenaMqConsumer> consumers) {
    
    final var builder = client.channelBuilder()
      .channelName(props.getChannelName())
      .comment("created with spring boot config for task client")
      .appId(props.getAppId())
      .addQueue(b -> b
          .queueName("queue.task.log")
          .comment("task logging queue")
          .build())
      .addQueue(b -> b
          .queueName("queue.task.worker_email")
          .comment("task queue for internal worker emails")
          .build())
      .addQueue(b -> b
          .queueName("queue.task.suomifi")
          .comment("task queue for sms notifications")
          .build());

    for(final var consumer : consumers) {
      builder.addConsumer(worker -> worker
          .routingKey(consumer.getRoutingKey())
          .consumerName(consumer.getConsumerName())
          .comment(consumer.getConsumerComment())
          .build(consumer)); 
    }
    
    return builder
        .build()
        .onItem().transform(resp -> {
          if( resp.getOperationStatus() == OperationStatus.ERROR || 
              resp.getOperationStatus() == OperationStatus.CONFLICT) {
            
            final var allLogs = resp.getOperationLogs().stream().map(e -> e.getText()).toList();
            final var logs = String.join("\r\n", allLogs);
            log.error("Failed to start MQ config because of:\r\n", logs);
            throw new EveliAutoConfigMqException(logs);
          }
          return resp.getObject();
        })
        .await().atMost(Duration.ofMinutes(1));   
  }
  
  @Bean 
  public DeliveryForChannels mqScheduler(ThenaMqClient client, ThenaMqAppConfig config) {
    return new DeliveryForChannels(config, client);
  }
  @Bean
  public ThenaMqConsumer consumerForCustomerNotification(CommsClient client, ProcessClient proc) {
    return new ConsumerForCustomerNotification(client, proc);
  }
  @Bean
  public ThenaMqConsumer consumerForWorkerEmail(CommsClient client, OrgClient orgClient) {
    return new ConsumerForWorkerEmail(client, orgClient);
  }
  @Bean
  public ThenaMqConsumer loggingThenaMqConsumer() {
    return new ConsumerForLogging();
  }
  @Bean
  public PublisherForTaskEvents queueWriter(TaskClient taskClient, ThenaMqClient mqClient, EveliEnvirClient envir) {
    return new PublisherForTaskEvents(taskClient, mqClient, envir);
  }
  @Bean
  public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
    final var eventMulticaster = new SimpleApplicationEventMulticaster();
    eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
    return eventMulticaster;
  }
  public static class EveliAutoConfigMqException extends RuntimeException {
    private static final long serialVersionUID = 6360677780999109334L;
    public EveliAutoConfigMqException(String message) {
      super(message);
    }    
  }
  @Bean 
  public QueueApiController queueApiController(ThenaMqClient client, ThenaMqAppConfig config) {
    return new QueueApiController(client, config);
  }
}
