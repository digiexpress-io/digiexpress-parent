package io.digiexpress.client.spi.support;

import java.util.function.Supplier;

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
  public static ExecutorException processNotFound(String nameOrId, Supplier<String> msg) {
    return new ExecutorException("Process not found by name or id: '" + nameOrId + "'!" + System.lineSeparator() + msg.get());
  }
  
  public static ExecutorException processInitVariableAlreadyDefined(String nameOrId, Supplier<String> msg) {
    return new ExecutorException("Process id: '" + nameOrId + "' variabled already defined'!" + System.lineSeparator() + msg.get());
  }
}
