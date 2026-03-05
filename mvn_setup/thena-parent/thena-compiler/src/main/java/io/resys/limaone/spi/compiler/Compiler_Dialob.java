package io.resys.limaone.spi.compiler;

import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.ModelWorld;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_Dialob implements CompilableUnit {

  private final ModelWorld world;
  private final Model<ArticleWorkflow> target;
  
  @Override
  public OpenProgram compile(NewArtifact resolution) {
    // TODO Auto-generated method stub
    return null;
  }
}
