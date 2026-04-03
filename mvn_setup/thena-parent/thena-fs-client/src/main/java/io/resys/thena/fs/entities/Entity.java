package io.resys.thena.fs.entities;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.f4b6a3.uuid.UuidCreator;
import com.google.common.hash.Hashing;

import io.resys.thena.support.TableUtils;
import jakarta.annotation.Nullable;

public interface Entity {
  UUID getId();
  
  @JsonIgnore
  FileSystemEntityType getDocType();
  
  
  enum FileSystemEntityType {
    BLOB,
    TREE,
    PROPS,
    COMMIT,
    REF,
    TAG,
    NODE,
    INDEX
  }
  
  public static final UUID EMPTY_UUID = Entity.uuid().build();
  
  public static UUIDBuilder uuid() {
    return new UUIDBuilder();
  }
  public static UUID genUUID() {
    return UuidCreator.getTimeOrderedEpoch();
  }
  
  /**
   * Safely converts a UUID string to a UUID object, returning null for null or incorrect input.
   * 
   * @param uuidString the UUID string to convert (can be null)
   * @return UUID object or null if input is null or can't be converted
   */
  public static @Nullable UUID toUuidOrNull(String uuidString) {
    try {
      return uuidString == null ? null : TableUtils.toUuid(uuidString);
    } catch(Exception e) {
      return null;
    }
  }
  
  public static @Nullable UUID toUuid(String uuidString) {
    return uuidString == null ? null : TableUtils.toUuid(uuidString);
  }

  public static class UUIDBuilder {
    private final StringBuilder content = new StringBuilder();
    public UUIDBuilder append(String append) {
      content.append(append);
      return this;
    }
    public UUIDBuilder append(UUID append) {
      content.append(append);
      return this;
    }
    public UUIDBuilder append(OffsetDateTime append) {
      content.append(append);
      return this;
    }    
    public UUIDBuilder append(Optional<UUID> append) {
      if(append.isPresent()) {
        return append(append.get());
      }
      return append("");
    }
    public UUIDBuilder append(long append) {
      content.append(append);
      return this;
    }
    public UUID build() {
      final var hash = Hashing.murmur3_128().hashString(content.toString(), StandardCharsets.UTF_8).toString();
      return TableUtils.toUuid(hash);
    }
  }
}
