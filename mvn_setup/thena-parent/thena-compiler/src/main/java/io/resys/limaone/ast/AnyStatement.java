package io.resys.limaone.ast;

import java.util.List;
import java.util.Map;

import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.ExpressionProgram;
import jakarta.annotation.Nullable;

public interface AnyStatement {
  
  //root marker
  StatementType getType();

  // markers
  interface BodyStatement {}
  interface NextStatement {}


  interface InputsStatement {
    List<Parameter> getParameters();
    default StatementType getType() { return StatementType.FLOW_INPUTS; }
  }

  interface ManyTasksStatement extends AnyStatement {
    NextStatement getNext();
    default StatementType getType() { return StatementType.FLOW_TASKS; }
  }

  interface OneTaskStatement extends AnyStatement {
    String getId();
    BodyStatement getBody();
    NextStatement getThen();
    default StatementType getType() { return StatementType.FLOW_TASK; }
  }

  interface EmptyBodyStatement extends BodyStatement, AnyStatement {
    default StatementType getType() { return StatementType.BODY_EMPTY; }
  }  
  interface DecisionTableStatement extends BodyStatement, AnyStatement {
    boolean isCollection();
    MappingStatement getMapping();
    default StatementType getType() { return StatementType.BODY_DECISION_TABLE; }
  }
  interface FlowTaskStatement extends BodyStatement, AnyStatement {
    boolean isCollection();
    MappingStatement getMapping();
    default StatementType getType() { return StatementType.BODY_FLOW_TASK; }
  }
  interface ReturnsStatement extends BodyStatement, AnyStatement {
    boolean isCollection();
    MappingStatement getMapping();
    default StatementType getType() { return StatementType.BODY_RETURNS; }
  }


  interface SwitchStatement extends BodyStatement, AnyStatement {
    List<CaseStatement> getCases();
    default StatementType getType() { return StatementType.BODY_SWITCH; }
  }
  interface CaseStatement extends AnyStatement {
    @Nullable ExpressionProgram getWhen();
    NextStatement getThen();    
    default StatementType getType() { return StatementType.BODY_SWITCH_CASE; }
  }

  interface StartStatement extends AnyStatement, NextStatement {
    OneTaskStatement getFirstTask();
    default StatementType getType() { return StatementType.NEXT_IS_START; }
  }

  interface EndStatement extends AnyStatement, NextStatement {
    default StatementType getType() { return StatementType.NEXT_IS_END; }
  }

  interface PointerStatement extends AnyStatement, NextStatement {
    OneTaskStatement getTask();

    default StatementType getType() { return StatementType.NEXT_IS_POINTER; }
  }
  interface MappingStatement extends AnyStatement {
    //  to------from
    Map<String, String> getAssignments();

    default StatementType getType() { return StatementType.MAPPING; }
  }


  enum StatementType {
    FLOW_INPUTS,
    FLOW_TASKS,
    FLOW_TASK,

    BODY_EMPTY,
    BODY_FLOW_TASK, 
    BODY_DECISION_TABLE, 
    BODY_SWITCH,
    BODY_SWITCH_CASE,
    BODY_RETURNS,

    MAPPING,

    NEXT_IS_START, 
    NEXT_IS_END, 
    NEXT_IS_POINTER
  }
}
