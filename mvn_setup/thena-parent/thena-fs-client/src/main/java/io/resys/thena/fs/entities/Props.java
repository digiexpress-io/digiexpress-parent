package io.resys.thena.fs.entities;

/*-
 * #%L
 * thena-fs-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableProps.class)
@JsonDeserialize(as = ImmutableProps.class)
public interface Props extends Entity {
  
  UUID getId();
  Optional<String> getPropsDescription();
  Optional<JsonObject> getPropsLabels();
  Optional<JsonObject> getPropsComments();
  Optional<JsonObject> getPropsPermissions();
  Optional<JsonObject> getPropsFlags();

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
  
  
  // H(props) = μ(props_description ⊕ props_labels ⊕ props_comments ⊕ props_permissions ⊕ props_flags)
  public static ImmutableProps.Builder newInstance(
      String description,
      JsonObject labels, 
      JsonObject comments, 
      JsonObject permissions, 
      JsonObject flags
    ) {
    final var content = Entity.uuid();
    content.append(description != null ? description : "null");
    content.append(labels != null ? Blob.canonicalizeJson(labels) : "null");
    content.append(comments != null ? Blob.canonicalizeJson(comments) : "null");
    content.append(permissions != null ? Blob.canonicalizeJson(permissions) : "null");
    content.append(flags != null ? Blob.canonicalizeJson(flags) : "null");
    
    return ImmutableProps.builder()
        .id(content.build())
        .propsDescription(Optional.ofNullable(description))
        .propsLabels(Optional.ofNullable(labels))
        .propsComments(Optional.ofNullable(comments))
        .propsPermissions(Optional.ofNullable(permissions))
        .propsFlags(Optional.ofNullable(flags));
  }
}
