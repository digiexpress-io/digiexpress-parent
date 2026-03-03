package io.resys.limaone.spi.program.result;

import java.io.Serializable;
import java.time.LocalDateTime;
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
  
  public void add(Match match) {
    this.matches.add(match);
  }
  
  public void add(Map<String, Serializable> inputs) {
    
  }  
  public void add(Map<String, Serializable> inputs, ProgramResult result) {
    
  }  
  
  
  public void add(Map<String, Serializable> inputs, FlowTaskResult result) {
    
  }
  
  public void add(Map<String, Serializable> inputs, DecisionResult result) {
    
  }
  
  
  @Value
  public static class Match {
    private final int id;
    private final Map<String, Serializable> inputs;
    private final Map<String, Serializable> outputs;  
    private final Serializable outputRaw;
    private final ProgramResult src;
    private final LocalDateTime createdAt;
  }

  public static ResultEnvlope of(int sequence, BodyStatement statement) {
    return new ResultEnvlope(sequence, statement);
  }
  

}