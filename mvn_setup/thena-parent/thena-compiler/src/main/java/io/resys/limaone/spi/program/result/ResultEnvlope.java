package io.resys.limaone.spi.program.result;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.resys.limaone.ast.Flow_AST.BodyStatement;
import io.resys.limaone.program.DecisionProgram.DecisionResult;
import io.resys.limaone.program.FlowTaskProgram.FlowTaskResult;
import io.resys.limaone.program.Program.ProgramResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@Getter
@RequiredArgsConstructor
public class ResultEnvlope {
  private final int id;
  private final BodyStatement statement;
  private final List<Match> matches = new ArrayList<>();
  
  private void add(Match match, LocalDateTime startedAt) {
    this.matches.add(match);
  }
  
  public void add(Map<String, Serializable> inputs, LocalDateTime startAt) {
    
  }  
  public void add(Map<String, Serializable> inputs, ProgramResult result, LocalDateTime startAt) {
    
  }  
  
  
  private void add(Map<String, Serializable> inputs, FlowTaskResult result) {
    
  }
  
  private void add(Map<String, Serializable> inputs, DecisionResult result) {
    
  }
  
  
  @Value
  public static class Match {
    private final int id;
    private final Map<String, Serializable> inputs;
    private final Map<String, Serializable> outputs;  
    private final Serializable outputRaw;
    private final ProgramResult src;
    private final LocalDateTime startedAt;
    private final LocalDateTime endedAt;
    
    public long getCost() {
      return ChronoUnit.MILLIS.between(startedAt, endedAt);
    }
  }

  public static ResultEnvlope of(int sequence, BodyStatement statement) {
    return new ResultEnvlope(sequence, statement);
  }
  

}