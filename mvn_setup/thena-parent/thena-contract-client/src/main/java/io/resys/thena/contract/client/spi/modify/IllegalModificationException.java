package io.resys.thena.contract.client.spi.modify;

@SuppressWarnings("serial")
public class IllegalModificationException extends RuntimeException {

  public IllegalModificationException(String message) {
    super(message);
  }

}
