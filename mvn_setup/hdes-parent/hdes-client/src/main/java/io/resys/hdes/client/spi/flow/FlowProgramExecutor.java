package io.resys.hdes.client.spi.flow;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.resys.hdes.client.api.HdesClient.HdesTypesMapper;
import io.resys.hdes.client.api.exceptions.ProgramException;
import io.resys.hdes.client.api.programs.FlowProgram;
import io.resys.hdes.client.api.programs.FlowProgram.FlowExecutionStatus;
import io.resys.hdes.client.api.programs.FlowProgram.FlowProgramStep;
import io.resys.hdes.client.api.programs.FlowProgram.FlowProgramStepThenPointer;
import io.resys.hdes.client.api.programs.FlowProgram.FlowProgramStepWhenThenPointer;
import io.resys.hdes.client.api.programs.FlowProgram.FlowResult;
import io.resys.hdes.client.api.programs.FlowProgram.FlowResultErrorLog;
import io.resys.hdes.client.api.programs.FlowProgram.FlowResultLog;
import io.resys.hdes.client.api.programs.ImmutableFlowExecutionLog;
import io.resys.hdes.client.api.programs.ImmutableFlowResult;
import io.resys.hdes.client.api.programs.ImmutableFlowResultErrorLog;
import io.resys.hdes.client.api.programs.ImmutableFlowResultLog;
import io.resys.hdes.client.api.programs.Program.ProgramContext;
import io.resys.hdes.client.api.programs.Program.ProgramContextNamedValue;
import io.resys.hdes.client.spi.ImmutableProgramContext;
import io.resys.hdes.client.spi.decision.DecisionProgramExecutor;
import io.resys.hdes.client.spi.expression.OperationFlowContext.FlowTaskExpressionContext;
import io.resys.hdes.client.spi.groovy.ServiceProgramExecutor;
import io.vertx.core.json.JsonObject;

public class FlowProgramExecutor {
  private static final Logger LOGGER = LoggerFactory.getLogger(FlowProgramExecutor.class);
  private final HdesTypesMapper factory;
  private final FlowProgram program;
  private final ProgramContext context;
  private final Map<String, Serializable> accepted = new HashMap<>();
  private final Map<String, FlowResultLog> stepLogs = new HashMap<>();
  private final LocalDateTime start = LocalDateTime.now();
  private final StringBuilder shortHistory = new StringBuilder();
  
  public FlowProgramExecutor(FlowProgram program, ProgramContext context, HdesTypesMapper factory) {
    super();
    this.program = program;
    this.context = context;
    this.factory = factory;
  }

  public FlowResult run() {
    
    accepted.putAll(visitAcceptedDef(program, context));
    
    List<FlowResultLog> last;
    FlowExecutionStatus status = FlowExecutionStatus.COMPLETED;
    try {
      last = visitStep(program.getStartStepId());
    } catch(StepException e) {
      LOGGER.error(e.getMessage(), e);
      status = FlowExecutionStatus.ERROR;
      last = Arrays.asList(visitException(e));
    }
    
    final List<FlowResultLog> logs = new ArrayList<>(stepLogs.values());
    if(last != null && status == FlowExecutionStatus.ERROR) {
      logs.addAll(last);
    }
    Collections.sort(logs, (o1, o2) -> Integer.compare(o1.getId(), o2.getId()));
    
    final var isArray = last != null && last.size() > 1;
  
    return ImmutableFlowResult.builder()
        .logs(logs)
        .lastLogs(last)
        .stepId(last.iterator().next().getStepId())
        .status(status)
        .accepts(accepted)
        .isReturnsCollection(isArray)
        .returns(isArray ? Map.<String, Serializable>of("", mergeResults(last)) : last.iterator().next().getReturns())
        .shortHistory(shortHistory.toString())
        .build();
  }
  
  private ArrayList<Map<String, Serializable>> mergeResults(List<FlowResultLog> last) {
    return new ArrayList<>(last.stream().map(e -> e.getReturns()).toList());
  }
  
