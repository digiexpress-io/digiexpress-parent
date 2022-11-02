package io.digiexpress.client.spi.support;

import io.digiexpress.client.api.ServiceClient.ServiceClientException;

public class ReleaseException extends RuntimeException implements ServiceClientException {

  private static final long serialVersionUID = -7154685569622201632L;

  public ReleaseException(String message, Throwable cause) {
    super(message, cause);
  }

  public ReleaseException(String message) {
    super(message);
  }
}
