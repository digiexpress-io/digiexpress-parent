package io.digiexpress.client.spi;

import java.util.Optional;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import io.digiexpress.client.api.ClientCache;
import io.digiexpress.client.api.ClientEntity.ConfigType;
import io.digiexpress.client.api.AssetEnvir.ServiceProgram;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ClientEhCache implements ClientCache {
  private static final String CACHE_PREFIX = ClientCache.class.getCanonicalName();
  private final CacheManager cacheManager;
  private final String cacheName;
  
  private ClientEhCache(CacheManager cacheManager, String cacheName) {
    super();
    this.cacheManager = cacheManager;
    this.cacheName = cacheName;
  }
  private Cache<String, CacheEntry> getCache() {
    return cacheManager.getCache(cacheName, String.class, ClientCache.CacheEntry.class);
  }
  
  @Override
  public ClientEhCache withName(String name) {
    final var cacheName = createName(name);
    final var cacheHeap = 500;
    final var cacheManager = CacheManagerBuilder.newCacheManagerBuilder() 
        .withCache(cacheName,
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String.class, ClientCache.CacheEntry.class, 
                ResourcePoolsBuilder.heap(cacheHeap))) 
        .build(); 
    cacheManager.init();
    return new ClientEhCache(cacheManager, cacheName);
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    public ClientEhCache build(String name) {
      final var cacheName = createName(name);
      final var cacheHeap = 500;
      final var cacheManager = CacheManagerBuilder.newCacheManagerBuilder() 
          .withCache(cacheName,
              CacheConfigurationBuilder.newCacheConfigurationBuilder(
                  String.class, ClientCache.CacheEntry.class, 
                  ResourcePoolsBuilder.heap(cacheHeap))) 
          .build(); 
      cacheManager.init();
      
      return new ClientEhCache(cacheManager, cacheName);
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
  public CacheEntry save(ServiceProgram src) {
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
  @Override
  public Optional<ServiceProgram> get(String id) {
    final var cache = getCache();
    final var entity = cache.get(id);
    return Optional.ofNullable(entity).map(e -> e.getProgram());
  }
  
  @lombok.Data @lombok.Builder
  public static class ImmutableCacheEntry implements CacheEntry {
    private static final long serialVersionUID = -1824945964044225824L;
    private final String id;
    private final ConfigType type;
    private final ServiceProgram program;

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ServiceProgram> T getProgram() {
      return (T) program;
    }
  }
}
