package io.resys.limaone.model;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableModelError.class)
@JsonDeserialize(as = ImmutableModelError.class)
public interface ModelError {
  @Nullable String getId();
  String getMsg();

  @Nullable Integer getLine();
  @Nullable Integer getColumn();
  @Nullable Exception getException();
}
