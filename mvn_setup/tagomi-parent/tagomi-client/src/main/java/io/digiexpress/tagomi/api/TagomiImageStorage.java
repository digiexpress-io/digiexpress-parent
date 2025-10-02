package io.digiexpress.tagomi.api;

import java.util.List;

import org.immutables.value.Value;

import io.resys.thena.api.envelope.Message;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

/**
 * Integration point for storing images.
 */
public interface TagomiImageStorage {
  Uni<ImageEnvlope> write(byte[] body);
  Uni<ImageEnvlope> read(String id);
  
  @Value.Immutable
  interface ImageEnvlope {
    OperationStatus getOperationStatus();
    List<Message> getOperationLogs();    
    @Nullable Image getObject(); // Operation result
  }
  
  @Value.Immutable
  interface Image {
    String getId();
    byte[] getBody();
  }
    
  enum OperationStatus { OK, ERROR }
}
