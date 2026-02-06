package io.resys.thena.fs.spi.queries;

import io.vertx.core.json.JsonObject;

public class FileSystemQueryException extends RuntimeException {

  private static final long serialVersionUID = 3868491498774789368L;

  
  public FileSystemQueryException(String msg, JsonObject props) {
    super(
        msg + System.lineSeparator() + props.encodePrettily()
    );
  }
}
