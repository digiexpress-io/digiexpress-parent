package io.dialob.client.spi.event;

import java.io.Serializable;

import javax.annotation.Nonnull;

public interface EventPublisher {
  void publish(@Nonnull Event event);
  
  interface Event extends Serializable { }
  
}
