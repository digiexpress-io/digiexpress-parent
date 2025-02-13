package io.resys.thena.tasks.dev.app.events;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableUIEvent.class) @JsonDeserialize(as = ImmutableUIEvent.class)
public interface UIEvent {
  UIEventType getType();
}