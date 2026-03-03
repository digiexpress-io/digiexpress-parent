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

import org.apache.commons.lang3.exception.ExceptionUtils;

import io.resys.limaone.ast.Flow_AST.BodyStatement;
import io.resys.limaone.ast.Flow_AST.DecisionTableStatement;
import io.resys.limaone.program.DecisionProgram.DecisionResult;
import io.resys.limaone.program.FlowProgram.FlowExecutionStatus;
import io.resys.limaone.program.FlowProgram.FlowResultLog;
import io.resys.limaone.program.ImmutableFlowResultErrorLog;
import io.resys.limaone.program.ImmutableFlowResultLog;
import io.resys.limaone.program.Program.ProgramResult;
import io.resys.limaone.spi.program.FlowProgramExecutor.StatementException;
import io.resys.limaone.spi.program.result.ResultEnvlope;
import io.vertx.core.json.JsonObject;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class FlowStack {
  private final AtomicInteger sequence = new AtomicInteger(0);
  private final LocalDateTime start = LocalDateTime.now();
  private final Map<String, ResultEnvlope> frames = new HashMap<>();
  private final StringBuilder shortHistory = new StringBuilder();
  private final List<FlowResultLog> errorFrames = new ArrayList<>();
  private String lastStepId;

  
  public FlowStackResult close() {

    List<FlowResultLog> allLogs = new ArrayList<>()
        .stream().sorted()
        .sorted((a, b) -> Integer.compare(a.getId(), b.getId()))
        .toList();
    List<FlowResultLog> lastLogs = new ArrayList<>();
    
    final boolean isReturnsCollection = last != null && last.size() > 1;
    final Map<String, Serializable> returns = Map.<String, Serializable>of("", mergeResults(last)) : last.iterator().next().getReturns();
    
    return new FlowStackResult(
        allLogs, 
        lastLogs, 
        shortHistory.toString(), 
        lastStepId, 
        isReturnsCollection, 
        returns);
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
    errorFrames.add(errorFrame);
    logToHistory(errorFrame.getStepId(), Optional.empty());
  }
  
  public void newFrame(BodyStatement statement, Map<String, Serializable> inputs) {
    final var wrapper = frames.computeIfAbsent(statement.getTaskId(), (taskId) -> ResultEnvlope.of(sequence.incrementAndGet(), statement));
    wrapper.add(inputs);
    logToHistory(statement.getTaskId(), Optional.empty());
  }
  public void newFrame(BodyStatement statement, Map<String, Serializable> inputs, ProgramResult result) {
    final var wrapper = frames.computeIfAbsent(statement.getTaskId(), (taskId) -> ResultEnvlope.of(sequence.incrementAndGet(), statement));
    wrapper.add(inputs, result);
    logToHistory(statement.getTaskId(), Optional.empty());
  }
  
  
  
  public void newFrame(DecisionTableStatement statement, Map<String, Serializable> inputs, DecisionResult result, int size) {
    if(size == 1) {
      return result.iterator().next();
    } else if(size == 0) {
      
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
  }
  
  
  

  
  private String getLogIndent(Optional<ResultEnvlope> previous) {
    if(previous.isEmpty()) {
      return "";
    }
    StringBuilder result = new StringBuilder();
    for(int index = 0; index <= previous.get().getId(); index++) {
      result.append("  ");
    }
    return result.toString();
  }
  private void logToHistory(String stepId, Optional<ResultEnvlope> env) {
    if(shortHistory.length() > 0) {
      shortHistory.append(" -> ");
    }
    
    if(frames.containsKey(stepId)) {
      shortHistory.append("(recursion) ");
      shortHistory.append(System.lineSeparator() + getLogIndent(env));
    }
    shortHistory.append(stepId);
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
