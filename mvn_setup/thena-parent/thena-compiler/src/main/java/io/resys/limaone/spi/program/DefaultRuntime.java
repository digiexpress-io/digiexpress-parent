package io.resys.limaone.spi.program;

import java.util.Optional;

import io.resys.limaone.program.Compiler.Bundle;
import io.resys.limaone.spi.LocalCache;
import io.resys.limaone.spi.LocalCache.Bundle_CacheKey;
import io.resys.limaone.spi.dialob.FormDb;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DefaultRuntime implements io.resys.limaone.program.Runtime {
  private static final long serialVersionUID = 119708670216052569L;
  private final Optional<Bundle_CacheKey> cacheKey;
  private final Optional<Bundle> bundle;
  private final Optional<FormDb> formDb;

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
  
  @Override
  public FormDb getFormDb() {
    if(formDb.isPresent()) {
      return formDb.get();
    }
    throw new RuntimeException("Can't load formDb");
  }  
  @Override
  public Bundle getBundle() {
    if(bundle.isPresent()) {
      return bundle.get();
    }

    if(cacheKey.isEmpty()) {
      throw new RuntimeException("Can't load bundle");
    }

    return LocalCache.computeIfAbsent(cacheKey.get(), (key) -> {
      throw new RuntimeException("Can't load bundle");
    });
  }
  
  public DefaultRuntime withFormDb(FormDb formDb) {
    return new DefaultRuntime(cacheKey, bundle, Optional.of(formDb));
  }

  public static DefaultRuntime empty() {
    return new DefaultRuntime(Optional.empty(), Optional.empty(), Optional.empty());
  }
  public static DefaultRuntime withCache(String cacheKey) {
    return new DefaultRuntime(Optional.of(new Bundle_CacheKey(cacheKey)), Optional.empty(), Optional.empty());
  }
  public static DefaultRuntime withBundle(Bundle bundle) {
    return new DefaultRuntime(Optional.empty(), Optional.of(bundle), Optional.empty());
  }
}
