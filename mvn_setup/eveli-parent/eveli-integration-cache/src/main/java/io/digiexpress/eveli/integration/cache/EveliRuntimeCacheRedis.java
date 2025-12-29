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

import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliRuntime;
import io.digiexpress.eveli.envir.spi.EveliRuntimeCacheInMemory;
import io.digiexpress.eveli.envir.spi.actions.EveliRuntimeCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class EveliRuntimeCacheRedis implements EveliRuntimeCache, MessageListener {
  
  private final EveliRuntimeCacheInMemory delegate;
  private final StringRedisTemplate redisTemplate;
  
  private static final String CHANNEL_DEPLOYMENT = "eveli:envir:cache:invalidate:deployment";
  private static final String CHANNEL_RUNTIME = "eveli:envir:cache:invalidate:runtime";
  private static final String CHANNEL_ALL = "eveli:envir:cache:invalidate:all";
  public static final String CHANNEL_LISTENER = "eveli:envir:cache:invalidate:*";
  
  // ========== Delegate read operations ==========
  
  @Override
  public Optional<EveliDeployment> getDeployment() {
    return delegate.getDeployment();
  }
  
  @Override
  public Optional<EveliRuntime> getRuntime(String runtimeId) {
    return delegate.getRuntime(runtimeId);
  }
  
  // ========== Write operations with Redis pub/sub ==========
  
  @Override
  public EveliDeployment save(EveliDeployment deployment) {
    final var result = delegate.save(deployment);
    publishInvalidation(CHANNEL_DEPLOYMENT);
    return result;
  }
  
  @Override
  public EveliRuntime save(EveliRuntime runtime) {
    final var result = delegate.save(runtime);
    publishInvalidation(CHANNEL_RUNTIME);
    return result;
  }
  
  public void invalidateId() {
    delegate.invalidateId();
    publishInvalidation(CHANNEL_DEPLOYMENT);
  }
  
  public void invalidateAll() {
    delegate.invalidateAll();
    publishInvalidation(CHANNEL_ALL);
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
    
    if (channel.endsWith(":deployment")) {
      delegate.invalidateId();
    } else if (channel.endsWith(":runtime")) {
      delegate.invalidateAll();
    } else if (channel.endsWith(":all")) {
      delegate.invalidateAll();
    } else {
      log.warn("Unknown invalidation channel: {}", channel);
    }
  }
}
