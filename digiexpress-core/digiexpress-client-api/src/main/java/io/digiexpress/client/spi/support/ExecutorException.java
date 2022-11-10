package io.digiexpress.client.spi.support;

import java.util.function.Supplier;

import io.digiexpress.client.api.ServiceDocument.ProcessValue;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgram;

public class ExecutorException extends RuntimeException {
  private static final long serialVersionUID = 7190168525508589141L;
  
  public ExecutorException(String msg) {
    super(msg);
  }
  public static ExecutorException stencilContentNotFound(Supplier<String> msg) {
    return new ExecutorException("Stencil content not found!" + System.lineSeparator() + msg.get());
  }  
  public static ExecutorException serviceNotFound(Supplier<String> msg) {
    return new ExecutorException("Service not found!" + System.lineSeparator() + msg.get());
  }  
  public static ExecutorException processNotFound(ServiceProgram wrapper, String nameOrId, Supplier<String> msg) {
    return new ExecutorException("Process not found by name or id: '" + nameOrId + "'!" + System.lineSeparator() + msg.get());
  }
  
  public static ExecutorException processInitVariableAlreadyDefined(ServiceProgram wrapper, ProcessValue process, Supplier<String> msg) {
    return new ExecutorException("Process id: '" + process.getId() + "/" + process.getName() + " variabled already defined'!" + System.lineSeparator() + msg.get());
  }
}
