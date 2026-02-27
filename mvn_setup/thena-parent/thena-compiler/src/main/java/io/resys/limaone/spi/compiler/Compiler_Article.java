package io.resys.limaone.spi.compiler;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.model.Model.ModelWorld;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_Article implements CompilableUnit {

  private final AST_Parser parser;
  private final ModelWorld world;

  @Override
  public OpenProgram compile(NewArtifact resolution) {

    
    parser.parseArticles();
    
    return null;
  }

  
  
  
  
}
