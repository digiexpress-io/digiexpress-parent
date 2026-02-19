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

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableObjectIndex.class)
@JsonDeserialize(as = ImmutableObjectIndex.class)
public interface ObjectIndex extends FileSystemEntity {
  String getObjectId();
  String getCreatedBy();
  String getUpdatedBy();
  OffsetDateTime getCreatedAt();
  OffsetDateTime getUpdatedAt();
  
  @Override
  default String getId() {
    return getObjectId();
  }
  
  @Override
  default FileSystemEntityType getDocType() { 
    return FileSystemEntityType.INDEX; 
  }
}
