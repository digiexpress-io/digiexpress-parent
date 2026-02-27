package io.resys.limaone.spi.compiler.flow;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.model.Flow;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.spi.compiler.CompilableUnit;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_Flow implements CompilableUnit {
  private final AST_Parser parser;
  private final ModelWorld world;
  private final Model<Flow> flow;
  
  
  @Override
  public OpenProgram compile(NewArtifact resolution) {
    // TODO Auto-generated method stub
    return null;
  }

}
