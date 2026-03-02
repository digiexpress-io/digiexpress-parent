package io.resys.limaone.spi.ast.flow;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.resys.limaone.ast.Flow_AST.MappingStatement;
import lombok.Getter;

@Getter
public class ImmutableMappingStatement implements MappingStatement {

  private final Map<String, String> assignments;
  private final List<String> deconstructors; 
  private final boolean deconstructing;
  private final String taskId;
  
  public ImmutableMappingStatement(Map<String, String> assignments, List<String> deconstructors, String taskId) {
    super();
    this.assignments = Collections.unmodifiableMap(assignments);
    this.deconstructing = !deconstructors.isEmpty();
    this.deconstructors = deconstructors;
    this.taskId = taskId;
  }
}