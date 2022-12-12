package io.digiexpress.client.spi.support;

import java.util.function.Supplier;

import io.digiexpress.client.api.Client.ClientException;

public class ReleaseException extends RuntimeException implements ClientException {

  private static final long serialVersionUID = -7154685569622201632L;

  public ReleaseException(String message, Throwable cause) {
    super(message, cause);
  }

  public ReleaseException(String message) {
    super(message);
  }
  
  public static ReleaseException sanityRuleViolations(Supplier<String> msg) {
    return new ReleaseException(
        "Can't create release because of errors: " + System.lineSeparator() + 
        msg.get());
  }  
}