  private FlowResultLog visitException(Exception e) {
    final List<FlowResultLog> logs = new ArrayList<>(stepLogs.values());
    Collections.sort(logs, (o1, o2) -> Integer.compare(o1.getId(), o2.getId()));
    
    
    final var rootCause = ExceptionUtils.getRootCause(e);
    final var rootMsg = ExceptionUtils.getRootCauseStackTrace(rootCause);
    final var messages = new ArrayList<FlowResultErrorLog>();
    
    final var traceBuilder = new StringBuilder();
    for(final var trace : rootMsg) {
      if(trace.contains("resys")) {
        traceBuilder.append(trace);
      }
    }
    
    messages.add(ImmutableFlowResultErrorLog.builder().id("error").msg(e.getMessage() == null ? "" : e.getMessage()).build());
    messages.add(ImmutableFlowResultErrorLog.builder().id("trace").msg(traceBuilder.toString()).build());
    
    final FlowResultLog lastLog;
    if(logs.isEmpty()) {
      lastLog = ImmutableFlowResultLog.builder()
          .id(0)
          .stepId("start")
          .start(start)
          .end(LocalDateTime.now())
          .status(FlowExecutionStatus.ERROR)
          .addAllErrors(messages)
          .isReturnsCollection(false)
          .build();        
    } else {
      lastLog = ImmutableFlowResultLog.builder()
          .from(logs.get(logs.size() - 1))
          .addAllErrors(messages)
          .build();
    }
    return lastLog;
  }
  
  private FlowResultLog visitStepLog(FlowResultLog log) {
    visitShortHistory(log);
    this.stepLogs.put(log.getStepId(), log);
    return log;
  }

  
  private List<FlowResultLog> visitStep(String stepId) {
    final var step = program.getSteps().get(stepId);    
    final var log = visitBody(step);
    
    switch (step.getPointer().getType()) {
    case THEN: return visitThenPointer(step);
    case SWITCH: return visitSwitchPointer(step);
    case END: return Arrays.asList(log);
    default: throw new ProgramException("Step pointer: '" + step.getPointer().getType() + "' not implemented!");
    }
  }
  
  @SuppressWarnings({"unchecked"})
  private FlowResultLog visitBody(FlowProgramStep step) {
    final var start = LocalDateTime.now();
    if(step.getBody() == null) {
      return visitStepLog(
          ImmutableFlowResultLog.builder()
          .id(this.stepLogs.size() + 1)
          .stepId(step.getId())
          .start(start)
          .end(LocalDateTime.now())
          .status(FlowExecutionStatus.COMPLETED)
          .isReturnsCollection(false)
          .build());
    }
    
    final var result = new ArrayList<FlowResultLog>();
    final var inputs = visitInputMapping(step);
    for(final var input : inputs) {
      result.add(visitSingleInput(step, input));
    }
  
    if(result.size() == 1) {
      return result.iterator().next();
    } else if(result.isEmpty()) {
      return visitStepLog(
          ImmutableFlowResultLog.builder()
          .id(this.stepLogs.size() + 1)
          .stepId(step.getId())
          .start(start)
          .end(LocalDateTime.now())
          .isReturnsCollection(Boolean.TRUE.equals(step.getBody().getCollection()))
          .status(FlowExecutionStatus.COMPLETED)
          .build()); 
    }
    
    // This should be only valid for DT return types that have multiple matches
    final var merged = ImmutableFlowResultLog.builder()
      .id(this.stepLogs.size() + 1)
      .stepId(step.getId())
      .isReturnsCollection(Boolean.TRUE.equals(step.getBody().getCollection()))
      .start(start)
      .status(FlowExecutionStatus.COMPLETED);
    
    var index = 0;
    final var returnValues = new HashMap<String, Serializable>();
    final var returns = new HashMap<String, Serializable>();
    for(final var entry : result) {
      
  
      merge(returnValues, (Map<String, Serializable>) entry.getReturnsValue());    
      merge(returns, toNonNull(entry.getReturns()));
      merged.putAccepts(String.valueOf(index++), (Serializable) entry.getAccepts());
      
      if(entry.getStatus() == FlowExecutionStatus.ERROR) {
        merged.status(entry.getStatus());
        break;
      }
    }
    
    return merged.end(LocalDateTime.now()).returns(returns).returnsValue(returnValues).build();
  }
  
  // TODO:: refactor
  @SuppressWarnings({"unchecked", "rawtypes"})
  private void merge(Map<String, Serializable> mergeTo, Map<String, Serializable> mergeFrom) {
    for(final var entry : mergeFrom.entrySet()) {
      final var old = mergeTo.get(entry.getKey());
      if(old == null && entry.getValue() instanceof Collection) {
        mergeTo.put(entry.getKey(), new ArrayList<>((Collection) entry.getValue()));
      } else if(old instanceof Collection) {
        final var newEntries = ((Collection) entry.getValue()).stream()
            .filter(e -> !((Collection) old).contains(e))
            .toList();
        
        ((Collection) old).addAll(newEntries);
      } else {
        throw new StepException("Don't know how to merge array result: " + JsonObject.mapFrom(entry), null);
      }
    }
  }
  
