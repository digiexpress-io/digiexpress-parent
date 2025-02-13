package io.dialob.client.spi.exceptions;

public class ErrorMsgBuilder {

  private final StringBuilder result = new StringBuilder(System.lineSeparator());
  
  public ErrorMsgBuilder(String msg) {
    result.append("Error desc: ").append(msg);
  }
  
  public final ErrorMsgBuilder field(String name, String value) {
    result.append(System.lineSeparator()).append("  - ").append(name).append(": ").append(value);
    return this;
  }
  
  public String build() {
    return result.toString();
  }

}
