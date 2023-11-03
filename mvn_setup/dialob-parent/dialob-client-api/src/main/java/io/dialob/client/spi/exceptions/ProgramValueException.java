package io.dialob.client.spi.exceptions;

import io.dialob.client.api.DialobErrorHandler.DialobClientException;

public class ProgramValueException extends RuntimeException implements DialobClientException {
  private static final long serialVersionUID = -7154685569622201632L;

  public ProgramValueException(String message) {
    super(message);
  }
  public ProgramValueException(String message, Throwable cause) {
    super(message, cause);
  }

}
