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

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableTag.class)
@JsonDeserialize(as = ImmutableTag.class)
public interface Tag extends Entity {
  
  String getId();
  Optional<String> getRefId();
  Optional<String> getExternalId();
  String getCommitId();

  OffsetDateTime getTagCreatedAt();
  Optional<OffsetDateTime> getTagStartsAt();
  Optional<OffsetDateTime> getTagEndsAt();
  
  String getTagName();
  Optional<String> getTagDescription();
  String getTagAuthor();

  Optional<String> getTagHealth();
  Optional<JsonObject> getTagErrors();

  Optional<JsonObject> getTagExtension();
  Optional<JsonObject> getTagReport();

  Optional<String> getTagLifecycle();

  
  @Value.Auxiliary
  @Nullable 
  TagTransitives getTransitives();

  @Override
  default FileSystemEntityType getDocType() { 
    return FileSystemEntityType.TAG; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableTagTransitives.class)
  @JsonDeserialize(as = ImmutableTagTransitives.class)
  interface TagTransitives {
    // might not be loaded
    @Nullable Commit getCommit();
    
    // might not be loaded
    @Nullable Tree getTree();
  }
  
  default Tag withTransitives(Commit commit, Tree tree) {
    return ImmutableTag.builder().from(this)
        .transitives(ImmutableTagTransitives.builder()
            .commit(commit)
            .tree(tree)
            .build())
        .build();
  }
}
