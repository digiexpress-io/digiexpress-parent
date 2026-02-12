package io.resys.thena.fs.spi.tag;

import io.vertx.core.json.JsonObject;

public class TagQueryException extends RuntimeException {

  private static final long serialVersionUID = 3868491498774789368L;

  public TagQueryException(String msg, JsonObject props) {
    super(msg + System.lineSeparator() + props.encodePrettily());
  }
}
