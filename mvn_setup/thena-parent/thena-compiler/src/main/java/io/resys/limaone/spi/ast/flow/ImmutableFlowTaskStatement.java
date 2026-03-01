package io.resys.limaone.spi.ast.flow;

import io.resys.limaone.ast.Flow_AST.FlowTaskStatement;
import io.resys.limaone.ast.Flow_AST.MappingStatement;
import lombok.Getter;

@Getter
public class ImmutableFlowTaskStatement implements FlowTaskStatement {

  private final boolean collection;
  private final MappingStatement mapping;
  private final String flowTaskName;
  
  public ImmutableFlowTaskStatement(String flowTaskName, boolean collection, MappingStatement mapping) {
    super();
    this.collection = collection;
    this.mapping = mapping;
    this.flowTaskName = flowTaskName;
  }
}