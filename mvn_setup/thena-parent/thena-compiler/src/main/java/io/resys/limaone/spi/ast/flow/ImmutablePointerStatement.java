package io.resys.limaone.spi.ast.flow;

import io.resys.limaone.ast.Flow_AST.OneTaskStatement;
import io.resys.limaone.ast.Flow_AST.PointerStatement;
import lombok.Getter;

@Getter
public class ImmutablePointerStatement implements PointerStatement {

  private final OneTaskStatement task;
  
  public ImmutablePointerStatement(OneTaskStatement task) {
    super();
    this.task = task;
  }
}