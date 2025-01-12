package io.digiexpress.thena.mq.client.api;

import java.util.Optional;

import org.immutables.value.Value;

import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.vertx.core.json.JsonObject;

@FunctionalInterface
public interface ThenaMqConsumer {
  MessageResponse accept(QueueMessage msg);
  
  @Value.Immutable
  interface MessageResponse {
    Optional<String> getComment();
    Optional<ConsumerWorkerError> getError();
    MessageResponseStatus getAck();
  }
  
  enum MessageResponseStatus {
    OK, ERROR
  }

  interface MessageBody {
    JsonObject getValue();
  }

  class ConsumerWorkerError extends RuntimeException {
    private static final long serialVersionUID = 8195192309468874169L;

    public ConsumerWorkerError(String message, Throwable cause) {
      super(message, cause);
    }

    public ConsumerWorkerError(String message) {
      super(message);
    }
  }
}
