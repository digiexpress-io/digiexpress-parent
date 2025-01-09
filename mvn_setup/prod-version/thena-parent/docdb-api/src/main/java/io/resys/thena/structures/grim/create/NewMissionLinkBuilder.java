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

import java.util.Map;

import io.resys.thena.api.entities.grim.GrimMissionLink;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionLink;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionLinkTransitives;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewLink;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimOneOfRelations;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public class NewMissionLinkBuilder implements ThenaGrimNewObject.NewLink {
  private final GrimCommitBuilder logger;
  private final @Nullable GrimOneOfRelations relation;
  private final Map<String, GrimMissionLink> all_missionLinks;
  private ImmutableGrimMissionLink.Builder next; 
  private boolean built;
  
  public NewMissionLinkBuilder(
      GrimCommitBuilder logger, 
      String missionId, 
      GrimOneOfRelations relation, 
      Map<String, GrimMissionLink> all_missionLinks) {
    
    super();
    this.logger = logger;
    this.relation = relation;
    this.next = ImmutableGrimMissionLink.builder()
        .missionId(missionId)
        .commitId(logger.getCommitId())
        .createdWithCommitId(logger.getCommitId())
        .transitives(ImmutableGrimMissionLinkTransitives.builder()
            .updatedAt(logger.getCreatedAt())
            .createdAt(logger.getCreatedAt())
            .build())
        .id(OidUtils.gen());
    this.all_missionLinks = all_missionLinks;
  }
  
  @Override
  public void build() {
    this.built = true;
  }
  
  @Override
  public NewLink linkType(String linkType) {
    this.next.linkType(linkType);
    return this;
  }
  @Override
  public NewLink linkValue(String linkValue) {
    this.next.externalId(linkValue);
    return this;
  }
  @Override
  public NewLink linkBody(JsonObject linkBody) {
    this.next.linkBody(linkBody);
    return this;
  }
  public ImmutableGrimMissionLink close() {
    RepoAssert.isTrue(built, () -> "you must call LabelChanges.build() to finalize mission CREATE or UPDATE!");
    
    final var built = next.relation(relation).build();
    
    RepoAssert.isTrue(
        this.all_missionLinks.values().stream()
        .filter(a -> 
          (a.getRelation() == null && relation == null) ||
          (a.getRelation() != null && a.getRelation().equals(relation))
        )
        .filter(a -> 
          a.getLinkType().equals(built.getLinkType()) &&
          a.getExternalId().equals(built.getExternalId())
        )
        .count() == 0
        , () -> "can't have duplicate link of type: " + built.getLinkType() + ", with value: " + built.getExternalId() + "!");
    
    this.logger.add(built);
    return built;
  }
}
