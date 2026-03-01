package io.resys.limaone.spi.ast.flow;

import java.util.Collections;
import java.util.Map;

import io.resys.limaone.ast.Flow_AST.MappingStatement;
import lombok.Getter;

@Getter
public class ImmutableMappingStatement implements MappingStatement {

  private final Map<String, String> assignments;
  private final boolean isObjectMapping;
  
  public ImmutableMappingStatement(Map<String, String> assignments, boolean isObjectMapping) {
    super();
    this.assignments = Collections.unmodifiableMap(assignments);
    this.isObjectMapping = isObjectMapping;
  }
}