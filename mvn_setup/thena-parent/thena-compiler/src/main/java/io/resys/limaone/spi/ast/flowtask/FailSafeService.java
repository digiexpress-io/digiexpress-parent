package io.resys.limaone.spi.ast.flowtask;

import java.util.HashMap;

import io.resys.limaone.ast.FlowTask_AST.ServiceExecutorType0;

public class FailSafeService implements ServiceExecutorType0<HashMap<String, String>> {
  @Override
  public HashMap<String, String> execute() {
    return new HashMap<>();
  }
}