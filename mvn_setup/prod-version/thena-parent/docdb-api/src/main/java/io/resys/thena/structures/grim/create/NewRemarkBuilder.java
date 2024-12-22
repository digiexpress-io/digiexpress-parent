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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.entities.grim.ImmutableGrimOneOfRelations;
import io.resys.thena.api.entities.grim.ImmutableGrimRemark;
import io.resys.thena.api.entities.grim.ImmutableGrimRemarkTransitives;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewAssignment;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewRemark;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimOneOfRelations;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimRelationType;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.ImmutableGrimBatchMissions;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class NewRemarkBuilder implements ThenaGrimNewObject.NewRemark {
  private final GrimCommitBuilder logger;
  private final @Nullable GrimOneOfRelations relation;
  private final Map<String, GrimRemark> all_remarks;
  private final ImmutableGrimBatchMissions.Builder batch;
  private final String remarkId;
  private final String missionId;
  private ImmutableGrimRemark.Builder next; 
  
  private boolean built;
  
  public NewRemarkBuilder(
      GrimCommitBuilder logger, 
      String missionId, 
      GrimOneOfRelations relation, 
      Map<String, GrimRemark> all_remarks) {
    
    super();
    this.missionId = missionId;
    this.logger = logger;
    this.relation = relation;
    this.all_remarks = all_remarks;
    this.remarkId = OidUtils.gen();
    this.next = ImmutableGrimRemark.builder()
        .id(remarkId)
        .missionId(missionId)
        .createdWithCommitId(logger.getCommitId())
        .commitId(logger.getCommitId())
        .transitives(ImmutableGrimRemarkTransitives.builder()
            .updatedAt(logger.getCreatedAt())
            .createdAt(logger.getCreatedAt())
            .createdBy(logger.getAuthor())
            .build());
    
    this.batch = ImmutableGrimBatchMissions.builder()
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
  public NewRemark remarkText(String remarkText) {
    RepoAssert.notEmpty(remarkText, () -> "remarkText can't be empty!");
    this.next.remarkText(remarkText);
    return this;
  }
  @Override
  public NewRemark remarkStatus(String remarkStatus) {
    this.next.remarkStatus(remarkStatus);
    return this;
  }
  @Override
  public NewRemark reporterId(String reporterId) {
    this.next.reporterId(reporterId);
    return this;
  }
  @Override
  public NewRemark remarkSource(String remarkSource) {
    this.next.remarkSource(remarkSource);
    return this;
  }
  @Override
  public NewRemark remarkType(String remarkType) {
    this.next.remarkType(remarkType);
    return this;
  }
  @Override
  public NewRemark parentId(String parentId) {
    RepoAssert.isTrue(parentId == null || all_remarks.containsKey(parentId), () -> "Can't find parent remark by id: '" +  parentId + "'!");
    this.next.parentId(parentId);
    return this;
  }
  @Override
  public NewRemark addAssignees(Consumer<NewAssignment> assignment) {
    final var all_assignments = this.batch.build().getAssignments().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewAssignmentBuilder(logger, missionId, 
        ImmutableGrimOneOfRelations.builder()
        .remarkId(remarkId)
        .relationType(GrimRelationType.REMARK)
        .build(), all_assignments);
    
    assignment.accept(builder);
    final var built = builder.close();
    this.batch.addAssignments(built);
    return this;
  }

  public ImmutableGrimBatchMissions close() {
    RepoAssert.isTrue(built, () -> "you must call RemarkChanges.build() to finalize mission CREATE or UPDATE!");
    final var built = next.relation(relation).build();
    
    this.logger.add(built);
    return this.batch.addRemarks(built).build();
  }

}
