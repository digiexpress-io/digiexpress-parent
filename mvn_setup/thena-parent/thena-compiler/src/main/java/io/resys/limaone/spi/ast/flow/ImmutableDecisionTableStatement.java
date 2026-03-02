package io.resys.limaone.spi.ast.flow;

import io.resys.limaone.ast.Flow_AST.DecisionTableStatement;
import io.resys.limaone.ast.Flow_AST.MappingStatement;
import lombok.Getter;

@Getter
public class ImmutableDecisionTableStatement implements DecisionTableStatement {

  private final boolean collection;
  private final MappingStatement mapping;
  private final String decisionTableName;
  private final String taskId;
  
  public ImmutableDecisionTableStatement(String decisionTableName, boolean collection, MappingStatement mapping, String taskId) {
    super();
    this.collection = collection;
    this.mapping = mapping;
    this.decisionTableName = decisionTableName;
    this.taskId = taskId;
  }
}