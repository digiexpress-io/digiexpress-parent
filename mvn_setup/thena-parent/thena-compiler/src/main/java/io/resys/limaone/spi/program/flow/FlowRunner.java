package io.resys.limaone.spi.program.flow;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.limaone.program.FlowProgram;
import io.resys.limaone.program.ImmutableFlowResultLog;
import io.resys.limaone.program.FlowProgram.FlowExecutionStatus;
import io.resys.limaone.program.FlowProgram.FlowProgramStep;
import io.resys.limaone.program.FlowProgram.FlowProgramStepThenPointer;
import io.resys.limaone.program.FlowProgram.FlowProgramStepWhenThenPointer;
import io.resys.limaone.program.FlowProgram.FlowResultLog;
import io.resys.limaone.spi.program.ProgramContext;
import io.resys.limaone.spi.program.ProgramException;
import io.resys.limaone.spi.program.expression.OperationFlowContext.FlowTaskExpressionContext;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FlowRunner {

  private final FlowProgram program;
  
  
  private Map<String, Serializable> visitInputStatement() {
    Map<String, Serializable> result = new HashMap<>();
    List<String> required = new ArrayList<>();
    for(final var dataType : program.getAcceptDefs()) {
      Serializable value = context.getValue(dataType);
      if(value != null) {
        result.put(dataType.getName(), value);
      }
      if(dataType.isRequired() && value == null) {
        required.add(dataType.getName());
      }
    }
    if(!required.isEmpty()) {
      throw new ProgramException("Flow can't have null inputs: " + String.join(", ", required) + "!");
    }
    
    return result;
  }

  
  private List<FlowResultLog> visitStepStatement(String stepId) {
    final var step = program.getSteps().get(stepId);    
    final var log = visitBody(step);
    
    switch (step.getPointer().getType()) {
    case THEN: return visitThenPointer(step);
    case SWITCH: return visitSwitchPointer(step);
    case END: return Arrays.asList(log);
    default: throw new ProgramException("Step pointer: '" + step.getPointer().getType() + "' not implemented!");
    }
  }
  
  
  private List<FlowResultLog> visitThenPointer(FlowProgramStep step) {
    final var stepId = ((FlowProgramStepThenPointer) step.getPointer()).getStepId();
    return visitStep(stepId);
  }
  
  

  private List<FlowResultLog> visitSwitchPointer(FlowProgramStep step) {
    final List<FlowResultLog> visited = new ArrayList<>();
    final var inputMapping = visitSwitchInputMapping(step);
    for(final var mappingEntry : inputMapping) {
      
      boolean isAtleastOneMatch = false;
      for(final var whenThen : ((FlowProgramStepWhenThenPointer) step.getPointer()).getConditions()) {
        final var expressionContext = new FlowTaskExpressionContext() {
          @Override
          public Object apply(String name) {
            if(mappingEntry.containsKey(name)) {
              return mappingEntry.get(name);
            }
            
            return mappingEntry.entrySet().stream()
              .filter(e -> e.getKey().startsWith(name + "."))
              .collect(Collectors.toMap(e -> e.getKey().substring(name.length() + 1), e -> e.getValue()));
          }
        };      
        
        if((Boolean) whenThen.getExpression().run(expressionContext).getValue()) {
          isAtleastOneMatch = true;
          //switch leads to end
          if(END_STEP.getId().equals(whenThen.getStepId())) {
            visited.add(visitStepLog(
                ImmutableFlowResultLog.builder()
                .id(this.stepLogs.size() + 1)
                .stepId(step.getId())
                .start(start)
                .end(LocalDateTime.now())
                .status(FlowExecutionStatus.COMPLETED)
                .isReturnsCollection(false)
                .build()));
              
          } else {
            visited.addAll(visitStep(whenThen.getStepId()));  
          }
          
          break;
        }     
      }
      
      if(!isAtleastOneMatch) {
        log.debug("Flow switch: '" + step.getId() + "' does not match any expressions!");
      }
    }
    
    // nothing found nowhere to route
    if(visited.isEmpty()) {
      visited.add(visitStepLog(
          ImmutableFlowResultLog.builder()
          .id(this.stepLogs.size() + 1)
          .stepId(step.getId())
          .start(start)
          .end(LocalDateTime.now())
          .status(FlowExecutionStatus.COMPLETED)
          .isReturnsCollection(false)
          .build()));
    }

    return visited;
  }
  
}
