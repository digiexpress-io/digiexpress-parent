package io.resys.limaone.spi.program.stack;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.commons.lang3.exception.ExceptionUtils;

import io.resys.limaone.ast.Flow_AST.BodyStatement;
import io.resys.limaone.ast.Flow_AST.StatementType;
import io.resys.limaone.program.FlowProgram;
import io.resys.limaone.program.FlowProgram.FlowExecutionStatus;
import io.resys.limaone.program.FlowProgram.FlowResultErrorLog;
import io.resys.limaone.program.FlowProgram.FlowResultLog;
import io.resys.limaone.program.ImmutableFlowResultErrorLog;
import io.resys.limaone.program.Program.ProgramResult;
import io.resys.limaone.spi.program.FlowProgramExecutor.StatementException;
import io.resys.limaone.spi.program.assignment.Assignment;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class FlowStack {
  private final AtomicInteger sequence = new AtomicInteger(0);
  private final LocalDateTime start = LocalDateTime.now();
  private final Map<String, StackFrame> frames = new HashMap<>();
  private final StringBuilder shortHistory = new StringBuilder();
  private final List<FlowResultLog> errorFrames = new ArrayList<>();
  private String lastStepId;

  
  public FlowStackResult close() {

    final List<FlowResultLog> allLogs = Stream.concat(
          frames.values().stream().flatMap(e -> mapToFlowResultLog(e).stream()), 
          errorFrames.stream())
        .sorted((a, b) -> Integer.compare(a.getId(), b.getId()))
        .toList();
    
    
    final List<FlowResultLog> lastLogs = allLogs.stream()
        .filter(log -> log.getStepId().equals(lastStepId))
        .toList();
    
    final boolean isReturnsCollection = lastLogs != null && lastLogs.size() > 1;
    final Map<String, Serializable> returnProps;
    if(isReturnsCollection) {
      returnProps = Assignment.toArrayMap(lastLogs.stream().map(e -> e.getReturns()));
    } else {
      returnProps = lastLogs.stream().findFirst().map(e -> e.getReturns()).orElse(Collections.emptyMap());
    }
    return new FlowStackResult(
        allLogs, 
        lastLogs, 
        shortHistory.toString().trim(), 
        lastStepId, 
        isReturnsCollection, 
        returnProps);
  }
  
  public void newFrame(Exception exception) {
    log.error(exception.getMessage(), exception);
    
    final var rootCause = ExceptionUtils.getRootCause(exception);
    final var rootMsg = ExceptionUtils.getRootCauseStackTrace(rootCause);
    
    final var traceBuilder = new StringBuilder();
    for(final var trace : rootMsg) {
      if(trace.contains("resys")) {
        traceBuilder.append(trace);
      }
    }
    
    final var errorMsg = new StringBuilder("Message: ").append(exception.getMessage() == null ? "-" : exception.getMessage());
    var stepId = lastStepId;
    if(exception instanceof StatementException) {
      final StatementException ex = (StatementException) exception;
      errorMsg.append(System.lineSeparator())
        .append(" props: ").append(JsonObject.mapFrom(ex.getProps()).encodePrettily()).append(System.lineSeparator())
        .append(" statement: ").append(JsonObject.mapFrom(ex.getStatement()).encodePrettily());
      
      final var stment = ex.getStatement();
      if(stment instanceof BodyStatement) {
        final var body = (BodyStatement) stment;
        stepId = body.getTaskId();
      }
    }

    final var errors = new ArrayList<FlowResultErrorLog>();
    errors.add(ImmutableFlowResultErrorLog.builder().id("error").msg(errorMsg.toString()).build());
    errors.add(ImmutableFlowResultErrorLog.builder().id("trace").msg(traceBuilder.toString()).build());
    
    final var errorFrame = ImmutableFlowResultLog.builder()
      .id(sequence.incrementAndGet())
      .start(start)
      .stepId(stepId == null ? "unknown" : stepId)
      .isReturnsCollection(false)
      .end(LocalDateTime.now())
      .errors(Collections.unmodifiableList(errors))
      .status(FlowExecutionStatus.ERROR)
      .build();
    
    logToHistory(errorFrame.getStepId(), Optional.empty());
    errorFrames.add(errorFrame);
  }
  
  public StackFrame newFrame(BodyStatement statement, Map<String, Serializable> inputs, LocalDateTime startedAt) {
    return newFrame(statement, inputs, null, startedAt);

  }
  public StackFrame newFrame(BodyStatement statement, Map<String, Serializable> inputs, @Nullable ProgramResult result, LocalDateTime startedAt) {
    logToHistory(statement.getTaskId(), Optional.of(statement));
    final var wrapper = frames.computeIfAbsent(statement.getTaskId(), (taskId) -> StackFrame.of(sequence.incrementAndGet(), statement));
    if(result == null) {
      wrapper.add(inputs, startedAt);  
    } else {
      wrapper.add(inputs, result, startedAt);
    }
    return wrapper;
  }
  
  private List<FlowResultLog> mapToFlowResultLog(StackFrame envlope) {
    return envlope.getMatches().stream().map(match -> {
      
      final FlowResultLog log = ImmutableFlowResultLog.builder()
        .id(envlope.getId())
        .stepId(envlope.getStatement().getTaskId())
        .start(match.getStartedAt())
        .end(match.getEndedAt())
        .status(FlowProgram.FlowExecutionStatus.COMPLETED)
        .isReturnsCollection(envlope.getStatement().isCollection())
        
        .accepts(match.getInputs())
        .returns(Collections.unmodifiableMap(match.getOutputs()))

        .returnsValue(match.getRaw().orElse(null))
        .cost(match.getCost())
        .build();
      
      return log;
    }).toList(); 
  }
  
  private String getLogIndent() {
    final var previous = Optional.ofNullable(frames.get(lastStepId));
    if(previous.isEmpty()) {
      return "";
    }
    StringBuilder result = new StringBuilder();
    for(int index = 0; index <= previous.get().getId(); index++) {
      result.append("  ");
    }
    return result.toString();
  }
  private void logToHistory(String stepId, Optional<BodyStatement> env) {
    final var statement = env.map(e -> e.getType()).orElse(null);
    
    if(shortHistory.length() > 0) {
      shortHistory.append(" -> ");
    }
    if(statement == StatementType.BODY_SWITCH) {
      
    } else if(stepId.equals(lastStepId)) {
      shortHistory.append("(loop)");
      shortHistory.append(System.lineSeparator() + getLogIndent());
    } else if(frames.containsKey(stepId)) {
      shortHistory.append("(recursion)");
      shortHistory.append(System.lineSeparator() + getLogIndent());
    }
    shortHistory.append(stepId);
    lastStepId = stepId;
  }
  
  
  @Value
  public static class FlowStackResult {
    List<FlowResultLog> logs;
    List<FlowResultLog> lastLogs;
    String shortHistory;
    String lastStepId;
    boolean isReturnsCollection;
    Map<String, Serializable> returns;
  }
  
  @Value @Builder
  public static class ImmutableFlowResultLog implements FlowResultLog {
    private static final long serialVersionUID = -4056844608187732484L;
    
    Integer id;
    String stepId;
    LocalDateTime start;
    LocalDateTime end;
    
    @Default
    List<FlowResultErrorLog> errors = Collections.emptyList();
    FlowExecutionStatus status;
    boolean isReturnsCollection;
    
    @Default
    Map<String, Serializable> accepts = Collections.emptyMap();
    
    @Default
    Map<String, Serializable> returns = Collections.emptyMap();
    
    @Nullable Serializable returnsValue;
    @Nullable Long cost; // cost in millis
  } 
}
