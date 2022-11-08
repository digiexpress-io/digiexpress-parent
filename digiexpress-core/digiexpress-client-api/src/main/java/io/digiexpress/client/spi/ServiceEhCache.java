package io.digiexpress.client.spi;

import java.util.Optional;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import io.digiexpress.client.api.ServiceCache;
import io.digiexpress.client.api.ServiceDocument.ConfigType;
import io.digiexpress.client.api.ServiceEnvir.Program;
import lombok.extern.slf4j.Slf4j;


@Slf4j
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
    cache.remove(entity.getProgram().getSource().getHash());
  }
  @Override
  public CacheEntry save(Program<?> src) {
    final var entry = ImmutableCacheEntry.builder()
        .id(src.getId())
        .program(src)
        .build();
    final var cache = getCache();
    final var previousEntity = cache.get(src.getSource().getHash());
    if(previousEntity != null) {
      log.info("Overwriting cached entry(id/type/hash): '" + 
          previousEntity.getId() + "/" + 
          previousEntity.getType() + "/" + 
          previousEntity.getProgram().getSource().getHash() + 
      "'");
    }
    cache.put(entry.getId(), entry);
    return entry;
  }
  @SuppressWarnings("unchecked")
  @Override
  public Optional<Program<?>> get(String id) {
    final var cache = getCache();
    final var entity = cache.get(id);
    return Optional.ofNullable(entity).map(e -> e.getProgram());
  }
  
  @lombok.Data @lombok.Builder
  public static class ImmutableCacheEntry implements CacheEntry {
    private static final long serialVersionUID = -1824945964044225824L;
    private final String id;
    private final ConfigType type;
    private final Program<?> program;

    @SuppressWarnings("unchecked")
    @Override
    public <T> Program<T> getProgram() {
      return (Program<T>) program;
    }
  }
}
