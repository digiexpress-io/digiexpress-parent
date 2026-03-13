package io.resys.limaone.program;

import java.io.Serializable;
import java.util.Map;

public interface DialobProgram extends Program {
  
  FormInstanceResult run(CreateFormInstanceInput props);

  
  record CreateFormInstanceInput(String locale, Map<String, Serializable> context) {}
  
  interface FormInstanceResult {
    String getFormName();
    String getFormVersion();
    String getFormSessionId();
  }
}
