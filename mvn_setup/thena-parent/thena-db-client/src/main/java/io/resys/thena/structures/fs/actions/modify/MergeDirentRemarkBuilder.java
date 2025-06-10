package io.resys.thena.structures.fs.actions.modify;

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

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.api.entities.fs.FsDirentRemark;
import io.resys.thena.api.entities.fs.ImmutableFsDirentRemark;
import io.resys.thena.api.entities.fs.ThenaFsContainers.FsDirentContainer;
import io.resys.thena.api.entities.fs.ThenaFsMergeObject;
import io.resys.thena.structures.fs.ImmutableFsBatchDirents;
import io.resys.thena.structures.fs.actions.commitlog.FsCommitBuilder;
import io.resys.thena.support.RepoAssert;

public class MergeDirentRemarkBuilder implements ThenaFsMergeObject.MergeDirentRemark {

  private final FsCommitBuilder logger;
  private final ImmutableFsBatchDirents.Builder batch;
  private final FsDirentRemark currentRemark; 
  private final ImmutableFsDirentRemark.Builder nextRemark;
  private final Map<String, FsDirentRemark> all_remarks;
  private boolean built;

  public MergeDirentRemarkBuilder(FsDirentContainer container, FsCommitBuilder logger, String direntId, String remarkId,
      Map<String, FsDirentRemark> all_remarks) {
    super();
    this.logger = logger;
    this.batch = ImmutableFsBatchDirents.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.currentRemark = container.getRemarks().get(remarkId);
    RepoAssert.notNull(currentRemark, () -> "Can't find remark with id: '" + remarkId + "' for dirent: '" + direntId + "'!");
    this.nextRemark = ImmutableFsDirentRemark.builder().from(currentRemark);
    this.all_remarks = all_remarks;
  }
  @Override
  public ThenaFsMergeObject.MergeDirentRemark remarkText(String remarkText) {
    this.nextRemark.remarkText(remarkText);
    return this;
  }
  @Override
  public ThenaFsMergeObject.MergeDirentRemark remarkStatus(String remarkStatus) {
    this.nextRemark.remarkStatus(remarkStatus);
    return this;
  }
  @Override
  public ThenaFsMergeObject.MergeDirentRemark reporterId(String reporterId) {
    this.nextRemark.reporterId(reporterId);
    return this;
  }
  @Override
  public ThenaFsMergeObject.MergeDirentRemark parentId(String parentId) {
    RepoAssert.isTrue(parentId == null || all_remarks.containsKey(parentId), () -> "Can't find parent remark by id: '" +  parentId + "'!");
    this.nextRemark.parentId(parentId);
    return this;
  }
  @Override
  public void build() {
    this.built = true;
  }
  public ImmutableFsBatchDirents close() {
    RepoAssert.isTrue(built, () -> "you must call MergeRemark.build() to finalize dirent MERGE!");
    
    var nextRemark = this.nextRemark.build();
    final var isModified = !nextRemark.equals(currentRemark);
    if(isModified) {
      nextRemark = ImmutableFsDirentRemark.builder()
          .from(nextRemark)
          .commitId(this.logger.getCommitId())
          .build();
      logger.merge(currentRemark, nextRemark);
      batch.addUpdateRemarks(nextRemark);
    }
    return batch.build();
  }
}
