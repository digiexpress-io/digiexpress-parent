package io.digiexpress.eveli.client.spi.mq;

import java.time.Duration;
import java.util.Optional;

import com.google.common.collect.ImmutableSet;

import io.digiexpress.eveli.client.api.CommsClient;
import io.digiexpress.eveli.client.api.TaskClient;
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
public class ConsumerForCustomerNotification implements ThenaMqConsumer {

  private final CommsClient commsClient;
  private final TaskClient processClient;
  private static final Duration timeout = Duration.ofMillis(10000);
  
  @Override
  public String getRoutingKey() {
    return "queue.task.suomifi";
  }
  @Override
  public String getConsumerName() {
    return "ConsumerForCustomerNotification";
  }
  @Override
  public String getConsumerComment() {
    return "Notify customer";
  }
  
  @Override
  public MessageResponse accept(QueueMessage msg) {
    try {
      final var notification = msg.getBodyValue().mapTo(TaskNotification.class);
      
      final var process = processClient.queryTaskProcesess().findOneByTaskId(notification.getTaskId()).await().atMost(timeout);
      final Optional<String> userId = process.map(p->p.getUserId());
      
      if (userId.isEmpty()) {
        log.debug("Message for task {} is skipped because user id is missing", notification.getTaskRef());
        // message requires user id, skip, if not present
        return ImmutableMessageResponse.builder()
            .ack(MessageResponseStatus.OK)
            .comment("Message skipped because no user ID")
            .build();     
      }
      // no point to notify user who made the change 
      if(notification.getCustomerId().equals(notification.getUpdaterId())) {
        log.debug("Message for task {} is skipped because user is same as sender", notification.getTaskRef());
        return ImmutableMessageResponse.builder()
            .ack(MessageResponseStatus.OK)
            .comment("Message skipped because receiver is same as sender")
            .build();        
      }
      
      final var customerLocale = notification.getCustomerLocale();
      
      final var builder = commsClient.createCustomerSms()
          .messageId(msg.getId())
          .senderId(userId.get())
          .sms(
              notification.getTitle().get(customerLocale), 
              notification.getMessage().get(customerLocale)
          );
      
      for(final var emailLocale : ImmutableSet.<String>builder()
          .addAll(notification.getEmail().keySet())
          .addAll(notification.getTitle().keySet())
          .addAll(notification.getMessage().keySet()).build()) {
       
        final var emailTitle = notification.getTitle().get(customerLocale);
        final var emailMessage = notification.getEmail().get(emailLocale);
        
        builder.email(emailLocale, emailTitle, emailMessage);
      }
      
      builder.build();
      
      return ImmutableMessageResponse.builder().ack(MessageResponseStatus.OK).build();
    } catch (Exception e) {
      log.error("Failed while accepting new message: \r\n{}", JsonObject.mapFrom(msg).encodePrettily(), e);
      return ImmutableMessageResponse.builder()
          .ack(MessageResponseStatus.ERROR)
          .build();
    }
  }
}
