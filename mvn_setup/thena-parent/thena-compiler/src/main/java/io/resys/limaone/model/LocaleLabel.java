package io.resys.limaone.model;

import java.io.Serializable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableLocaleLabel.class)
@JsonDeserialize(as = ImmutableLocaleLabel.class)
public interface LocaleLabel extends Serializable {
  
  String getLocale();
  String getLabelValue();
}