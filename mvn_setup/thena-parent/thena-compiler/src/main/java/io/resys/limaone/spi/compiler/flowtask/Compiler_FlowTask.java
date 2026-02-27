package io.resys.limaone.spi.compiler.flowtask;

import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.spi.compiler.CompilableUnit;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_FlowTask implements CompilableUnit {
  private final ModelWorld world;
  private final Model<FlowTask> flowTask;
}
