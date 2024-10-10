package io.resys.sysconfig.client.spi.executor.exceptions;

public class ExecutorException extends RuntimeException {
  private static final long serialVersionUID = 7190168525508589141L;
  
  public ExecutorException(String msg) {
    super(msg);
  }
  
  public ExecutorException(String msg, Exception e) {
    super(msg, e);
  }
  
}
