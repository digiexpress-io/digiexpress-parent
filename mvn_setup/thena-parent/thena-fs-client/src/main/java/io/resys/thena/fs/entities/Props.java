package io.resys.thena.fs.entities;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.hash.Hashing;

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
  
  
  // H(props) = μ(props_labels ⊕ props_comments ⊕ props_permissions ⊕ props_flags)
  public static ImmutableProps.Builder newInstance(JsonObject labels, JsonObject comments, JsonObject permissions, JsonObject flags) {
    final var content = new StringBuilder();
    content.append(labels != null ? labels.encode() : "null");
    content.append(comments != null ? comments.encode() : "null");
    content.append(permissions != null ? permissions.encode() : "null");
    content.append(flags != null ? flags.encode() : "null");
    
    final var hash = Hashing.murmur3_128().hashString(content.toString(), StandardCharsets.UTF_8).toString();
    return ImmutableProps.builder()
        .id(hash)
        .propsLabels(labels)
        .propsComments(comments)
        .propsPermissions(permissions)
        .propsFlags(flags);
  }
}