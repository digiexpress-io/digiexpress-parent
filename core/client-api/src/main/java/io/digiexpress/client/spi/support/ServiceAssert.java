package io.digiexpress.client.spi.support;

import java.util.ArrayList;
import java.util.function.Supplier;

import io.digiexpress.client.api.Client.ServiceClientException;

public class ServiceAssert {
  
  public static String BRANCH_MAIN = "main";
  
  public static class ServiceAssertException extends IllegalArgumentException implements ServiceClientException {
    private static final long serialVersionUID = 6305063707279384796L;
    public ServiceAssertException(String s) {
      super(s);
    }
  }
  public static void isUnique(Supplier<String> message, String ...strings) {
    
    final var result = new ArrayList<String>();
    for(final var value : strings) {
      if (result.contains(value)) {
        throw new ServiceAssertException(
            getMessage(message) + System.lineSeparator() + 
            " - values: " + String.join(", ", strings));
      }
      result.add(value);
    }
  }
  public static void notNull(Object object, Supplier<String> message) {
    if (object == null) {
      throw new ServiceAssertException(getMessage(message));
    }
  }
  public static void isNull(Object object, Supplier<String> message) {
    if (object != null) {
      throw new ServiceAssertException(getMessage(message));
    }
  }
  public static void notEmpty(String object, Supplier<String> message) {
    if (object == null || object.isBlank()) {
      throw new ServiceAssertException(getMessage(message));
    }
  }
  public static void isTrue(boolean expression, Supplier<String> message) {
    if (!expression) {
      throw new ServiceAssertException(getMessage(message));
    }
  }
  private static String getMessage(Supplier<String> supplier) {
    return (supplier != null ? supplier.get() : null);
  }

}
