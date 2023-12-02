package io.resys.sysconfig.client.spi.support;

import io.vertx.core.json.JsonObject;

public interface ErrorMsg {

  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private JsonObject props;
    private String code;
    private Exception e;
    private String message;
    
    public Builder withProps(JsonObject props) { this.props = props; return this; }
    public Builder withCode(String code) { this.code = code; return this; }
    public Builder withMessage(String message) { this.message = message; return this; }
    public Builder withException(Exception e) { this.e = e; return this; }

    public String toString() {
      SysConfigAssert.notEmpty(code, () -> "code must be defined!");
      final var result = new StringBuilder();
      result.append("code/").append(code).append("/").append(message);
      if(props != null) {
        result
        .append("/props/").append(props.encode());
      }
      
      if(e != null) {
        result
        .append("/exception/").append(e.getClass().getSimpleName())
        .append("/").append(e.getMessage());
      }
      
      return result.toString();
    }
  }
}
