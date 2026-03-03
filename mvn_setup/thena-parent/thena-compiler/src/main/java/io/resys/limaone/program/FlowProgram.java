package io.resys.limaone.program;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.ast.Flow_AST;
import jakarta.annotation.Nullable;



public interface FlowProgram extends Program {
  
  Flow_AST getAst();
  
  FlowExecutor run(ProgramInput input, Runtime runtime);
  FlowExecutor run(Map<String, Serializable> input);
  
  interface FlowExecutor {
    @Nullable
    FlowResultLog andGetTask(String task);
    FlowResult andGetBody();
    String andEncodePrettily();
  }
  
  @Value.Immutable
  interface FlowResult extends ProgramResult {
    String getStepId();
    String getShortHistory();
    List<FlowResultLog> getLogs();
    List<FlowResultLog> getLastLogs();
    FlowExecutionStatus getStatus();
    boolean isReturnsCollection();
    Map<String, Serializable> getAccepts();
    Map<String, Serializable> getReturns();
  }

  @Value.Immutable
  interface FlowResultLog extends Serializable {
    Integer getId();
    String getStepId();
    LocalDateTime getStart();
    LocalDateTime getEnd();
    List<FlowResultErrorLog> getErrors();
    FlowExecutionStatus getStatus();
    boolean isReturnsCollection();
    
    
    Map<String, Serializable> getAccepts();
    Map<String, Serializable> getReturns();
    
    @Nullable Serializable getReturnsValue();
    @Nullable Long getCost(); // cost in millis
  }
  
  @Value.Immutable
  interface FlowResultErrorLog extends Serializable {
    String getId();
    String getMsg();
  }
  
  @JsonSerialize(as = ImmutableFlowExecutionLog.class)
  @JsonDeserialize(as = ImmutableFlowExecutionLog.class)
  @Value.Immutable
  interface FlowExecutionLog extends ProgramLog {
    Map<String, Serializable> getAccepts();
    Map<String, FlowResultLog> getSteps(); 
  }
  
  enum FlowProgramStepPointerType { SWITCH, THEN, END }
  enum FlowProgramStepRefType { SERVICE, DT, RETURNS } 
  enum FlowExecutionStatus { COMPLETED, ERROR }
}
