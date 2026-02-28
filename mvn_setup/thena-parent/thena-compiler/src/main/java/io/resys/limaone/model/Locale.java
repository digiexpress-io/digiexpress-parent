package io.resys.limaone.model;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.Body;

@Value.Immutable
@JsonSerialize(as = ImmutableLocale.class)
@JsonDeserialize(as = ImmutableLocale.class)
public interface Locale extends Body {
  String getValue();
  Boolean getEnabled();
}
