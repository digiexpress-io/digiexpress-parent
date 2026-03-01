package io.resys.limaone.spi.ast.flow;

import java.util.Collections;
import java.util.List;

import io.resys.limaone.ast.Flow_AST.InputsStatement;
import io.resys.limaone.ast.Flow_AST.ManyTasksStatement;
import io.resys.limaone.model.Parameter;
import lombok.Getter;

@Getter
public class ImmutableInputsStatement implements InputsStatement {

  private final List<Parameter> parameters;
  private final ManyTasksStatement next;
  
  public ImmutableInputsStatement(List<Parameter> parameters, ManyTasksStatement next) {
    super();
    this.parameters = Collections.unmodifiableList(parameters);
    this.next = next;
  }
}
