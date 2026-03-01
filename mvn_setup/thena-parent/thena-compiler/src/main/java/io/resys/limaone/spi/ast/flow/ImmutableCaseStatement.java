package io.resys.limaone.spi.ast.flow;

import io.resys.limaone.ast.Flow_AST.CaseStatement;
import io.resys.limaone.ast.Flow_AST.NextStatement;
import io.resys.limaone.program.ExpressionProgram;
import jakarta.annotation.Nullable;
import lombok.Getter;

@Getter
public class ImmutableCaseStatement implements CaseStatement {

  @Nullable
  private final ExpressionProgram when;
  private final NextStatement then;
  
  public ImmutableCaseStatement(@Nullable ExpressionProgram when, NextStatement then) {
    super();
    this.when = when;
    this.then = then;
  }
}