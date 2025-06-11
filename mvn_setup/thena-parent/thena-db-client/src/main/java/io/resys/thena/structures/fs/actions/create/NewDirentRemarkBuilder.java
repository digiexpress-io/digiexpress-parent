package io.resys.thena.structures.fs.actions.create;

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
import io.resys.thena.api.entities.fs.ImmutableFsDirentRemarkTransitives;
import io.resys.thena.api.entities.fs.ThenaFsNewObject;
import io.resys.thena.structures.fs.ImmutableFsBatchDirents;
import io.resys.thena.structures.fs.actions.commitlog.FsCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;

public class NewDirentRemarkBuilder implements ThenaFsNewObject.NewDirentRemark {
  private final FsCommitBuilder logger;
  private final Map<String, FsDirentRemark> all_remarks;
  private final ImmutableFsBatchDirents.Builder batch;
  private final String remarkId;
  private final String direntId;
  private ImmutableFsDirentRemark.Builder next; 
  
  private boolean built;
  
  public NewDirentRemarkBuilder(
      FsCommitBuilder logger, 
      String direntId, 
      Map<String, FsDirentRemark> all_remarks) {
    
    super();
    this.direntId = direntId;
    this.logger = logger;
    this.all_remarks = all_remarks;
    this.remarkId = OidUtils.gen();
    this.next = ImmutableFsDirentRemark.builder()
        .id(remarkId)
        .direntId(this.direntId)
        .createdWithCommitId(logger.getCommitId())
        .commitId(logger.getCommitId())
        .transitives(ImmutableFsDirentRemarkTransitives.builder()
            .updatedAt(logger.getCreatedAt())
            .createdAt(logger.getCreatedAt())
            .createdBy(logger.getAuthor())
            .build());
    
    this.batch = ImmutableFsBatchDirents.builder()
        .tenantId(logger.getTenantId())
        .status(BatchStatus.OK)
        .log("");
  }
  
  @Override
  public String build() {
    this.built = true;
    return this.remarkId;
  }
  @Override
  public ThenaFsNewObject.NewDirentRemark remarkText(String remarkText) {
    RepoAssert.notEmpty(remarkText, () -> "remarkText can't be empty!");
    this.next.remarkText(remarkText);
    return this;
  }
  @Override
  public ThenaFsNewObject.NewDirentRemark remarkStatus(String remarkStatus) {
    this.next.remarkStatus(remarkStatus);
    return this;
  }
  @Override
  public ThenaFsNewObject.NewDirentRemark reporterId(String reporterId) {
    this.next.reporterId(reporterId);
    return this;
  }
  @Override
  public ThenaFsNewObject.NewDirentRemark remarkSource(String remarkSource) {
    this.next.remarkSource(remarkSource);
    return this;
  }
  @Override
  public ThenaFsNewObject.NewDirentRemark remarkType(String remarkType) {
    this.next.remarkType(remarkType);
    return this;
  }
  @Override
  public ThenaFsNewObject.NewDirentRemark parentId(String parentId) {
    RepoAssert.isTrue(parentId == null || all_remarks.containsKey(parentId), () -> "Can't find parent remark by id: '" +  parentId + "'!");
    this.next.parentId(parentId);
    return this;
  }
  public ImmutableFsBatchDirents close() {
    RepoAssert.isTrue(built, () -> "you must call RemarkChanges.build() to finalize dirent CREATE or UPDATE!");
    final var built = next.build();
    
    this.logger.add(built);
    return this.batch.addRemarks(built).build();
  }

}
