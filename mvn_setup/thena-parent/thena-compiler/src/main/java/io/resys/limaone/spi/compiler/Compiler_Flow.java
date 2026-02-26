package io.resys.limaone.spi.compiler;

import io.resys.limaone.model.Flow;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.ModelWorld;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_Flow implements CompilableUnit {

  private final ModelWorld world;
  private final Model<Flow> flow;

}
