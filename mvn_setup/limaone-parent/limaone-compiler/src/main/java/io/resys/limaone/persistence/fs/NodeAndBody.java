package io.resys.limaone.persistence.fs;

import java.util.Optional;

import io.resys.limaone.model.Model.Body;
import io.resys.limaone.model.Model.BodyType;
import io.resys.thena.fs.entities.Node;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value
public class NodeAndBody {
  Node value;
  BodyType bodyType;
  Optional<Body> body;
  
  @SuppressWarnings("unchecked")
  public <T extends Body> T getBodyOfType() {
    return (T) body.orElse(null);
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Body> T getBodyOfType(Class<T> clazz) {
    return (T) body.orElse(null);
  }
  
  public String getObjectId() {
    return value.getObjectId();
  }

  /**
   * resolve dirent type based on blob or other props
   */
  public static Optional<NodeAndBody> of(Node node) {

    if(node.getBlobId().isEmpty()) {
      return Optional.of(new NodeAndBody(node, BodyType.FOLDER, Optional.empty()));
    }
    final var blob = node.getTransitives().getBlob();
    
    
    try {
      final var type = BodyType.valueOf(blob.getBlobType());
      
      final Body body;
      if(blob.getBlobValue() == null) {
        body = null;
      } else {
        body = blob.getBlobValue().mapTo(type.getBodyClass());
      }
      
      return Optional.of(new NodeAndBody(node, type, Optional.of(body)));
    } catch(Exception e) {
      log.warn("Failed to get node type from blob: {}, message: {}", node.getNodeName(), e.getMessage());
      return Optional.empty();
    }
  }
}