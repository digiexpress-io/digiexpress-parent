package io.resys.limaone.spi.compiler.groovy;

import java.util.HashMap;

import io.resys.limaone.program.FlowTaskProgram.ServiceExecutorType0;

public class FailSafeService implements ServiceExecutorType0<HashMap<String, String>> {
  @Override
  public HashMap<String, String> execute() {
    return new HashMap<>();
  }
}