package io.resys.limaone.spi.program;

public class ProgramException extends RuntimeException {

  private static final long serialVersionUID = -7154685569622201632L;

  public ProgramException(String message) {
    super(message);
  }
  public ProgramException(String message, Throwable cause) {
    super(message, cause);
  }
}