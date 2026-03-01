package io.resys.limaone.program;


import java.io.Serializable;
import java.util.Map;

import io.resys.limaone.program.FlowProgram.FlowResult;
import io.resys.limaone.program.FlowProgram.FlowResultLog;
import jakarta.annotation.Nullable;



public interface FlowProgram2 extends Program {
  
  FlowExecutor run(ProgramInput input, Runtime runtime);
  FlowExecutor run(Map<String, Serializable> input);
  
  interface FlowExecutor {
    @Nullable
    FlowResultLog andGetTask(String task);
    FlowResult andGetBody();
  }
}
