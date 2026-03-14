package io.resys.limaone.spi.dialob.builders;



public class DialobException extends RuntimeException {
  private static final long serialVersionUID = 4905500192836989583L;

  public DialobException(String message, Throwable cause) {
    super(message, cause);
  }
  public DialobException(String message) {
    super(message);
  }
}
