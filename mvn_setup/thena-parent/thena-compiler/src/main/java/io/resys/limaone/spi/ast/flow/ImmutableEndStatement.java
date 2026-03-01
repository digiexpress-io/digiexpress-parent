package io.resys.limaone.spi.ast.flow;

import io.resys.limaone.ast.Flow_AST.EndStatement;


public class ImmutableEndStatement implements EndStatement {

  private static final ImmutableEndStatement INSTANCE = new ImmutableEndStatement();
  
  private ImmutableEndStatement() {
    super();
  }
  
  public static ImmutableEndStatement getInstance() {
    return INSTANCE;
  }
}