package io.resys.limaone.spi.ast.flow;

import io.resys.limaone.ast.Flow_AST.ManyTasksStatement;
import io.resys.limaone.ast.Flow_AST.NextStatement;
import lombok.Getter;

@Getter
public class ImmutableManyTasksStatement implements ManyTasksStatement {

  private final NextStatement next;
  
  public ImmutableManyTasksStatement(NextStatement next) {
    super();
    this.next = next;
  }
}