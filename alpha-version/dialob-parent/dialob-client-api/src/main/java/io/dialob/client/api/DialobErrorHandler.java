package io.dialob.client.api;

import io.dialob.api.proto.Actions;

public interface DialobErrorHandler {

  Actions toActions(Exception e);
  
  interface DialobClientException {}

  
  public static class DocumentNotFoundException extends DatabaseException implements DialobClientException {
    private static final long serialVersionUID = 1995945947775467635L;
    private final String id;
    
    public DocumentNotFoundException(String message) {
      super(message);
      this.id = null;
    }
    
    public DocumentNotFoundException(String message, String id) {
      super(message);
      this.id = id;
    }

    public String getId() {
      return id;
    }
  }

  public static class DatabaseException extends RuntimeException implements DialobClientException {
    private static final long serialVersionUID = 7193990799948041231L;
    public DatabaseException(String message) {
      super(message);
    }
    public DatabaseException(String message, Throwable cause) {
      super(message, cause);
    }
  }

}