  private FlowResultLog visitSingleInput(FlowProgramStep step, Map<String, Serializable> inputs) {
    
    switch (step.getBody().getRefType()) {
      case RETURNS: {
        try {
          final var outputs = visitReturnMapping(step);
          return visitStepLog(ImmutableFlowResultLog.builder()
              .id(this.stepLogs.size() + 1)
              .stepId(step.getId())
              .start(start)
              .end(LocalDateTime.now())
              .status(FlowExecutionStatus.COMPLETED)
              .isReturnsCollection(Boolean.TRUE.equals(step.getBody().getCollection()))
              .accepts(inputs)
              .returns(outputs)
              .returnsValue((Serializable) outputs)
              .build());
        } catch(Exception e) {
          visitStepLog(ImmutableFlowResultLog.builder()
              .id(this.stepLogs.size() + 1)
              .stepId(step.getId())
              .start(start)
              .end(LocalDateTime.now())
              .isReturnsCollection(Boolean.TRUE.equals(step.getBody().getCollection()))
              .status(FlowExecutionStatus.ERROR)
              .accepts(inputs)
              .build());
          throw new StepException(e.getMessage(), e);
        }
      }
      case DT: {
        final var program = context.getDecision(step.getBody().getRef());
        try {
          final var result = DecisionProgramExecutor.run(program, ImmutableProgramContext.from(context).map(inputs).build());
          final var outputs = step.getBody().getCollection() ? 
              Map.of("", (Serializable) DecisionProgramExecutor.find(result)): 
              DecisionProgramExecutor.get(result);
            
          return visitStepLog(ImmutableFlowResultLog.builder()
              .id(this.stepLogs.size() + 1)
              .stepId(step.getId())
              .start(start)
              .end(LocalDateTime.now())
              .status(FlowExecutionStatus.COMPLETED)
              .accepts(inputs)
              .isReturnsCollection(Boolean.TRUE.equals(step.getBody().getCollection()))
              .returns(toNonNull(outputs))
              .returnsValue((Serializable) outputs)
              .build());
        } catch(Exception e) {
          visitStepLog(ImmutableFlowResultLog.builder()
              .id(this.stepLogs.size() + 1)
              .stepId(step.getId())
              .start(start)
              .end(LocalDateTime.now())
              .isReturnsCollection(Boolean.TRUE.equals(step.getBody().getCollection()))
              .status(FlowExecutionStatus.ERROR)
              .accepts(inputs)
              .build());
          throw new StepException(e.getMessage(), e);
        }
      }
      case SERVICE: {
        final var program = context.getService(step.getBody().getRef());
        final var log = ImmutableFlowExecutionLog.builder().putAllSteps(stepLogs).putAllAccepts(inputs).build();
        try { 
          final var result = ServiceProgramExecutor.run(program, ImmutableProgramContext.from(context).log(log).map(inputs).build());
          final var outputs = factory.toMap(result.getValue());
          return visitStepLog(ImmutableFlowResultLog.builder()
              .id(this.stepLogs.size() + 1)
              .stepId(step.getId())
              .start(start)
              .end(LocalDateTime.now())
              .status(FlowExecutionStatus.COMPLETED)
              .accepts(inputs)
              .returnsValue((Serializable) result.getValue())
              .isReturnsCollection(Boolean.TRUE.equals(step.getBody().getCollection()))
              .returns(toNonNull(outputs))
              .build());
        } catch(Exception e) {
          visitStepLog(ImmutableFlowResultLog.builder()
              .id(this.stepLogs.size() + 1)
              .stepId(step.getId())
              .start(start)
              .end(LocalDateTime.now())
              .isReturnsCollection(Boolean.TRUE.equals(step.getBody().getCollection()))
              .status(FlowExecutionStatus.ERROR)
              .accepts(inputs)
              .build());
          throw new StepException(e.getMessage(), e);
        }
      }
      default: 
        throw new ProgramException("Flow step: '" + step.getId() + "' ref: '" + step.getBody().getRefType() + "' is not supported !");
      }
  }
  
