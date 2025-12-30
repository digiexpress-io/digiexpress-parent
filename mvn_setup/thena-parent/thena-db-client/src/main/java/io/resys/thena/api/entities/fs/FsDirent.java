package io.resys.thena.api.entities.fs;

import java.beans.Transient;

/*-
 * #%L
 * thena-db-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.api.entities.AnyTenantEntity;
import io.resys.thena.api.entities.fs.ThenaFsObject.IsFsObject;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;



@JsonSerialize(as = ImmutableFsDirent.class)
@JsonDeserialize(as = ImmutableFsDirent.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Value.Immutable
public interface FsDirent extends AnyTenantEntity, IsFsObject {

  String getId(); // internal GID
  String getCommitId();
  String getCreatedWithCommitId();
  @Nullable String getUpdatedTreeWithCommitId();
  String getExternalId();
  
  
  @Nullable OffsetDateTime getArchivedAt(); // when we archived it
  @Nullable String getArchivedStatus(); // on the way to be archived or is already archived

  
  @Nullable String getDirentParentId();
  String getDirentRef(); // user friendly id
  DirentType getDirentType();
  String getDirentName();
  String getDirentDescription();
  @Nullable String getDirentUserType(); // user defined optional type
  
  @Transient @JsonIgnore
  @Nullable FsDirentTransitives getTransitives();
  
  @Override default public FsDocType getDocType() { return FsDocType.FS_DIRENT; };

  
  enum DirentType { FOLDER, FILE }
  
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableFsDirentTransitives.class)
  @JsonDeserialize(as = ImmutableFsDirentTransitives.class)
  interface FsDirentTransitives {
    OffsetDateTime getCreatedAt(); // Transitive from commit table
    OffsetDateTime getTreeUpdatedAt(); // Transitive from commit table
    String getTreeUpdatedBy(); // Transitive from commit table
    @Nullable OffsetDateTime getUpdatedAt(); // Transitive from commit table
    @Nullable JsonObject getDataExtension();
  }
}
