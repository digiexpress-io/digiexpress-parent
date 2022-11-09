package io.digiexpress.client.spi.support;

import java.util.function.Supplier;

import io.digiexpress.client.api.ServiceDocument.ProcessValue;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgram;

public class ServiceExecutorException extends RuntimeException {
  private static final long serialVersionUID = 7190168525508589141L;
  
  public ServiceExecutorException(String msg) {
    super(msg);
  }
  public static ServiceExecutorException serviceNotFound(Supplier<String> msg) {
    return new ServiceExecutorException("Service not found!" + System.lineSeparator() + msg.get());
  }  
  public static ServiceExecutorException processNotFound(ServiceProgram wrapper, String nameOrId, Supplier<String> msg) {
    return new ServiceExecutorException("Process not found by name or id: '" + nameOrId + "'!" + System.lineSeparator() + msg.get());
  }
  
  public static ServiceExecutorException processInitVariableAlreadyDefined(ServiceProgram wrapper, ProcessValue process, Supplier<String> msg) {
    return new ServiceExecutorException("Process id: '" + process.getId() + "/" + process.getName() + " variabled already defined'!" + System.lineSeparator() + msg.get());
  }
}
