package io.digiexpress.client.spi;

import java.util.Optional;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import io.digiexpress.client.api.ComposerCache;
import io.digiexpress.client.api.ComposerEntity;
import io.digiexpress.client.api.ComposerEntity.DefinitionState;
import io.digiexpress.client.api.ComposerEntity.HeadState;
import io.digiexpress.client.api.ComposerEntity.TagState;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ComposerEhCache implements ComposerCache {
  private static final String CACHE_PREFIX = ComposerCache.class.getCanonicalName();
  private final CacheManager cacheManager;
  private final String cacheName;
  
  private ComposerEhCache(CacheManager cacheManager, String cacheName) {
    super();
    this.cacheManager = cacheManager;
    this.cacheName = cacheName;
  }
  @Override
  public ComposerEhCache withName(String name) {
    final var cacheName = createName(name);
    final var cacheHeap = 500;
    final var cacheManager = CacheManagerBuilder.newCacheManagerBuilder() 
        .withCache(cacheName,
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String.class, ComposerCacheEntry.class, 
                ResourcePoolsBuilder.heap(cacheHeap))) 
        .build(); 
    cacheManager.init();
    return new ComposerEhCache(cacheManager, cacheName);
  }
  @Override
  public void flush() {
    final var cache = getCache();
    cache.clear();
  }
  @Override
  public void flush(String id) {
    final var cache = getCache();
    final var entity = cache.get(id);
    if(entity == null) {
      return;
    }
    cache.remove(entity.getId());
  }
  @Override
  public ComposerCacheEntry save(DefinitionState src) {
    final var entry = ImmutableComposerCacheEntry.builder()
        .id(src.getDefinition().getId())
        .value(src)
        .build();
    final var cache = getCache();
    final var previousEntity = cache.get(src.getDefinition().getId());
    if(previousEntity != null) {
      log.info("Overwriting cached definition state(id/type/version): '" + 
          previousEntity.getId() + "/" + 
          previousEntity.getType() + "/" + 
          ((DefinitionState) previousEntity.getValue()).getDefinition().getVersion() + 
      "'");
    }
    cache.put(entry.getId(), entry);
    return entry;
  }
  @Override
  public ComposerCacheEntry save(HeadState src) {
    final var entry = ImmutableComposerCacheEntry.builder()
        .id(src.getName())
        .value(src)
        .build();
    final var cache = getCache();
    final var previousEntity = cache.get(src.getName());
    if(previousEntity != null) {
      log.info("Overwriting cached definition state(id/type/commit): '" + 
          previousEntity.getId() + "/" + 
          previousEntity.getType() + "/" + 
          ((HeadState) previousEntity.getValue()).getCommit() + 
      "'");
    }
    cache.put(entry.getId(), entry);
    return entry;
  }
  
  @Override
  public ComposerCacheEntry save(TagState src) {
    final var entry = ImmutableComposerCacheEntry.builder()
        .id(src.getName())
        .value(src)
        .build();
    final var cache = getCache();
    final var previousEntity = cache.get(src.getName());
    if(previousEntity != null) {
      log.info("Overwriting cached tag state(id/type/name): '" + 
          previousEntity.getId() + "/" + 
          previousEntity.getType() + "/" + 
          ((TagState) previousEntity.getValue()).getName() + 
      "'");
    }
    cache.put(entry.getId(), entry);
    return entry;
  }
  @Override
  public Optional<TagState> getTagState(String id) {
    final var cache = getCache();
    final var entity = cache.get(id);
    return Optional.ofNullable(entity).map(e -> e.getValue());
  }
  @Override
  public Optional<DefinitionState> getDefinitionState(String id) {
    final var cache = getCache();
    final var entity = cache.get(id);
    return Optional.ofNullable(entity).map(e -> e.getValue());
  }
  @Override
  public Optional<HeadState> getHeadState(String id) {
    final var cache = getCache();
    final var entity = cache.get(id);
    return Optional.ofNullable(entity).map(e -> e.getValue());
  }
  
  public static Builder builder() {
    return new Builder();
  }
  private Cache<String, ComposerCacheEntry> getCache() {
    return cacheManager.getCache(cacheName, String.class, ComposerCacheEntry.class);
  }
  private static String createName(String name) {
    return CACHE_PREFIX + "-" + name;
  }
  
  public static class Builder {
    public ComposerEhCache build(String name) {
      final var cacheName = createName(name);
      final var cacheHeap = 500;
      final var cacheManager = CacheManagerBuilder.newCacheManagerBuilder() 
          .withCache(cacheName,
              CacheConfigurationBuilder.newCacheConfigurationBuilder(
                  String.class, ComposerCacheEntry.class, 
                  ResourcePoolsBuilder.heap(cacheHeap))) 
          .build(); 
      cacheManager.init();
      
      return new ComposerEhCache(cacheManager, cacheName);
    }
  }
  
  @lombok.Data @lombok.Builder
  public static class ImmutableComposerCacheEntry implements ComposerCacheEntry {
    private static final long serialVersionUID = -1824945964044225824L;
    private final String id;
    private final ComposerCacheEntryType type;
    private final ComposerEntity value;

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ComposerEntity> T getValue() {
      return (T) value;
    }
  }
}
