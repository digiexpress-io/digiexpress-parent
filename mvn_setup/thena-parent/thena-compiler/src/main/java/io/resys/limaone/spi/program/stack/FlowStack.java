package io.resys.limaone.spi.program.stack;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.commons.lang3.exception.ExceptionUtils;

import io.resys.limaone.ast.Flow_AST.BodyStatement;
import io.resys.limaone.ast.Flow_AST.DecisionTableStatement;
import io.resys.limaone.ast.Flow_AST.ReturnsStatement;
import io.resys.limaone.program.DecisionProgram.DecisionResult;
import io.resys.limaone.program.FlowProgram.FlowExecutionStatus;
import io.resys.limaone.program.FlowProgram.FlowResultLog;
import io.resys.limaone.program.ImmutableFlowResultErrorLog;
import io.resys.limaone.program.ImmutableFlowResultLog;
import io.resys.limaone.program.Program.ProgramResult;
import io.resys.limaone.spi.program.FlowProgramExecutor.StatementException;
import io.resys.limaone.spi.program.result.ResultEnvlope;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class FlowStack {
  private final AtomicInteger sequence = new AtomicInteger(0);
  private final LocalDateTime start = LocalDateTime.now();
  private final Map<String, ResultEnvlope> frames = new HashMap<>();
  private final StringBuilder shortHistory = new StringBuilder();
  private String lastStepId;

  
  private void newFrame(ResultEnvlope frame) {
    final var stepId = frame.getStatement().getTaskId();
    
    this.frames.put(stepId, frame);
    this.lastStepId = stepId;
    
    if(shortHistory.length() > 0) {
      shortHistory.append(" -> ");
    }
    
    if(frames.containsKey(stepId)) {
      shortHistory.append("(recursion) ");
      shortHistory.append(System.lineSeparator() + getIndent(frame));
    }
    shortHistory.append(stepId);
  }

  
  public void newError(Exception exception) {
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
    if(exception instanceof StatementException) {
      final StatementException ex = (StatementException) exception;
      errorMsg.append(System.lineSeparator())
        .append(" props: ").append(JsonObject.mapFrom(ex.getProps()).encodePrettily()).append(System.lineSeparator())
        .append(" statement: ").append(JsonObject.mapFrom(ex.getStatement()).encodePrettily());
    }
    
    newFrame((newFrame) -> newFrame
        .addErrors(ImmutableFlowResultErrorLog.builder().id("error").msg(errorMsg.toString()).build())
        .addErrors(ImmutableFlowResultErrorLog.builder().id("trace").msg(traceBuilder.toString()).build())
        .status(FlowExecutionStatus.ERROR));
  }
  
  public void newFrame(BodyStatement statement, Map<String, Serializable> inputs) {
    final var wrapper = frames.computeIfAbsent(statement.getTaskId(), (taskId) -> ResultEnvlope.of(statement));
    wrapper.add(inputs);
  }
  public void newFrame(BodyStatement statement, Map<String, Serializable> inputs, ProgramResult result) {
    final var wrapper = frames.computeIfAbsent(statement.getTaskId(), (taskId) -> ResultEnvlope.of(statement));
    wrapper.add(inputs, result);
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
  
  public String getLastStepId() {
    return this.lastStepId;
  }
  public String getShortHistory() {
    return shortHistory.toString();
  }
  public List<FlowResultLog> getLogs() {
    return frames.values().stream().sorted()
        .sorted((a, b) -> a.getId().compareTo(b.getId()))
        .toList();    
  }
  public List<FlowResultLog> getLastLogs() {
    return Collections.emptyList();
  }
  public boolean isReturnsCollection() {
    final var isArray = last != null && last.size() > 1;
    return false;
  }
  public Map<String, Serializable> getReturns() {
    Map.<String, Serializable>of("", mergeResults(last)) : last.iterator().next().getReturns()
    return Collections.emptyMap();
  }
  
  private String getIndent(ResultEnvlope previous) {
    StringBuilder result = new StringBuilder();
    for(int index = 0; index <= previous.getId(); index++) {
      result.append("  ");
    }
    return result.toString();
  }
}
