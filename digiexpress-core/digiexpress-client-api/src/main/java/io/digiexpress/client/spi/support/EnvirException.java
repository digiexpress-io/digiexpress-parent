package io.digiexpress.client.spi.support;

import java.util.function.Supplier;

import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;

public class EnvirException extends RuntimeException {
  private static final long serialVersionUID = 7190168525508589141L;
  
  public EnvirException(String msg) {
    super(msg);
  }  
  public static EnvirException isDefined(ServiceReleaseDocument wrapper, Supplier<String> msg) {
    return new EnvirException(
        "Service release: '" + wrapper.getName() + "' can't be defined because there is already a " + 
        "release for date: '" + wrapper.getActiveFrom() + "'!" + System.lineSeparator() + msg.get());
  }
  
}
