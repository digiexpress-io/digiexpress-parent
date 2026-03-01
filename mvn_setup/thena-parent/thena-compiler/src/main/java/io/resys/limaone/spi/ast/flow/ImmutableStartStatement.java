package io.resys.limaone.spi.ast.flow;

import io.resys.limaone.ast.Flow_AST.OneTaskStatement;
import io.resys.limaone.ast.Flow_AST.StartStatement;
import lombok.Getter;

@Getter
public class ImmutableStartStatement implements StartStatement {

  private final OneTaskStatement firstTask;
  
  public ImmutableStartStatement(OneTaskStatement firstTask) {
    super();
    this.firstTask = firstTask;
  }
}