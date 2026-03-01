package io.resys.limaone.spi.ast.flow;

import io.resys.limaone.ast.Flow_AST.EmptyBodyStatement;
import lombok.Getter;

@Getter
public class ImmutableEmptyBodyStatement implements EmptyBodyStatement {

  public ImmutableEmptyBodyStatement() {
    super();
  }
}