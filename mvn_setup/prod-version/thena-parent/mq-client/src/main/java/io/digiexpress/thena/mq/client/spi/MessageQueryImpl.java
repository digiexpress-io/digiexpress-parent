package io.digiexpress.thena.mq.client.spi;

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

import java.util.List;

import io.digiexpress.thena.mq.client.api.ThenaMqClient.MessageQuery;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class MessageQueryImpl implements MessageQuery {
  private final ThenaMqChannelState state;
  private Long lastNEntriesToGet;
  
  @Override
  public MessageQuery lastNEntries(long lastNEntriesToGet) {
    this.lastNEntriesToGet = lastNEntriesToGet;
    return this;
  }

  @Override
  public Uni<List<QueueMessage>> findAll() {
    return state.queryMessages().findLastNEntries(lastNEntriesToGet == null ? 100 : lastNEntriesToGet);
  }
}
