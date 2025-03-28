package io.digiexpress.eveli.client.spi.mq;

import io.digiexpress.eveli.client.api.CommsClient;
import io.digiexpress.eveli.client.spi.mq.WrenchFlowCommand.TaskNotification;

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

import io.digiexpress.thena.mq.client.api.ImmutableMessageResponse;
import io.digiexpress.thena.mq.client.api.ThenaMqConsumer;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class ConsumerForWorkerEmail implements ThenaMqConsumer {
  
  private final CommsClient commsClient;
  
  @Override
  public String getRoutingKey() {
    return "queue.task.worker_email";
  }
  @Override
  public String getConsumerName() {
    return "ConsumerForWorkerEmail";
  }
  @Override
  public String getConsumerComment() {
    return "Notify worker";
  }
  
  @Override
  public MessageResponse accept(QueueMessage msg) {
    try {
      final var notification = msg.getBodyValue().mapTo(TaskNotification.class);
      
      
      return ImmutableMessageResponse.builder().ack(MessageResponseStatus.OK).build();
    } catch (Exception e) {
      log.error("Failed while accepting new message: \r\n{}", JsonObject.mapFrom(msg).encodePrettily());
      return ImmutableMessageResponse.builder()
          .ack(MessageResponseStatus.ERROR)
          .build();
    }

  }
}
