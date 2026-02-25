package io.resys.limaone.spi.ast.decisiontable;


import io.vertx.core.json.JsonObject;


public class AST_Exception extends RuntimeException {

  private static final long serialVersionUID = -7154685569622201632L;

  private final JsonObject props;

  public AST_Exception(Object props, String message, Throwable cause) {
    super(message, cause);
    this.props = props == null ? null : JsonObject.mapFrom(props);
  }

  public AST_Exception(String message) {
    super(message);
    this.props = null;
  }
  public AST_Exception(String message, Throwable cause) {
    super(message, cause);
    this.props = null;
  }

  public AST_Exception(Object props, String message) {
    super(message);
    this.props = props == null ? null : JsonObject.mapFrom(props);
  }

  public JsonObject getCommand() {
    return props;
  }

}
