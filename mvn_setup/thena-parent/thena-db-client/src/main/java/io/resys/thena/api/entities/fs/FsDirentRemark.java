package io.resys.thena.api.entities.fs;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.api.entities.TenantEntity;
import io.resys.thena.api.entities.fs.ThenaFsObject.IsFsObject;
import jakarta.annotation.Nullable;


// user inputed "comment" text that can be connected to the most of entities
@JsonSerialize(as = ImmutableFsDirentRemark.class)
@JsonDeserialize(as = ImmutableFsDirentRemark.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Value.Immutable
public interface FsDirentRemark extends TenantEntity, IsFsObject {
  String getId();
  String getCommitId();
  String getCreatedWithCommitId();
  String getDirentId();
  @Nullable String getParentId();
  @Nullable FsDirentRemarkTransitives getTransitives();
  
  String getRemarkText(); // user inputed free text
  String getReporterId(); // user who inputed it
  @Nullable String getRemarkStatus(); // optional status ie... open/approved, not mandatory, can be empty for most implementation
  @Nullable String getRemarkType();   // optional type ie. internal / external 
  @Nullable String getRemarkSource(); // comment origin ie. frontoffice/backoffice
  
  
  @Override default public FsDocType getDocType() { return FsDocType.FS_DIRENT_REMARK; };
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableFsDirentRemarkTransitives.class)
  @JsonDeserialize(as = ImmutableFsDirentRemarkTransitives.class)
  interface FsDirentRemarkTransitives {
    String getCreatedBy(); // Transitive from commit table
    OffsetDateTime getCreatedAt(); // Transitive from commit table
    OffsetDateTime getUpdatedAt(); // Transitive from commit table
  }
}
