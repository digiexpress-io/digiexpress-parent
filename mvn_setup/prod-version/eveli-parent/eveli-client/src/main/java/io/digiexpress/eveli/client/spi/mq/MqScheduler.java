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
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;

import io.digiexpress.thena.mq.client.api.ThenaMqAppConfig;
import io.digiexpress.thena.mq.client.api.ThenaMqClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class MqScheduler {
  
  private final ThenaMqAppConfig config;
  private final ThenaMqClient client;
  
  @Scheduled(fixedRate = 15, timeUnit = TimeUnit.SECONDS)
  public void executeSync() {
    try {
      client.withChannel(config.getChannel())
      .bindingBuilder()
      .build()
      .await().atMost(Duration.ofMinutes(1));
    
      // Deliver the message to the consumers
      client.withChannel(config.getChannel())
        .deliveryBuilder()
        .config(config)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    } catch(Exception e) {
      log.error("Failed to run MQ sync, because of error: {}!", e.getMessage(), e);
    }
  }
}
