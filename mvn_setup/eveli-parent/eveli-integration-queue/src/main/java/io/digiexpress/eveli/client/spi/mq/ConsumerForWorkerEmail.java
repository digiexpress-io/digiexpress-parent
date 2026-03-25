package io.digiexpress.eveli.client.spi.mq;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.digiexpress.eveli.client.api.CommsClient;
import io.digiexpress.eveli.client.api.OrgClient;
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
  private final OrgClient orgClient;
  private final TaskNotificationTransformer transformer;
  private final String email_locale = "fi";

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
      if (log.isDebugEnabled()) {
        log.debug("Handling email queue message: id: {}, body: {}", msg.getId(), notification);
      }
      commsClient.createEmail()
        .message(getMessage(notification))
        .title(getTitle(notification))
        .refId(notification.getTaskRef())
        .recipientAddress(getEmails(notification))
        .build();
      
      return ImmutableMessageResponse.builder().ack(MessageResponseStatus.OK).build();
    } catch (Exception e) {
      log.error("Failed while accepting new message: \r\n{}", JsonObject.mapFrom(msg).encodePrettily(), e);
      return ImmutableMessageResponse.builder()
          .ack(MessageResponseStatus.ERROR)
          .build();
    }
  }
  
  private String getMessage(TaskNotification notification) {
    return transformer.transform(notification.getEmail().get(email_locale), notification, email_locale);
  }
  
  private String getTitle(TaskNotification notification) {
    return transformer.transform(notification.getTitle().get(email_locale), notification, email_locale);
  }
  
  private List<String> getEmails(TaskNotification notification) {
    final List<String> emails = StringUtils.isNotEmpty(notification.getAssigneeEmail()) ? Arrays.asList(notification.getAssigneeEmail()) :
        (StringUtils.isNotEmpty(notification.getTaskGroupId()) ? 
            orgClient.queryGroupEmails().findAllByGroupName(notification.getTaskGroupId()) :Collections.emptyList()); 
    
    final var updaterIsAssignee = notification.getUpdaterId().equals(notification.getAssigneeId());
    
    if(updaterIsAssignee) {
      return emails.stream()
        .filter(e -> updaterIsAssignee ? !e.equals(notification.getAssigneeEmail()) : true)
        .toList();
    }
    
    return emails;
  }
}
