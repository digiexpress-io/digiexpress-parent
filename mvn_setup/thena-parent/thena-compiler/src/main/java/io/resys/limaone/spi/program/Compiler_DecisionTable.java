package io.resys.limaone.spi.program;

import io.resys.limaone.model.DecisionTable;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.ModelWorld;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class Compiler_DecisionTable implements CompilableUnit {
  private final ModelWorld world;
  private final Model<DecisionTable> target;
}
