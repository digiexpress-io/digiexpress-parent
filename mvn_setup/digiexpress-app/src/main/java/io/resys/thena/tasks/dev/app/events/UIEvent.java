package io.resys.thena.tasks.dev.app.events;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class UIEvent {
  private final UIEventType type;
}