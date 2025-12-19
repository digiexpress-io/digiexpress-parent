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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.digiexpress.eveli.client.config.EveliPropsEnvir;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliRuntime;
import io.digiexpress.eveli.envir.spi.actions.EveliRuntimeCache;


@Configuration
@EnableCaching
public class EveliCacheConfiguration {

  @Bean
  public EveliRuntimeCache eveliRuntimeCache(EveliPropsEnvir envirProps, RedisConnectionFactory connectionFactory) {
    final RedisTemplate<String, EveliDeployment> short_deployment_cache = createCache(connectionFactory);
    final RedisTemplate<String, EveliRuntime> long_runtime_cache = createCache(connectionFactory);
    
    return new EveliRuntimeCacheRedis(short_deployment_cache, long_runtime_cache, 
        envirProps.getCacheExpirations().getLongRuntime(),
        envirProps.getCacheExpirations().getLongRuntime());
  }
 
  private <T> RedisTemplate<String, T> createCache(RedisConnectionFactory connectionFactory) {
    final RedisTemplate<String, T> redis = new RedisTemplate<>();
    redis.setConnectionFactory(connectionFactory);
    redis.setKeySerializer(new StringRedisSerializer());
    redis.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    redis.afterPropertiesSet();
    return redis;    
  }
}
