package io.resys.limaone.spi;

import io.resys.limaone.spi.expression.ExpressionProgramFactory;

public class LocalCache {
  public static void flushAll() {
    ExpressionProgramFactory.flushAll();
  }
}
