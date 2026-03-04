package io.resys.limaone.spi.program;

import java.util.Optional;

import io.resys.limaone.program.Compiler.Bundle;
import io.resys.limaone.spi.LocalCache;
import io.resys.limaone.spi.LocalCache.Bundle_CacheKey;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DefaultRuntime implements io.resys.limaone.program.Runtime {
  private static final long serialVersionUID = 119708670216052569L;
  private final Optional<Bundle_CacheKey> cacheKey;
  

  @Override
  public Heap getHeap() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EnvironmentProperties getProperties() {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public <T> T getBean(Class<T> type) {
    // TODO Auto-generated method stub
    return null;
  }

  
  public static DefaultRuntime empty() {
    return new DefaultRuntime(Optional.empty());
  }

  public static DefaultRuntime withCache(String cacheKey) {
    return new DefaultRuntime(Optional.of(new Bundle_CacheKey(cacheKey)));
  }
  
  @Override
  public Bundle getBundle() {

    if(cacheKey.isEmpty()) {
      throw new RuntimeException("Can't load bundle");
    }

    return LocalCache.computeIfAbsent(cacheKey.get(), (key) -> {
      throw new RuntimeException("Can't load bundle");
    });
  }
}
