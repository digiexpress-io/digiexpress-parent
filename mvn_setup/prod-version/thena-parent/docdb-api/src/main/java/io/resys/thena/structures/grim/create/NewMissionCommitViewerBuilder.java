package io.resys.thena.structures.grim.create;

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

import io.resys.thena.api.entities.grim.ImmutableGrimCommitViewer;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewMissionCommitViewer;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NewMissionCommitViewerBuilder implements NewMissionCommitViewer {
  private final OffsetDateTime createdAt;
  private final String missionId;
  private final String commitId;
  
  private boolean built;
  private String userId;
  private String usedFor;
  
  @Override
  public NewMissionCommitViewer userId(String userId) {
    this.userId = userId;
    return this;
  }
  @Override
  public NewMissionCommitViewer usedFor(String usedFor) {
    this.usedFor = usedFor;
    return this;
  }
  @Override
  public void build() {
    this.built = true;
  }
  
  public ImmutableGrimCommitViewer close() {
    RepoAssert.isTrue(built, () -> "you must call MissionChanges.build() to finalize mission CREATE or UPDATE!");
    RepoAssert.notEmpty(userId, () -> "userId must be defined!");
    RepoAssert.notEmpty(usedFor, () -> "usedFor  must be defined!");
    
    return ImmutableGrimCommitViewer.builder()
        .id(OidUtils.gen())
        .missionId(missionId)
        .commitId(commitId)
        .createdAt(createdAt)
        .updatedAt(createdAt)
        .objectId(missionId)
        .objectType(GrimDocType.GRIM_MISSION)
        .usedBy(userId)
        .usedFor(usedFor)
        .build();
  }
}
