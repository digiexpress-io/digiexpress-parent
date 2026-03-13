package io.resys.limaone.program;

import java.io.Serializable;
import java.util.Map;

import org.immutables.value.Value;

public interface DialobProgram extends Program {
  
  FormInstanceResult run(CreateFormInstanceInput props);

  
  @Value.Immutable
  interface CreateFormInstanceInput extends ProgramInput {
    String getLocale();
    Map<String, Serializable> getContext(); 
  }
  
  interface FormInstanceResult {
    String getFormName();
    String getFormVersion();
    String getFormSessionId();
  }
}
