package io.digiexpress.eveli.integration.cache;

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