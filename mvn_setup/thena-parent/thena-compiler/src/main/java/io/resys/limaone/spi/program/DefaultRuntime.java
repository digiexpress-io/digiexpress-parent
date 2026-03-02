package io.resys.limaone.spi.program;

import io.resys.limaone.program.Compiler.Bundle;
import io.resys.limaone.spi.LocalCache;
import io.resys.limaone.spi.bundler.BundleBuilderImpl;

public class DefaultRuntime implements io.resys.limaone.program.Runtime {
  private static final long serialVersionUID = 119708670216052569L;
  
  

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
    return new DefaultRuntime();
  }

  @Override
  public Bundle getBundle() {
    return LocalCache.computeIfAbsent(BundleBuilderImpl.LAST_BUNDLE, (key) -> {
      throw new RuntimeException("Can't load bundle");
    });
  }
}
