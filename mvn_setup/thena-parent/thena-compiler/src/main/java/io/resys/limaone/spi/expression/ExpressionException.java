package io.resys.limaone.spi.expression;

import io.vertx.core.json.JsonObject;

public class ExpressionException extends RuntimeException {

  private static final long serialVersionUID = -7154685569622201632L;

  private final JsonObject props;

  public ExpressionException(Object props, String message, Throwable cause) {
    super(message, cause);
    this.props = props == null ? null : JsonObject.mapFrom(props);
  }

  public ExpressionException(String message) {
    super(message);
    this.props = null;
  }
  public ExpressionException(String message, Throwable cause) {
    super(message, cause);
    this.props = null;
  }

  public ExpressionException(Object props, String message) {
    super(message);
    this.props = props == null ? null : JsonObject.mapFrom(props);
  }

  public JsonObject getCommand() {
    return props;
  }
}
