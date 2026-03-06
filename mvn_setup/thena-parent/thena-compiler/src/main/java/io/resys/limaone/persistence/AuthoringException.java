package io.resys.limaone.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.resys.limaone.authoring.Authoring.AuthoringModelProps;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class AuthoringException extends RuntimeException {
  private static final long serialVersionUID = 7190168525508589141L;
  
  private final List<AuthoringModelProps> entity = new ArrayList<>();
  
  public AuthoringException(AuthoringModelProps entity, String msg) {
    super(msg(Arrays.asList(entity), msg));
    this.entity.add(entity);
  }
  
  public List<AuthoringModelProps> getEntity() {
    return entity;
  }
  
  private static String msg(List<AuthoringModelProps> entity, String msg) {
    StringBuilder messages = new StringBuilder()
      .append(System.lineSeparator())
      .append("  - ").append(msg);
    return new StringBuilder("Can't persist model").append(System.lineSeparator())
        .append("  errors: ").append(System.lineSeparator())
        .append(messages)
        .append("  model: ").append(System.lineSeparator())
        .append(entity.size() == 1 ? JsonObject.mapFrom(entity.getFirst()).encodePrettily() : new JsonArray(entity).encodePrettily())
        .toString();
  }
}
