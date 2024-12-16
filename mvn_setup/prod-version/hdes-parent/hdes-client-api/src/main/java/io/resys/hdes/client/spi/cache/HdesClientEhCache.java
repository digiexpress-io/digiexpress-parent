package io.resys.hdes.client.spi.cache;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import io.resys.hdes.client.api.HdesCache;
import io.resys.hdes.client.api.ImmutableCacheEntry;
import io.resys.hdes.client.api.ast.AstBody;
import io.resys.hdes.client.api.ast.AstBody.AstSource;
import io.resys.hdes.client.api.programs.Program;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import java.util.Optional;

public class HdesClientEhCache implements HdesCache {
  private static final String CACHE_PREFIX = HdesCache.class.getCanonicalName();
  private final CacheManager cacheManager;
  private final String cacheName;
  
  private HdesClientEhCache(CacheManager cacheManager, String cacheName) {
    super();
    this.cacheManager = cacheManager;
    this.cacheName = cacheName;
  }
  private Cache<String, CacheEntry> getCache() {
    return cacheManager.getCache(cacheName, String.class, CacheEntry.class);
  }
  @Override
  public Optional<Program> getProgram(AstSource src) {
    final var cache = getCache();
    return Optional.ofNullable(cache.get(src.getHash()))
        .or(() -> Optional.ofNullable(cache.get(src.getId())))
        .map(e -> e.getProgram().orElse(null));
  }
  @Override
  public Optional<AstBody> getAst(AstSource src) {
    final var cache = getCache();
    return Optional.ofNullable(cache.get(src.getHash()))
        .or(() -> Optional.ofNullable(cache.get(src.getId())))
        .map(e -> e.getAst());
  }
  @Override
  public Program setProgram(Program program, AstSource src) {
    final var cache = getCache();
    final var previous = cache.get(src.getHash());
    final var entry = ImmutableCacheEntry.builder().from(previous).program(program).build();
    cache.put(entry.getId(), entry);
    cache.put(src.getHash(), entry);
    return program;
  }
  @Override
  public AstBody setAst(AstBody ast, AstSource src) {
    final var entry = ImmutableCacheEntry.builder().id(src.getId()).source(src).ast(ast).build();
    final var cache = getCache();
    cache.put(entry.getId(), entry);
    cache.put(src.getHash(), entry);
    return ast;
  }
  
  @Override
  public HdesCache withName(String name) {
    final var cacheName = createName(name);
    final var cacheHeap = 500;
    final var cacheManager = CacheManagerBuilder.newCacheManagerBuilder() 
        .withCache(cacheName,
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String.class, CacheEntry.class, 
                ResourcePoolsBuilder.heap(cacheHeap))) 
        .build(); 
    cacheManager.init();
    return new HdesClientEhCache(cacheManager, cacheName);
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    public HdesClientEhCache build(String name) {
      final var cacheName = createName(name);
      final var cacheHeap = 500;
      final var cacheManager = CacheManagerBuilder.newCacheManagerBuilder() 
          .withCache(cacheName,
              CacheConfigurationBuilder.newCacheConfigurationBuilder(
                  String.class, CacheEntry.class, 
                  ResourcePoolsBuilder.heap(cacheHeap))) 
          .build(); 
      cacheManager.init();
      
      return new HdesClientEhCache(cacheManager, cacheName);
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
    cache.remove(entity.getSource().getHash());
  }
}
