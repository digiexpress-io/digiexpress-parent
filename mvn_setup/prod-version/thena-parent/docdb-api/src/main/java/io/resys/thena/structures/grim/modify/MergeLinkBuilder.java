package io.resys.thena.structures.grim.modify;

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

import io.resys.thena.api.entities.grim.GrimMissionLink;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionLink;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.MergeLink;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.ImmutableGrimBatchMissions;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;

public class MergeLinkBuilder implements MergeLink {

  private final GrimCommitBuilder logger;
  private final ImmutableGrimBatchMissions.Builder batch;
  private final GrimMissionLink currentLink; 
  private final ImmutableGrimMissionLink.Builder nextLink;
  private boolean built;

  public MergeLinkBuilder(GrimMissionContainer container, GrimCommitBuilder logger, String missionId, String linkId) {
    super();
    this.logger = logger;
    this.batch = ImmutableGrimBatchMissions.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.currentLink = container.getLinks().get(linkId);
    RepoAssert.notNull(currentLink, () -> "Can't find link with id: '" + linkId + "' for mission: '" + missionId + "'!");
    this.nextLink = ImmutableGrimMissionLink.builder().from(currentLink);
  }
  @Override
  public MergeLink linkType(String linkType) {
    RepoAssert.notEmpty(linkType, () -> "linkType must be defined!");
    this.nextLink.linkType(linkType);
    return this;
  }
  @Override
  public MergeLink linkValue(String linkValue) {
    RepoAssert.notEmpty(linkValue, () -> "linkValue must be defined!");
    this.nextLink.externalId(linkValue);
    return this;
  }
  @Override
  public MergeLink linkBody(JsonObject linkBody) {
    this.nextLink.linkBody(linkBody);
    return this;
  }
  @Override
  public void build() {
    this.built = true;
  }
  public ImmutableGrimBatchMissions close() {
    RepoAssert.isTrue(built, () -> "you must call MergeLink.build() to finalize mission MERGE!");
    
    var nextLink = this.nextLink.build();
    final var isModified = !nextLink.equals(currentLink);
    if(isModified) {
      nextLink = ImmutableGrimMissionLink.builder()
          .from(nextLink)
          .commitId(this.logger.getCommitId())
          .build();
      logger.merge(currentLink, nextLink);
      batch.addUpdateLinks(nextLink);
    }
    return batch.build();
  }

}
