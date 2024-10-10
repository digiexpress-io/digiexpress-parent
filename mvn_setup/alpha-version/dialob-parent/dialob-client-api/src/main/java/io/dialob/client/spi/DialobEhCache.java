package io.dialob.client.spi;


import io.dialob.client.api.DialobCache;
import io.dialob.client.api.DialobDocument;
import io.dialob.client.api.DialobStore.StoreEntity;
import io.dialob.client.api.ImmutableCacheEntry;
import io.dialob.program.DialobProgram;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import java.util.ArrayList;
import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
public class DialobEhCache implements DialobCache {

  private static final String CACHE_PREFIX = DialobCache.class.getCanonicalName();
  private final CacheManager cacheManager;
  private final String cacheName;

  private Cache<String, CacheEntry> getCache() {
    return cacheManager.getCache(cacheName, String.class, CacheEntry.class);
  }
  @Override
  public Optional<DialobProgram> getProgram(StoreEntity src) {
    final var id = toCacheId(src);
    final var cache = getCache();
    final var result = Optional.ofNullable(cache.get(id)).map(e -> e.getProgram().orElse(null));
    log.debug("Dialob, caching, program resolved: " + result.isPresent() + ", id: " + id);
    return result;
  }
  @Override
  public Optional<DialobDocument> getAst(StoreEntity src) {
    final var id = toCacheId(src);
    log.debug("Dialob, caching, document resolved: " + id);
    final var cache = getCache();
    final var result = Optional.ofNullable(cache.get(id)).map(e -> e.getAst());
    return result;
  }
  @Override
  public DialobProgram setProgram(DialobProgram program, StoreEntity src) {
    final var id = toCacheId(src);
    log.debug("Dialob, caching a program, id: " + id);
    final var cache = getCache();
    final var previous = cache.get(id);
    final var entry = ImmutableCacheEntry.builder().from(previous).program(program).build();
    cache.put(id, entry);
    return program;
  }
  @Override
  public DialobDocument setAst(DialobDocument ast, StoreEntity src) {
    final var id = toCacheId(src);
    log.debug("Dialob, caching a document, id: " + id);
    final var entry = ImmutableCacheEntry.builder().id(id).rev(ast.getVersion()).source(src).ast(ast).build();
    final var cache = getCache();
    cache.put(id, entry);
    return ast;
  }
  private String toCacheId(StoreEntity src) {
    return src.getId() + "/" + src.getVersion();
  }

  @Override
  public DialobEhCache withName(String name) {
    final var cacheName = createName(name);
    final var cacheHeap = 500;
    final var cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .withCache(cacheName,
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String.class, CacheEntry.class,
                ResourcePoolsBuilder.heap(cacheHeap)))
        .build();
    cacheManager.init();
    return new DialobEhCache(cacheManager, cacheName);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    public DialobEhCache build(String name) {
      final var cacheName = createName(name);
      final var cacheHeap = 10000;
      final var cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
          .withCache(cacheName,
              CacheConfigurationBuilder.newCacheConfigurationBuilder(
                  String.class, CacheEntry.class,
                  ResourcePoolsBuilder.heap(cacheHeap)))
          .build();
      cacheManager.init();

      return new DialobEhCache(cacheManager, cacheName);
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
    final var flush = new ArrayList<String>();
    flush.add(id);

    cache.forEach(e -> {
      if(e.getKey().startsWith(id + "/")) {
        flush.add(e.getKey());
        return;
      }
      final var ast = e.getValue().getAst();
      if(ast.getId().equals(id) || ast.getName().equals(id)) {
        flush.add(e.getKey());
      }
    });

    flush.forEach(flushId -> cache.remove(flushId));

  }
}
