package io.resys.thena.structures.fs.actions.modify;

import io.resys.thena.api.entities.BatchStatus;

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

import io.resys.thena.api.entities.fs.FsDirentLink;
import io.resys.thena.api.entities.fs.ImmutableFsDirentLink;
import io.resys.thena.api.entities.fs.ThenaFsContainers.FsDirentContainer;
import io.resys.thena.api.entities.fs.ThenaFsMergeObject;
import io.resys.thena.api.entities.fs.ThenaFsMergeObject.MergeDirentLink;
import io.resys.thena.structures.fs.ImmutableFsBatchDirents;
import io.resys.thena.structures.fs.actions.commitlog.FsCommitBuilder;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;

public class MergeDirentLinkBuilder implements ThenaFsMergeObject.MergeDirentLink {

  private final FsCommitBuilder logger;
  private final ImmutableFsBatchDirents.Builder batch;
  private final FsDirentLink currentLink; 
  private final ImmutableFsDirentLink.Builder nextLink;
  private boolean built;

  public MergeDirentLinkBuilder(FsDirentContainer container, FsCommitBuilder logger, String direntId, String linkId) {
    super();
    this.logger = logger;
    this.batch = ImmutableFsBatchDirents.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.currentLink = container.getLinks().get(linkId);
    RepoAssert.notNull(currentLink, () -> "Can't find link with id: '" + linkId + "' for dirent: '" + direntId + "'!");
    this.nextLink = ImmutableFsDirentLink.builder().from(currentLink);
  }
  @Override
  public MergeDirentLink linkType(String linkType) {
    RepoAssert.notEmpty(linkType, () -> "linkType must be defined!");
    this.nextLink.linkType(linkType);
    return this;
  }
  @Override
  public MergeDirentLink linkValue(String linkValue) {
    RepoAssert.notEmpty(linkValue, () -> "linkValue must be defined!");
    this.nextLink.linkValue(linkValue);
    return this;
  }
  @Override
  public MergeDirentLink linkBody(JsonObject linkBody) {
    this.nextLink.linkBody(linkBody);
    return this;
  }
  @Override
  public void build() {
    this.built = true;
  }
  public ImmutableFsBatchDirents close() {
    RepoAssert.isTrue(built, () -> "you must call MergeLink.build() to finalize dirent MERGE!");
    
    var nextLink = this.nextLink.build();
    final var isModified = !nextLink.equals(currentLink);
    if(isModified) {
      nextLink = ImmutableFsDirentLink.builder()
          .from(nextLink)
          .commitId(this.logger.getCommitId())
          .build();
      logger.merge(currentLink, nextLink);
      batch.addUpdateLinks(nextLink);
    }
    return batch.build();
  }

}
