package io.resys.limaone.spi.compiler;

import io.resys.limaone.ast.Flow_AST;
import io.resys.limaone.spi.compiler.CompilableUnit.NewArtifact;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_FlowDepsValidator {
  private final NewArtifact resolution;
  private final Flow_AST ast;
  
  public void validate() {
  }
}