  private List<Map<String, Serializable>> visitInputMapping(FlowProgramStep step) {

    final var inputMapping = Objects.requireNonNull(step.getBody()).getInputMapping();
    if (inputMapping.containsKey(FlowProgramBuilder.OBJECT_INPUT_FLAG)) {
      return Arrays.asList(factory.toMap(accepted.get(inputMapping.get(FlowProgramBuilder.OBJECT_INPUT_FLAG))));
    }
    
    final List<Map<String, Serializable>> allInputs = new ArrayList<>();
    for(final var mapping : new InputMappingResolver(inputMapping, (path) -> visitVariableOnPath(path)).accept()) {
      
      final Map<String, Serializable> result = new HashMap<>();
      allInputs.add(result);
      
      for(final var entry : mapping.entrySet()) {
        final String nameOnService = entry.getKey();
        
        try {
          // Flat mapping
          Serializable value;
          if(accepted.containsKey(entry.getValue())) {
            value = accepted.get(entry.getValue());
          } else {
            value = visitVariableOnPath(entry.getValue());
          }
          
          if(value != null) {
            result.put(nameOnService, value);
          }
          
        } catch(Exception e) {
          throw new ProgramException(
              "Failed to get parameter: '" + entry.getKey() + ":" + entry.getValue() + "' while mapping step: '" + step.getId() + "'" + System.lineSeparator() + 
              e.getMessage(), e);
        }
      }
    }
    return Collections.unmodifiableList(allInputs);
  }
  
