package io.resys.limaone.spi.program;

import io.resys.limaone.program.Compiler.Bundle;

public class DefaultRuntime implements io.resys.limaone.program.Runtime {

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
  public ProgramParameters getParameters() {
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
    // TODO Auto-generated method stub
    return null;
  }
}
