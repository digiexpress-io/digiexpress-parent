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

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import io.digiexpress.eveli.client.config.EveliAutoConfigEnvir;
import io.digiexpress.eveli.client.config.EveliPropsEnvir;
import io.digiexpress.eveli.envir.spi.actions.EveliRuntimeCache;


@Configuration
@EnableCaching
public class EveliCacheConfiguration {

  @Bean
  public EveliRuntimeCache eveliRuntimeCache(EveliPropsEnvir envirProps, StringRedisTemplate redisTemplate) {
    final var delegate = EveliAutoConfigEnvir.defaultEnvirCache(envirProps);
    return new EveliRuntimeCacheRedis(delegate, redisTemplate);
  }
  
  @Bean
  public RedisMessageListenerContainer eveliEnvirCacheListener(
      RedisConnectionFactory connectionFactory,
      EveliRuntimeCache cache) {
    
    final var container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.addMessageListener((EveliRuntimeCacheRedis) cache, new PatternTopic(EveliRuntimeCacheRedis.CHANNEL_LISTENER));
    return container;
  }
}
