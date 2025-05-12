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
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;


@JsonSerialize(as = ImmutableFsDirentLink.class)
@JsonDeserialize(as = ImmutableFsDirentLink.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Value.Immutable
public interface FsDirentLink extends TenantEntity, IsFsObject {
  String getId();
  String getCommitId();
  String getCreatedWithCommitId();
  String getDirentId();
  String getLinkValue();
  String getLinkType();
  
  @Nullable JsonObject getLinkBody();
  @Nullable FsDirentLinkTransitives getTransitives();
  
  
  @Override default public FsDocType getDocType() { return FsDocType.FS_DIRENT_LINKS; };
  
  @JsonSerialize(as = ImmutableFsDirentLinkTransitives.class)
  @JsonDeserialize(as = ImmutableFsDirentLinkTransitives.class)
  @Value.Immutable
  interface FsDirentLinkTransitives {
    OffsetDateTime getCreatedAt(); // Transitive from commit table
    OffsetDateTime getUpdatedAt(); // Transitive from commit table
  }
}
