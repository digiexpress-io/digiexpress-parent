package io.digiexpress.thena.mq.client.api.entities;

/*-
 * #%L
 * thena-mq-client
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

import java.time.OffsetDateTime;
import java.util.List;

import org.immutables.value.Value;

import io.digiexpress.thena.mq.client.api.ThenaMqConsumer.MessageResponseStatus;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface Delivery extends ThenaMqEntity {
  String getId();
  String getMessageId();
  String getQueueId();
  String getConsumerId();
  DeliveryStatus getStatus();
  
  OffsetDateTime getCreatedAt();
  OffsetDateTime getStartsAt();
  OffsetDateTime getExpiresAt();
  @Nullable OffsetDateTime getCompletedAt();
  List<DeliveryAttempt> getAttempts();
  
  @Value.Immutable
  interface DeliveryAttempt extends ThenaMqEntity {
    String getId();
    String getDeliveryId();
    
    OffsetDateTime getCreatedAt();
    @Nullable String getConsumerComment();
    @Nullable JsonObject getConsumerError();
    @Nullable MessageResponseStatus getConsumerStatus();
  }
  
  enum DeliveryStatus {
    OPEN, COMPLETED
  }
}
