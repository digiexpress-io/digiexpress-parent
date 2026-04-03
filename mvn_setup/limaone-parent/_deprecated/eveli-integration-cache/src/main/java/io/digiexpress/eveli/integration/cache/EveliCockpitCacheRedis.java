package io.digiexpress.eveli.integration.cache;

/*-
 * #%L
 * eveli-integration-cache
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

import java.util.Optional;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;

import io.digiexpress.thena.cockpit.client.api.CockpitAware.CockpitContainerCache;
import io.digiexpress.thena.cockpit.client.api.CockpitContainer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class EveliCockpitCacheRedis implements CockpitContainerCache, MessageListener {
  
  private final CockpitContainerCache delegate;
  private final StringRedisTemplate redisTemplate;
  
  public static final String CHANNEL_LISTENER = "eveli:cockpit:cache:invalidate";
  

  @Override
  public boolean contains(String anyId) {
    return delegate.contains(anyId);
  }
  @Override
  public Optional<CockpitContainer> get(String anyId) {
    return delegate.get(anyId);
  }
  @Override
  public Optional<CockpitContainer> save(String anyId, Optional<CockpitContainer> container) {
    final var result = delegate.save(anyId, container);
    publishInvalidation(CHANNEL_LISTENER);
    return result;
  }
  
  public void invalidateAll() {
    delegate.invalidateAll();
    publishInvalidation(CHANNEL_LISTENER);
  }
  
  // ========== Redis Pub/Sub ==========
  private void publishInvalidation(String channel) {
    try {
      redisTemplate.convertAndSend(channel, "");
      log.debug("Published invalidation signal to channel: {}", channel);
    } catch (Exception e) {
      log.error("Failed to publish cache invalidation to Redis. Local cache still updated.", e);
    }
  }
  
  @Override
  public void onMessage(Message message, byte[] pattern) {
    final String channel = new String(message.getChannel());
    log.info("Received cache invalidation signal from channel: {}", channel);
    delegate.invalidateAll();
  }
}
