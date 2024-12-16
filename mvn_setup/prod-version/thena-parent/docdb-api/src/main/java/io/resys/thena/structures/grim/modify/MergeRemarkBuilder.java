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

import java.util.Map;

import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.entities.grim.ImmutableGrimRemark;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeRemark;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.ImmutableGrimBatchMissions;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.support.RepoAssert;

public class MergeRemarkBuilder implements MergeRemark {

  private final GrimCommitBuilder logger;
  private final ImmutableGrimBatchMissions.Builder batch;
  private final GrimRemark currentRemark; 
  private final ImmutableGrimRemark.Builder nextRemark;
  private final Map<String, GrimRemark> all_remarks;
  private boolean built;

  public MergeRemarkBuilder(GrimMissionContainer container, GrimCommitBuilder logger, String missionId, String remarkId,
      Map<String, GrimRemark> all_remarks) {
    super();
    this.logger = logger;
    this.batch = ImmutableGrimBatchMissions.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.currentRemark = container.getRemarks().get(remarkId);
    RepoAssert.notNull(currentRemark, () -> "Can't find remark with id: '" + remarkId + "' for mission: '" + missionId + "'!");
    this.nextRemark = ImmutableGrimRemark.builder().from(currentRemark);
    this.all_remarks = all_remarks;
  }
  @Override
  public MergeRemark remarkText(String remarkText) {
    this.nextRemark.remarkText(remarkText);
    return this;
  }
  @Override
  public MergeRemark remarkStatus(String remarkStatus) {
    this.nextRemark.remarkStatus(remarkStatus);
    return this;
  }
  @Override
  public MergeRemark reporterId(String reporterId) {
    this.nextRemark.reporterId(reporterId);
    return this;
  }
  @Override
  public MergeRemark parentId(String parentId) {
    RepoAssert.isTrue(parentId == null || all_remarks.containsKey(parentId), () -> "Can't find parent remark by id: '" +  parentId + "'!");
    this.nextRemark.parentId(parentId);
    return this;
  }
  @Override
  public void build() {
    this.built = true;
  }
  public ImmutableGrimBatchMissions close() {
    RepoAssert.isTrue(built, () -> "you must call MergeRemark.build() to finalize mission MERGE!");
    
    var nextRemark = this.nextRemark.build();
    final var isModified = !nextRemark.equals(currentRemark);
    if(isModified) {
      nextRemark = ImmutableGrimRemark.builder()
          .from(nextRemark)
          .commitId(this.logger.getCommitId())
          .build();
      logger.merge(currentRemark, nextRemark);
      batch.addUpdateRemarks(nextRemark);
    }
    return batch.build();
  }
}
