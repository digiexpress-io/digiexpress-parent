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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.digiexpress.thena.mq.client.api.ThenaMqClient.DeliveryQuery;
import io.digiexpress.thena.mq.client.api.entities.Delivery;
import io.digiexpress.thena.mq.client.api.entities.ImmutableDelivery;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DeliveryQueryImpl implements DeliveryQuery {
  private final ThenaMqChannelState state;
  private Long lastNEntriesToGet;

  @Override
  public DeliveryQuery lastNEntries(long lastNEntriesToGet) {
    this.lastNEntriesToGet = lastNEntriesToGet;
    return this;
  }

  @Override
  public Uni<List<Delivery>> findAll() {
    final var n = lastNEntriesToGet == null ? 100 : lastNEntriesToGet;
    return Uni.combine().all().unis(
        state.queryDeliveries().findLastNEntries(n),
        state.queryDeliveries().findLastNAttemptEntries(n)
    ).asTuple().onItem().transform(tuple -> {
      final var byDelivery = tuple.getItem2().stream().collect(Collectors.groupingBy(e -> e.getDeliveryId()));
      return tuple.getItem1().stream().map(e -> ImmutableDelivery.builder()
          .from(e)
          .addAllAttempts(byDelivery.getOrDefault(e.getId(), Collections.emptyList())).build())
          .collect(Collectors.toList());
    });
  }
}
