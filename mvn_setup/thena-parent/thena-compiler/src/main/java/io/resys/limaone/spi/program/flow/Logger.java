package io.resys.limaone.spi.program.flow;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.exception.ExceptionUtils;

import io.resys.limaone.program.FlowProgram.FlowExecutionStatus;
import io.resys.limaone.program.FlowProgram.FlowProgramStep;
import io.resys.limaone.program.FlowProgram.FlowResultErrorLog;
import io.resys.limaone.program.FlowProgram.FlowResultLog;
import io.resys.limaone.program.ImmutableFlowResultErrorLog;
import io.resys.limaone.program.ImmutableFlowResultLog;




public class Logger {
  private final LocalDateTime start = LocalDateTime.now();
  private final Map<String, FlowResultLog> stepLogs = new HashMap<>();
  private final StringBuilder shortHistory = new StringBuilder();
  
  private List<FlowResultLog> last;
  private boolean isBlowUp;
  
  
  
  

  public void ok(FlowProgramStep step, Consumer<ImmutableFlowResultLog.Builder> logs) {
    
  }
  
  public void error(Exception e) {
    this.isBlowUp = true;
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
  
  
  public List<FlowResultLog> close() {
    final List<FlowResultLog> logs = new ArrayList<>(stepLogs.values());
    if(last != null && isBlowUp) {
      logs.addAll(last);
    }
    Collections.sort(logs, (o1, o2) -> Integer.compare(o1.getId(), o2.getId()));  
    return logs;
  }
  
}
