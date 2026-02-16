package io.resys.thena.fs.spi.commit;

import io.vertx.core.json.JsonObject;

public class CommitQueryException extends RuntimeException {

  private static final long serialVersionUID = 3868491498774789368L;

  public CommitQueryException(String msg, JsonObject props) {
    super(msg + System.lineSeparator() + props.encodePrettily());
  }
}
