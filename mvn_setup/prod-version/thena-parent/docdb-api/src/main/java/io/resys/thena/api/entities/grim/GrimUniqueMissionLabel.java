package io.resys.thena.api.entities.grim;

import org.immutables.value.Value;

import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface GrimUniqueMissionLabel {
  String getLabelType(); 
  String getLabelValue();
  @Nullable JsonObject getLabelBody();
}