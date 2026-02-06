package io.resys.thena.fs.entities;

import java.time.OffsetDateTime;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableProps.class)
@JsonDeserialize(as = ImmutableProps.class)
public interface Props extends FileSystemEntity {
  
  String getId();
  JsonObject getPropsLabels();
  JsonObject getPropsComments();
  JsonObject getPropsPermissions();
  JsonObject getPropsFlags();

  @Value.Auxiliary
  @Nullable 
  PropsTransitives getTransitives();

  @Override
  default FileSystemEntityType getDocType() { 
    return FileSystemEntityType.PROPS; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutablePropsTransitives.class)
  @JsonDeserialize(as = ImmutablePropsTransitives.class)
  interface PropsTransitives {
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
  }
}