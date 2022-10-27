package io.digiexpress.client.api;

public interface ServiceException {
  
  
  
  public static class ProgramNotFoundException extends RuntimeException implements ServiceException {
    private static final long serialVersionUID = 1995945947775467635L;
    private final String id;
    
    public ProgramNotFoundException(String message) {
      super(message);
      this.id = null;
    }
    
    public ProgramNotFoundException(String message, String id) {
      super(message);
      this.id = id;
    }

    public String getId() {
      return id;
    }
  }
}
