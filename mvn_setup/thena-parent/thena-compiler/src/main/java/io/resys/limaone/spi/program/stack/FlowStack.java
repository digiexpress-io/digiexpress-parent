package io.resys.limaone.spi.program.stack;

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
import io.resys.limaone.program.FlowProgram.FlowResultLog;
import io.resys.limaone.program.ImmutableFlowResultErrorLog;
import io.resys.limaone.program.ImmutableFlowResultLog;
import io.resys.limaone.program.Program.ProgramResult;
import io.resys.limaone.spi.program.FlowProgramExecutor.StatementException;
import io.resys.limaone.spi.program.assignment.Assignment;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;
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

    
    final var errorFrame = ImmutableFlowResultLog.builder()
      .id(sequence.incrementAndGet())
      .start(start)
      .stepId(stepId == null ? "unknown" : stepId)
      .isReturnsCollection(false)
      .end(LocalDateTime.now())
      .addErrors(ImmutableFlowResultErrorLog.builder().id("error").msg(errorMsg.toString()).build())
      .addErrors(ImmutableFlowResultErrorLog.builder().id("trace").msg(traceBuilder.toString()).build())
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
        .returns(match.getOutputs())

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
}
