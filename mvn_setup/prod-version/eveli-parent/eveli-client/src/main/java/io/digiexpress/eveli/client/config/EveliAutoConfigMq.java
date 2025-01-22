package io.digiexpress.eveli.client.config;

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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.digiexpress.eveli.client.spi.mq.LoggingThenaMqConsumer;
import io.digiexpress.eveli.client.spi.mq.MqScheduler;
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
  public ThenaMqClient mqClient(io.vertx.mutiny.pgclient.PgPool pgPool, EveliPropsMq props) {
    return ThenaMqChannelStateImpl.create().db(props.getChannelName()).client(pgPool).build();
  }
  
  @Bean
  public ThenaMqAppConfig mqAppConfig(ThenaMqClient client, EveliPropsMq props, List<ThenaMqConsumer> consumers) {
    
    final var builder = client.channelBuilder()
      .channelName(props.getChannelName())
      .comment("created with spring boot config for task client")
      .appId(props.getAppId());

    for(final var consumer : consumers) {
      builder.addConsumer(worker -> worker
          .routingKey(consumer.getRoutingKey())
          .consumerName(consumer.getConsumerName())
          .comment(consumer.getConsumerComment())
          .build(consumer)); 
    }
    
    return builder.build()
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
  public MqScheduler mqScheduler(ThenaMqClient client, ThenaMqAppConfig config) {
    return new MqScheduler(config, client);
  }

  @Bean
  public ThenaMqConsumer loggingThenaMqConsumer() {
    return new LoggingThenaMqConsumer() ;
  }

  public static class EveliAutoConfigMqException extends RuntimeException {
    private static final long serialVersionUID = 6360677780999109334L;
    public EveliAutoConfigMqException(String message) {
      super(message);
    }    
  }
}
