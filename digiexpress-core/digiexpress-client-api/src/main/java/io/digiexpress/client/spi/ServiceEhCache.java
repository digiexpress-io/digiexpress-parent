package io.digiexpress.client.spi;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import io.digiexpress.client.api.ServiceCache;

public class ServiceEhCache implements ServiceCache {
  private static final String CACHE_PREFIX = ServiceCache.class.getCanonicalName();
  private final CacheManager cacheManager;
  private final String cacheName;
  
  private ServiceEhCache(CacheManager cacheManager, String cacheName) {
    super();
    this.cacheManager = cacheManager;
    this.cacheName = cacheName;
  }
  private Cache<String, CacheEntry> getCache() {
    return cacheManager.getCache(cacheName, String.class, ServiceCache.CacheEntry.class);
  }
  
  @Override
  public ServiceEhCache withName(String name) {
    final var cacheName = createName(name);
    final var cacheHeap = 500;
    final var cacheManager = CacheManagerBuilder.newCacheManagerBuilder() 
        .withCache(cacheName,
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String.class, ServiceCache.CacheEntry.class, 
                ResourcePoolsBuilder.heap(cacheHeap))) 
        .build(); 
    cacheManager.init();
    return new ServiceEhCache(cacheManager, cacheName);
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    public ServiceEhCache build(String name) {
      final var cacheName = createName(name);
      final var cacheHeap = 500;
      final var cacheManager = CacheManagerBuilder.newCacheManagerBuilder() 
          .withCache(cacheName,
              CacheConfigurationBuilder.newCacheConfigurationBuilder(
                  String.class, ServiceCache.CacheEntry.class, 
                  ResourcePoolsBuilder.heap(cacheHeap))) 
          .build(); 
      cacheManager.init();
      
      return new ServiceEhCache(cacheManager, cacheName);
    }
  }
  
  private static String createName(String name) {
    return CACHE_PREFIX + "-" + name;
  }
  @Override
  public void flush(String id) {
    final var cache = getCache();
    final var entity = cache.get(id);
    if(entity == null) {
      return;
    }
    cache.remove(entity.getId());
    //cache.remove(entity.getSource().getHash());
  }
}
