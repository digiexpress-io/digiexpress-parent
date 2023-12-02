package io.resys.sysconfig.client.spi.support;

import java.util.ArrayList;
import java.util.function.Supplier;

public class SysConfigAssert {

  public static class SysConfigAssertException extends IllegalArgumentException {
    private static final long serialVersionUID = 6305063707279384796L;
    public SysConfigAssertException(String s) {
      super(s);
    }
  }
  public static void isUnique(Supplier<String> message, String ...strings) {
    
    final var result = new ArrayList<String>();
    for(final var value : strings) {
      if (result.contains(value)) {
        throw new SysConfigAssertException(
            getMessage(message) + System.lineSeparator() + 
            " - values: " + String.join(", ", strings));
      }
      result.add(value);
    }
  }
  public static void notNull(Object object, Supplier<String> message) {
    if (object == null) {
      throw new SysConfigAssertException(getMessage(message));
    }
  }
  public static void isNull(Object object, Supplier<String> message) {
    if (object != null) {
      throw new SysConfigAssertException(getMessage(message));
    }
  }
  public static void notEmpty(String object, Supplier<String> message) {
    if (object == null || object.isBlank()) {
      throw new SysConfigAssertException(getMessage(message));
    }
  }
  public static void isTrue(boolean expression, Supplier<String> message) {
    if (!expression) {
      throw new SysConfigAssertException(getMessage(message));
    }
  }
  private static String getMessage(Supplier<String> supplier) {
    return (supplier != null ? supplier.get() : null);
  }

}
