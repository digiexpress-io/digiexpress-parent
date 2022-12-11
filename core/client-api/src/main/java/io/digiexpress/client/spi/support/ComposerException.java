package io.digiexpress.client.spi.support;

import io.digiexpress.client.api.ServiceClient.ServiceClientException;

public class ComposerException extends RuntimeException implements ServiceClientException {

  private static final long serialVersionUID = -7154685569622201632L;

  public ComposerException(String message, Throwable cause) {
    super(message, cause);
  }

  public ComposerException(String message) {
    super(message);
  }
}
