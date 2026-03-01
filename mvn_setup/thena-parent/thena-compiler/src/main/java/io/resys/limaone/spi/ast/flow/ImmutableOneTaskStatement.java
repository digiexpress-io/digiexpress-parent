package io.resys.limaone.spi.ast.flow;

import io.resys.limaone.ast.Flow_AST.BodyStatement;
import io.resys.limaone.ast.Flow_AST.NextStatement;
import io.resys.limaone.ast.Flow_AST.OneTaskStatement;
import lombok.Getter;

@Getter
public class ImmutableOneTaskStatement implements OneTaskStatement {

  private final String id;
  private final BodyStatement body;
  private final NextStatement then;
  
  public ImmutableOneTaskStatement(String id, BodyStatement body, NextStatement then) {
    super();
    this.id = id;
    this.body = body;
    this.then = then;
  }
}