package io.digiexpress.thena.mq.client.api;

import java.time.OffsetDateTime;
import java.util.Optional;

import io.vertx.core.json.JsonObject;

@FunctionalInterface
public interface ThenaMqConsumer {
  MessageResponse accept(MessageHeader header, MessageBody body);
  
    
  interface MessageResponse {
    Optional<String> getComment();
    Optional<ConsumerWorkerError> getError();
    MessageResponseStatus getAck();
  }
  
  enum MessageResponseStatus {
    OK, ERROR
  }
  
  interface MessageHeader {
    String getBodyType();
    String getRoutingKey();
    
    String getPublisherId();
    String getPublisherBodyId();
    
    OffsetDateTime getPublishedAt();
    Optional<OffsetDateTime> getExpiresAt();
    Optional<OffsetDateTime> getStartsAt();
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
