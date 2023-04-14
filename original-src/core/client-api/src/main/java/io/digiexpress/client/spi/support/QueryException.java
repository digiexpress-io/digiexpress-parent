package io.digiexpress.client.spi.support;


import io.digiexpress.client.api.Client.ClientException;



public class QueryException extends RuntimeException implements ClientException {
  private static final long serialVersionUID = 7190168525508589141L;

  public QueryException(String message, Throwable cause) {
    super(message, cause);
  }

  public QueryException(String message) {
    super(message);
  }
}
