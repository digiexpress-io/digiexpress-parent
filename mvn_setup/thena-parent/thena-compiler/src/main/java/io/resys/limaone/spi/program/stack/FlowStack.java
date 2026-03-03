package io.resys.limaone.spi.program.stack;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import io.resys.limaone.ast.Flow_AST.DecisionTableStatement;
import io.resys.limaone.ast.Flow_AST.FlowTaskStatement;
import io.resys.limaone.ast.Flow_AST.ReturnsStatement;
import io.resys.limaone.program.DecisionProgram.DecisionResult;
import io.resys.limaone.program.FlowProgram.FlowExecutionStatus;
import io.resys.limaone.program.FlowProgram.FlowResultLog;
import io.resys.limaone.program.FlowTaskProgram.FlowTaskResult;
import io.resys.limaone.program.ImmutableFlowResultLog;

public class FlowStack {
  private final AtomicInteger sequence = new AtomicInteger(0);
  private final LocalDateTime start = LocalDateTime.now();
  private final Map<String, FlowResultLog> frames = new HashMap<>();
  private final StringBuilder shortHistory = new StringBuilder();
  
  private String lastStepId;
  private FlowExecutionStatus status = FlowExecutionStatus.COMPLETED;
  
  
  public void newFrame(Consumer<ImmutableFlowResultLog.Builder> callback) {
    final var builder = ImmutableFlowResultLog.builder()
        .id(sequence.incrementAndGet())
        .start(start)
        .isReturnsCollection(false)
        .end(LocalDateTime.now());
    callback.accept(builder);
    final var frame = builder.build();
    
    this.frames.put(frame.getStepId(), frame);
    this.lastStepId = frame.getStepId();
    
    
    if(shortHistory.length() > 0) {
      shortHistory.append(" -> ");
    }
    
    if(frames.containsKey(frame.getStepId())) {
      shortHistory.append("(recursion) ");
      shortHistory.append(System.lineSeparator() + getIndent(frame));
    }
    shortHistory.append(frame.getStepId());
  }
  
  
  public void newFrame(ReturnsStatement statement, Map<String, Serializable> inputs, int size) {
    
  }  
  
  public void newFrame(FlowTaskStatement statement, Map<String, Serializable> inputs, FlowTaskResult result, int size) {
    
  }
  
  public void newFrame(DecisionTableStatement statement, Map<String, Serializable> inputs, DecisionResult result, int size) {

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
  public FlowExecutionStatus getStatus() {
    return status;
  }
  public boolean isReturnsCollection() {
    return false;
  }
  public Map<String, Serializable> getReturns() {
    return Collections.emptyMap();
  }
  
  private String getIndent(FlowResultLog previous) {
    StringBuilder result = new StringBuilder();
    for(int index = 0; index <= previous.getId(); index++) {
      result.append("  ");
    }
    return result.toString();
  }
}
