package io.resys.limaone.program;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;



public interface FlowProgram extends Program {
  
  String getStartStepId();
  Map<String, FlowProgramStep> getSteps();
  FlowExecutor run(ProgramInput input, Runtime runtime);
  
  interface FlowExecutor {
    @Nullable
    FlowResultLog andGetTask(String task);
    FlowResult andGetBody();
  }
  

  @Value.Immutable
  interface FlowProgramStep extends Serializable {
    String getId();
    FlowProgramStepPointer getPointer();
    @Nullable
    FlowProgramStepBody getBody();
  }
  
  @Value.Immutable
  interface FlowProgramStepBody extends Serializable {
    String getRef();
    FlowProgramStepRefType getRefType();
    Map<String, String> getInputMapping();
    Boolean getCollection();
  }
  interface FlowProgramStepPointer extends Serializable {
    FlowProgramStepPointerType getType();
  }
  @Value.Immutable
  interface FlowProgramStepEndPointer extends FlowProgramStepPointer {
  }
  @Value.Immutable
  interface FlowProgramStepThenPointer extends FlowProgramStepPointer {
    String getStepId();
  }
  @Value.Immutable
  interface FlowProgramStepWhenThenPointer extends FlowProgramStepPointer {
    List<FlowProgramStepConditionalThenPointer> getConditions();
  }
  @Value.Immutable  
  interface FlowProgramStepConditionalThenPointer {
    @Nullable
    ExpressionProgram getExpression();
    String getStepId();
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
    @Nullable
    Serializable getReturnsValue();
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
