package io.resys.limaone.spi.ast.attribute;

import io.vertx.core.json.JsonObject;

public class Attribute_AST_Exception extends RuntimeException {

  private static final long serialVersionUID = -7154685569622201632L;

  private final JsonObject props;

  public Attribute_AST_Exception(JsonObject props, String message, Throwable cause) {
    super(message, cause);
    this.props = props;
  }

  public Attribute_AST_Exception(String message) {
    super(message);
    this.props = null;
  }
  public Attribute_AST_Exception(String message, Throwable cause) {
    super(message, cause);
    this.props = null;
  }

  public Attribute_AST_Exception(JsonObject props, String message) {
    super(message);
    this.props = props;
  }

  public JsonObject getCommand() {
    return props;
  }
}
