package io.resys.limaone.program;

import java.io.Serializable;
import java.util.Map;


public interface DialobProgram extends Program {
  
  DialobExecutor run(ProgramInput input, Runtime runtime);
  DialobExecutor run(Map<String, Serializable> input);

  interface DialobExecutor {
    DialobResult andGetBody();
  }
  
  interface DialobResult {
    String getFormName();
    String getFormVersion();
    String getFormSessionId();
  }
}
