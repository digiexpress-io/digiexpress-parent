package io.digiexpress.thena.mq.client.api.persistence;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import org.immutables.value.Value;

import io.digiexpress.thena.mq.client.api.entities.Channel;

@Value.Immutable
public abstract class ThenaMqTableNames {
  private static final ThenaMqTableNames DEFAULTS = defaults("");
    
  public abstract String getDb();
  public abstract String getPrefix();
  public abstract String getChannel();

  public abstract String getQueues();
  public abstract String getMessages();
  public abstract String getBindings();
  public abstract String getDelivery();
  public abstract String getDeliveryAttempt();

  
  public ThenaMqTableNames toChannel(Channel repo) {
    final var prefix = repo.getPrefix();
    return ImmutableThenaMqTableNames.builder()
      .db(this.getDb())
      .channel(this.getChannel())
      .prefix(prefix)

      .queues((          prefix + DEFAULTS.getQueues()).toUpperCase())
      .messages((        prefix + DEFAULTS.getMessages()).toUpperCase())
      .bindings((        prefix + DEFAULTS.getBindings()).toUpperCase())
      .delivery((        prefix + DEFAULTS.getDelivery()).toUpperCase())
      .deliveryAttempt(( prefix + DEFAULTS.getDeliveryAttempt()).toUpperCase())
      
      .build();
  }
  
  public static ThenaMqTableNames defaults(String db) {
    return ImmutableThenaMqTableNames.builder()
        .db(db == null ? "mqdb" : db)
        .channel("CHANNEL")
        .prefix("")

        .queues("THENAMQ_QUEUES")
        .messages("THENAMQ_MESSAGES")
        .bindings("THENAMQ_BINDINGS")
        .delivery("THENAMQ_DELIVERY")
        .deliveryAttempt("THENAMQ_DELIVERY_ATTEMPT")

        .build();
  }
}
