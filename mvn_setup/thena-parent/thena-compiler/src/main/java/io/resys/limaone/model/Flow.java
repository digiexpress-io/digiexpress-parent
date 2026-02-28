package io.resys.limaone.model;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.Body;


@Value.Immutable
@JsonSerialize(as = ImmutableFlow.class)
@JsonDeserialize(as = ImmutableFlow.class)
public interface Flow extends Body {
  String getFlowName();
  String getFlowValue();
}
