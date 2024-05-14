package io.resys.avatar.client.api;

public class AvatarNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 3045255967621840944L;

  public AvatarNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public AvatarNotFoundException(String message) {
    super(message);
  }

  public AvatarNotFoundException(Throwable cause) {
    super(cause);
  }
}