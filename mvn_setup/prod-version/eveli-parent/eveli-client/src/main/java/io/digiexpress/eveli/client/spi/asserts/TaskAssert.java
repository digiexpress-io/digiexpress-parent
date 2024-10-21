package io.digiexpress.eveli.client.spi.asserts;

import java.util.function.Supplier;

public class TaskAssert {
  public static class TaskException extends RuntimeException {

    private static final long serialVersionUID = 1781444267360040922L;

    public TaskException(String message, Throwable cause) {
      super(message, cause);
    }

    public TaskException(String message) {
      super(message);
    }
  }
  
  public static void notNull(Object object, Supplier<String> message) {
    if (object == null) {
      throw new TaskException(getMessage(message));
    }
  }
  public static void notEmpty(String object, Supplier<String> message) {
    if (object == null || object.isBlank()) {
      throw new TaskException(getMessage(message));
    }
  }
  public static void isTrue(boolean expression, Supplier<String> message) {
    if (!expression) {
      throw new TaskException(getMessage(message));
    }
  }
  private static String getMessage(Supplier<String> supplier) {
    return (supplier != null ? supplier.get() : null);
  }

}
