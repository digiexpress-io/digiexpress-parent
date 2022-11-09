package io.digiexpress.client.spi.support;

import java.util.function.Supplier;

import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramSource;

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
  public static EnvirException notFoundHash(String id, Supplier<String> msg) {
    return new EnvirException(
        "Service program source with hash: '" + id + "' is not defined." + 
        System.lineSeparator() + msg.get());
  }
  public static EnvirException notFoundId(String id, Supplier<String> msg) {
    return new EnvirException(
        "Service program source with id: '" + id + "' is not defined." + 
        System.lineSeparator() + msg.get());
  }
  public static EnvirException notSupportedSource(ServiceProgramSource src, Supplier<String> msg) {
    return new EnvirException(
        "Service program source with id: '" + src.getId() + "/" + src.getType() + "' is not supported." + 
        System.lineSeparator() + msg.get());
  }  
}
