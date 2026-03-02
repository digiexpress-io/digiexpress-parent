package io.resys.limaone.spi.ast.flow;

import io.resys.limaone.ast.Flow_AST.MappingStatement;
import io.resys.limaone.ast.Flow_AST.ReturnsStatement;
import lombok.Getter;

@Getter
public class ImmutableReturnsStatement implements ReturnsStatement {

  private final boolean collection;
  private final MappingStatement mapping;
  private final String taskId;
  
  public ImmutableReturnsStatement(boolean collection, MappingStatement mapping, String taskId) {
    super();
    this.collection = collection;
    this.mapping = mapping;
    this.taskId = taskId;
  }
}