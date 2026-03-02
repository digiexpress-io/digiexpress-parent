package io.resys.limaone.spi.ast.flow;

import java.util.Collections;
import java.util.Map;

import io.resys.limaone.ast.Flow_AST.ManyTasksStatement;
import io.resys.limaone.ast.Flow_AST.NextStatement;
import io.resys.limaone.ast.Flow_AST.OneTaskStatement;
import lombok.Getter;

@Getter
public class ImmutableManyTasksStatement implements ManyTasksStatement {

  private final NextStatement next;
  private final Map<String, OneTaskStatement> tasks;
  
  public ImmutableManyTasksStatement(NextStatement next, Map<String, OneTaskStatement> tasks) {
    super();
    this.next = next;
    this.tasks = Collections.unmodifiableMap(tasks);
  }
}