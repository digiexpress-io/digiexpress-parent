package io.resys.limaone.spi.program;

import io.resys.limaone.spi.dependency.Resolution.ResolutionBuilder;

public interface CompilableUnit {
  OpenProgram compile(ResolutionBuilder resolution);
  
  interface OpenProgram {
    
  }
  
  interface CompilationEvent {
    
  }
}
