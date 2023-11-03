package io.digiexpress.client.spi.support;

import io.digiexpress.client.api.Client.ClientException;

public class JsonMappingException extends RuntimeException implements ClientException {

  private static final long serialVersionUID = -7154685569622201632L;

  public JsonMappingException(String message, Throwable cause) {
    super(message, cause);
  }

  public JsonMappingException(String message) {
    super(message);
  }
}