  private Map<String, ? extends Serializable> visitReturnMapping(FlowProgramStep step) {
    final ArrayList<Map<String, Serializable>> returns = new ArrayList<>();
    
    final var inputMappings = step.getBody().getInputMapping();
    
    final var arrays = new InputMappingResolver(inputMappings, (path) -> visitVariableOnPath(path)).accept();
    
    
    for(final var mapping : arrays) {
      
      final Map<String, Serializable> result = new HashMap<>();
      returns.add(result);
      
      for(final var entry : mapping.entrySet()) {
        final String nameOnService = entry.getKey();
        
        try {
          // Flat mapping
          Serializable value;
          if(accepted.containsKey(entry.getValue())) {
            value = accepted.get(entry.getValue());
          } else {
            value = visitVariableOnPath(entry.getValue());
          }
          
          if(value != null) {
            result.put(nameOnService, value);
          }
          
        } catch(Exception e) {
          throw new ProgramException(
              "Failed to get parameter: '" + entry.getKey() + ":" + entry.getValue() + "' while mapping step: '" + step.getId() + "'" + System.lineSeparator() + 
              e.getMessage(), e);
        }
      }
    }

    if(Boolean.TRUE.equals(step.getBody().getCollection())) {
      return Map.of("", returns);
    }
    return returns.iterator().next();
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
          if(FlowProgramBuilder.END_STEP.getId().equals(whenThen.getStepId())) {
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
        throw new ProgramException("Flow switch: '" + step.getId() + "' does not match any expressions!");
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
  
  
  private List<Map<String, Serializable>> visitSwitchInputMapping(FlowProgramStep step) {
    final List<Map<String, Serializable>> allInputs = new ArrayList<>();    

    final var inputMappings = new HashMap<String, String>();
    for(final var whenThen : ((FlowProgramStepWhenThenPointer) step.getPointer()).getConditions()) {
      whenThen.getExpression().getConstants().forEach(e -> inputMappings.put(e, e));
    }
    
    final var arrays = new InputMappingResolver(inputMappings, (path) -> visitVariableOnPath(path)).accept();
    for(final var mapping : arrays) {
      
      final Map<String, Serializable> result = new HashMap<>();
      allInputs.add(result);
      
      for(final var entry : mapping.entrySet()) {
        final String nameOnService = entry.getKey();
        
        try {
          // Flat mapping
          Serializable value;
          if(accepted.containsKey(entry.getValue())) {
            value = accepted.get(entry.getValue());
          } else {
            value = visitVariableOnPath(entry.getValue());
          }
          
          if(value != null) {
            result.put(nameOnService, value);
          }
          
        } catch(Exception e) {
          throw new ProgramException(
              "Failed to get parameter: '" + entry.getKey() + ":" + entry.getValue() + "' while mapping step: '" + step.getId() + "'" + System.lineSeparator() + 
              e.getMessage(), e);
        }
      }
    }

    return Collections.unmodifiableList(allInputs);
  }
  
  
  
  private Map<String, Serializable> visitAcceptedDef(FlowProgram program, ProgramContext context) {
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

  @SuppressWarnings("unchecked")
  private Serializable visitVariableOnPath(String name) {

    String[] paths = name.split("\\.");
    if(paths.length == 0) {
      return null;
    }

    Map<String, Serializable> prev = null;
    StringBuilder fullName = new StringBuilder();
    int index = 0;
    for(String path : paths) {
      index++;
      final var isLast = index == paths.length;
      if(fullName.length() > 0) {
        fullName.append(".");
      }
      fullName.append(path);
      
      
      // first parameter
      if(prev == null) {
        // resolve based on accepted
        if(accepted.containsKey(path)) {
          Object target = accepted.get(path);
          if(Map.class.isAssignableFrom(target.getClass())) {
            prev = (Map<String, Serializable>) target;
          } else if(!isLast) {
            prev = (Map<String, Serializable>) factory.toMap(target);
          } else {
            return (Serializable) target;
          }
          continue;
        }
      
        // resolve from executed steps
        // TODO: refactor this mess
        if(stepLogs.containsKey(path)) {
          FlowResultLog target = stepLogs.get(path);
          
          final var returnVariables = new HashMap<String, Serializable>(target.getReturns());
          if(target.isReturnsCollection()) {

            if(returnVariables.containsKey("")) {
              final var readonlyCollectionType = returnVariables.get(""); 
              if(readonlyCollectionType instanceof Collection) {
                final var mutableCollectionType = new ArrayList<>();
                
                for(final var readonlyEntry : (Collection<?>) readonlyCollectionType) {
                  final var mutableEntry = new HashMap<>((Map<String, Serializable>) readonlyEntry);
                  target.getAccepts().entrySet().forEach(e -> mutableEntry.put("_" + e.getKey(), e.getValue()));      
                  mutableCollectionType.add(mutableEntry);
                }
                
                returnVariables.put("", mutableCollectionType);
              }
            }
          } else {
            target.getAccepts().entrySet().forEach(e -> returnVariables.put("_" + e.getKey(), e.getValue()));  
          }
          
          
          prev = returnVariables;
          if(isLast) {
            return (Serializable) prev;
          }
          continue; 
        }
        
        // root context
        final ProgramContextNamedValue contextValue = context.getValueWithMeta(path);
        if(contextValue.getFound()) {
          Serializable result = contextValue.getValue();
          if(isLast) {
            return result;
          } else if(contextValue instanceof Map) {
            prev = (Map<String, Serializable>) result;
          }
        }
      }
      
      
      if(prev == null) {
        // if parameter isn't found, return the provided value, or null if the value is null
        if (path == null || path.equals("null")) {
          return null;
        }
        return path;
      }
      
      
      if(isArray(path)) {
        final var arrayIndex = getArrayIndex(path);
        final var array = InputMappingResolver.getCollectionType(prev);
        
        if(array.size() < arrayIndex) {
          return null;  
        }
        
        try {
          prev = (Map<String, Serializable>) array.get(arrayIndex);
          continue; 
        } catch(Exception e) {
          
          //throw new ProgramException("Can't find parameter with name: '" + name + "' from: '" + fullName + "'!");
          return null;  
        }
        
      }
      

      if(prev.containsKey(path) && isLast) {
        return prev.get(path);
      } else if(prev.containsKey(path) && !isLast) {
        prev = (Map<String, Serializable>) prev.get(path);
      } else {
        //throw new ProgramException("Can't find parameter with name: '" + name + "' from: '" + fullName + "'!");
        return null;
      }
    }
    return null;
  }
  private boolean isArray(String path) {
    try {
      Integer.parseInt(path.substring(1, path.length() -1));
      return true;
    } catch(Exception e) {
      return false;
    }
  }
  private int getArrayIndex(String path) {
    return Integer.parseInt(path.substring(1, path.length() -1));
  }
  public void visitShortHistory(FlowResultLog log) {
    if(shortHistory.length() > 0) {
      shortHistory.append(" -> ");
    }
    
    if(stepLogs.containsKey(log.getStepId())) {
      shortHistory.append("(recursion) ");
      shortHistory.append(System.lineSeparator() + getIndent(log));
    }
    shortHistory.append(log.getStepId());
  }
  
  private String getIndent(FlowResultLog previous) {
    StringBuilder result = new StringBuilder();
    for(int index = 0; index <= previous.getId(); index++) {
      result.append("  ");
    }
    return result.toString();
  }
  
  private Map<String, Serializable> toNonNull(Map<String, Serializable> input) {
    Map<String, Serializable> result = new HashMap<>();
    input.entrySet().stream().filter(e -> e.getValue() != null && e.getKey() != null)
    .forEach(e -> result.put(e.getKey(), e.getValue()));
    
    return result;
  }
  
  
  private static class StepException extends RuntimeException {
    private static final long serialVersionUID = 2352180316876534777L;

    public StepException(String message, Throwable cause) {
      super(message, cause);
    }
    
  }
}
