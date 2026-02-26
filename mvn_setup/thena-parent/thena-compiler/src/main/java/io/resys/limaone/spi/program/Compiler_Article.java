package io.resys.limaone.spi.program;

import io.resys.limaone.model.Model.ModelWorld;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_Article implements CompilableUnit {

  private final ModelWorld world;

}
