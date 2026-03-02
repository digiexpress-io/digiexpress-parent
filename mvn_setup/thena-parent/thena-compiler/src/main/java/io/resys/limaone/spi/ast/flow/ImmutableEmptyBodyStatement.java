package io.resys.limaone.spi.ast.flow;

import io.resys.limaone.ast.Flow_AST.EmptyBodyStatement;
import lombok.Getter;

@Getter
public class ImmutableEmptyBodyStatement implements EmptyBodyStatement {
  private final String taskId;
  
  public ImmutableEmptyBodyStatement(String taskId) {
    super();
    this.taskId = taskId;
  }
}