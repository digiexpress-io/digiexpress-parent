package io.resys.thena.api.envelope;

import java.util.List;

import io.vertx.core.json.JsonObject;

public interface GitContainer extends ThenaContainer {
  <T> List<T> accept(BlobVisitor<T> visitor);

  @FunctionalInterface
  interface BlobVisitor<T> {
    T visit(JsonObject blobValue);
  }

}
