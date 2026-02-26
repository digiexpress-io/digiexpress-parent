package io.resys.limaone.spi.compiler;

import io.resys.limaone.spi.dependency.Resolution.ResolutionBuilder;

public interface CompilableUnit {
  OpenProgram compile(ResolutionBuilder resolution);
  
  interface OpenProgram {
    
  }
  
  interface CompilationEvent {
    
  }
}
