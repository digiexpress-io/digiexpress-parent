package io.resys.limaone.program;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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
  FlowExecutor run(ProgramInput input);
  FlowExecutor run(Map<String, Serializable> input);

  
  interface FlowExecutor {
    @Nullable
    FlowResultLog andGetTask(String task);
    FlowResult andGetBody();
    String andEncodePrettily();
  }
  
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
