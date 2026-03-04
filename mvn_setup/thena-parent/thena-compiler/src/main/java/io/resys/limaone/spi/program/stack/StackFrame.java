package io.resys.limaone.spi.program.stack;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
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
    final var endAt  = LocalDateTime.now();
    final Optional<ProgramResult> src = Optional.of(result);    
    
    if(result instanceof DecisionResult) {
      final var dt = (DecisionResult) result;
      final var isCollection = dt.getMatches().size() > 1;
      final var unwrap = isCollection ? DecisionProgramExecutor.find(dt) : Arrays.asList(DecisionProgramExecutor.get(dt));
      
      for(final var set : unwrap) {
        final Map<String, Serializable> outputs = set;
        final Optional<Serializable> raw = Optional.of((Serializable) set);
        final int id = matches.size();
        matches.add(new StackFrameBody(id, inputs, outputs, raw, src, startAt, endAt));
      }
      return;
    } else if (result instanceof FlowTaskResult) {
      final int id = matches.size();
      final var ft = (FlowTaskResult) result;
      final var raw = Optional.ofNullable(ft.getValue());
      final var outputs = raw.map(JsonObject::mapFrom)
          .map(json -> json.mapTo(Map.class))
          .orElse(Collections.<String, Serializable>emptyMap());
      matches.add(new StackFrameBody(id, inputs, outputs, raw, src, startAt, endAt));
      return;      
    } else if(result instanceof ExpressionResult) {
      final int id = matches.size();
      final var et = (ExpressionResult) result;
      final var outputs = Map.of(et.getSrc(), (Serializable) et.getValue());
      final var raw = Optional.ofNullable((Serializable) et.getValue());
      matches.add(new StackFrameBody(id, inputs, outputs, raw, src, startAt, endAt));
      return;
    }
    
    throw new RuntimeException("Result handling for: " + result.getClass() + " not implemented");
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