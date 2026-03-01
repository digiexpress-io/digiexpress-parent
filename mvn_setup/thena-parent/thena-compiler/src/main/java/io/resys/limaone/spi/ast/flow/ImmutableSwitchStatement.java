package io.resys.limaone.spi.ast.flow;

import java.util.Collections;
import java.util.List;

import io.resys.limaone.ast.Flow_AST.CaseStatement;
import io.resys.limaone.ast.Flow_AST.SwitchStatement;
import lombok.Getter;

@Getter
public class ImmutableSwitchStatement implements SwitchStatement {

  private final List<CaseStatement> cases;
  
  public ImmutableSwitchStatement(List<CaseStatement> cases) {
    super();
    this.cases = Collections.unmodifiableList(cases);
  }
}