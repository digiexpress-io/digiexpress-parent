package io.resys.limaone.spi.program.stack;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.resys.limaone.ast.Flow_AST.BodyStatement;
import io.resys.limaone.program.DecisionProgram.DecisionResult;
import io.resys.limaone.program.ExpressionProgram.ExpressionResult;
import io.resys.limaone.program.FlowTaskProgram.FlowTaskResult;
import io.resys.limaone.program.Program.ProgramResult;
import io.resys.limaone.spi.program.DecisionProgramExecutor;
import io.resys.limaone.spi.program.assignment.Assignment;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@Getter
@RequiredArgsConstructor
public class StackFrame {
  private final int id;
  private final BodyStatement statement;
  private final List<StackFrameBody> matches = new ArrayList<>();
  
  public void add(Map<String, Serializable> inputs, LocalDateTime startAt) {
    matches.add(
        new StackFrameBody(
            matches.size(), 
            Collections.emptyMap(), 
            inputs,
            Optional.empty(),
            Optional.empty(), 
            startAt, LocalDateTime.now())
    );
  }  
  @SuppressWarnings("unchecked")
  public void add(Map<String, Serializable> inputs, ProgramResult result, LocalDateTime startAt) {
    
    Map<String, Serializable> outputs = Collections.emptyMap();
    Optional<Serializable> raw = Optional.empty();
    if(result instanceof DecisionResult) {
      final var dt = (DecisionResult) result;
      final var isCollection = dt.getMatches().size() > 1;
      outputs = isCollection ? 
        Assignment.toArrayMap(DecisionProgramExecutor.find(dt).stream()) : 
        DecisionProgramExecutor.get(dt);
      raw = Optional.ofNullable((Serializable) outputs);
      
    } else if (result instanceof FlowTaskResult) {
      final var ft = (FlowTaskResult) result;
      raw = Optional.ofNullable(ft.getValue());
      outputs = raw.map(JsonObject::mapFrom)
          .map(json -> json.mapTo(Map.class))
          .orElse(Collections.<String, Serializable>emptyMap()); 
    } else if(result instanceof ExpressionResult) {
      
      final var et = (ExpressionResult) result;
      outputs = Map.of(et.getSrc(), (Serializable) et.getValue());      
    }
    
    
    
    matches.add(
        new StackFrameBody(
            matches.size(), 
            inputs, 
            outputs, 
            raw,
            Optional.of(result), 
            startAt, LocalDateTime.now())
    );
  }  
  
  
  @Value
  public static class StackFrameBody {
    private final int id;
    private final Map<String, Serializable> inputs;
    private final Map<String, Serializable> outputs;
    private final Optional<Serializable> raw;
    private final Optional<ProgramResult> src;
    
    private final LocalDateTime startedAt;
    private final LocalDateTime endedAt;
    
    public long getCost() {
      return ChronoUnit.MILLIS.between(startedAt, endedAt);
    }
  }

  public static StackFrame of(int sequence, BodyStatement statement) {
    return new StackFrame(sequence, statement);
  }
  

}