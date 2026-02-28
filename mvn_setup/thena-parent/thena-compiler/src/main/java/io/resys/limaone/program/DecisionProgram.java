package io.resys.limaone.program;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.immutables.value.Value;

import io.resys.limaone.ast.DecisionTable_AST;
import io.resys.limaone.model.DecisionTable.HitPolicy;
import io.resys.limaone.model.Parameter;
import jakarta.annotation.Nullable;




public interface DecisionProgram extends Program {
  List<DecisionRow> getRows();
  HitPolicy getHitPolicy();
  
  DecisionExecutor run(Map<String, ?> input);
  DecisionExecutor run(ProgramInput input, Runtime runtime);
  
  interface DecisionExecutor {
    DecisionExecutor callback(Consumer<DecisionTable_AST> callback);
    Map<String, Serializable> andGet();
    List<Map<String, Serializable>> andFind();
    DecisionResult andGetBody();
  }
  
  @Value.Immutable
  interface DecisionRow extends Serializable {
    int getOrder();
    
    List<DecisionRowAccepts> getAccepts();
    List<DecisionRowReturns> getReturns();
  }
  @Value.Immutable
  interface DecisionRowAccepts extends Serializable {
    Parameter getKey();
    ExpressionProgram getExpression();
  }
  @Value.Immutable
  interface DecisionRowReturns extends Serializable {
    Parameter getKey();
    Serializable getValue();
  }

  @Value.Immutable
  interface DecisionResult extends ProgramResult {
    List<DecisionLog> getRejections();
    List<DecisionLog> getMatches();
  }
  @Value.Immutable
  interface DecisionLog extends Serializable {
    Boolean getMatch();
    Integer getOrder();
    List<DecisionLogEntry> getAccepts();
    List<DecisionLogEntry> getReturns();
  }
  @Value.Immutable
  interface DecisionLogEntry extends Serializable {
    Boolean getMatch();
    Parameter getHeaderType();
    String getExpression();
    @Nullable Serializable getUsedValue();
  }
}
