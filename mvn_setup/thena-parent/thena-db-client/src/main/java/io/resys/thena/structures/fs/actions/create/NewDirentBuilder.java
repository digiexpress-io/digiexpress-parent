package io.resys.thena.structures.fs.actions.create;

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

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.fs.FsDirent.DirentType;
import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.api.entities.fs.ImmutableFsDirent;
import io.resys.thena.api.entities.fs.ImmutableFsDirentContainer;
import io.resys.thena.api.entities.fs.ImmutableFsDirentData;
import io.resys.thena.api.entities.fs.ImmutableFsDirentTransitives;
import io.resys.thena.api.entities.fs.ThenaFsContainers.FsDirentContainer;
import io.resys.thena.api.entities.fs.ThenaFsNewObject;
import io.resys.thena.api.entities.fs.ThenaFsNewObject.NewDirent;
import io.resys.thena.api.entities.fs.ThenaFsNewObject.NewDirentAssignment;
import io.resys.thena.api.entities.fs.ThenaFsNewObject.NewDirentLabel;
import io.resys.thena.api.entities.fs.ThenaFsNewObject.NewDirentLink;
import io.resys.thena.api.entities.fs.ThenaFsNewObject.NewDirentRemark;
import io.resys.thena.structures.fs.ImmutableFsBatchDirents;
import io.resys.thena.structures.fs.actions.commitlog.FsCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;



public class NewDirentBuilder implements ThenaFsNewObject.NewDirent {
  private final FsCommitBuilder logger;
  private final ImmutableFsDirent.Builder dirent;
  private final String direntId;
  private final String commitId;
  private final ImmutableFsDirentData.Builder direntMeta;
  private final OffsetDateTime createdAt;
  private static final String DATE_NUMBER_SEPARATOR_DEFAULT = "-";
  private static final SimpleDateFormat dataFormat = new SimpleDateFormat("yyyyMM");
  
  private ImmutableFsBatchDirents.Builder next;
  private Consumer<FsDirentContainer> handleNewState;
  private String direntUserType;
  
  private boolean built;
 

  public NewDirentBuilder(FsCommitBuilder logger, long nextVal) {
    super();
    this.next = ImmutableFsBatchDirents.builder()
        .tenantId(logger.getTenantId())
        .status(BatchStatus.OK)
        .log("");

    this.createdAt = logger.getCreatedAt();
    this.commitId = logger.getCommitId();
    this.direntId = OidUtils.gen();
    this.dirent = ImmutableFsDirent.builder()
        .id(direntId)
        .commitId(commitId)
        .updatedTreeWithCommitId(commitId)
        .createdWithCommitId(commitId)
        .direntType(DirentType.FILE)
        .direntName("")
        .direntDescription("")
        .direntRef(generateTaskRef(nextVal));
    this.direntMeta = ImmutableFsDirentData.builder()
      .id(OidUtils.gen())
      .createdWithCommitId(logger.getCommitId())
      .commitId(commitId)
      .direntId(direntId);
        
    this.logger = logger;
  }
  
  public String generateTaskRef(long nextVal) {
    final Date now = new Date();
    return dataFormat.format(now) + DATE_NUMBER_SEPARATOR_DEFAULT + nextVal;
  }
  @Override
  public NewDirent direntName(String direntName) {
    this.dirent.direntName(direntName);
    return this;
  }
  @Override
  public NewDirent direntDescription(String direntDescription) {
    this.dirent.direntDescription(direntDescription);
    return this;
  }

  @Override
  public NewDirent externalId(String externalId) {
    this.dirent.externalId(externalId);
    return this;
  }

  @Override
  public NewDirent direntParentId(String direntParentId) {
    this.dirent.direntParentId(direntParentId);
    return this;
  }

  @Override
  public NewDirent direntType(DirentType direntType) {
    this.dirent.direntType(direntType);
    return this;
  }

  @Override
  public NewDirent direntUserType(String direntUserType) {
    this.direntUserType = direntUserType;
    this.dirent.direntUserType(direntUserType);
    return this;
  }
  @Override
  public String getDirentUserType() {
    return direntUserType;
  }
  
  
  @Override
  public NewDirent addAssignees(Consumer<NewDirentAssignment> assignment) {
    final var all_assignments = this.next.build().getAssignments().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewDirentAssignmentBuilder(logger, direntId, all_assignments);
    assignment.accept(builder);
    final var built = builder.close();
    this.next.addAssignments(built);
    return this;
  }
  @Override
  public NewDirent addLabels(Consumer<NewDirentLabel> label) {
    final var all_dirent_label = this.next.build().getLabels().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewDirentLabelBuilder(
        logger, direntId, 
        all_dirent_label
    );
    
    label.accept(builder);
    final var built = builder.close();
    this.next.addLabels(built);
    
    return this;
  }
  @Override
  public NewDirent addLink(Consumer<NewDirentLink> link) {
    final var all_links = this.next.build().getLinks().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewDirentLinkBuilder(logger, direntId, all_links);
    link.accept(builder);
    final var built = builder.close();
    this.next.addLinks(built);
    return this;
  }
  @Override
  public NewDirent addRemark(Consumer<NewDirentRemark> remark) {
    final var all_remarks = this.next.build().getRemarks().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewDirentRemarkBuilder(logger, direntId, Collections.unmodifiableMap(all_remarks));
    remark.accept(builder);
    final var built = builder.close();
    this.next.from(built);
    return this;
  }
  @Override
  public void build() {
    this.built = true;
  }
  @Override
  public NewDirent onNewState(Consumer<FsDirentContainer> handleNewState) {
    this.handleNewState = handleNewState;
    return this;
  }
  public ImmutableFsBatchDirents close() {
    RepoAssert.isTrue(built, () -> "you must call DirentChanges.build() to finalize dirent CREATE or UPDATE!");

    final var data = this.direntMeta.build();
    final var dirent = this.dirent
        .transitives(ImmutableFsDirentTransitives.builder()
            .createdAt(createdAt)
            .updatedAt(createdAt)
            .treeUpdatedAt(createdAt)
            .treeUpdatedBy(logger.getAuthor())
            .dataExtension(Optional.ofNullable(data.getDataExtension()).orElse(null))
            .build())
        .build();
    
    logger.add(dirent);
    
    next.addDirents(dirent);
    if(data.getDataExtension() != null) {
      logger.add(data);
      next.addData(data);
    }
    final var batch = next.build();
    
    onNewState(batch);
    
    return batch;
  }
  
  private void onNewState(ImmutableFsBatchDirents batch) {
    if(handleNewState == null) {
      return;
    }
    final var dirent = batch.getDirents().iterator().next();
    final var builders = ImmutableFsDirentContainer.builder().putDirents(dirent.getId(), dirent);

    batch.getLabels().forEach(label -> builders.putDirentLabels(label.getId(), label));
    batch.getLinks().forEach(link -> builders.putLinks(link.getId(), link));
    batch.getRemarks().forEach(remark -> builders.putRemarks(remark.getId(), remark));
    batch.getData().forEach(data -> builders.putData(data.getId(), data));
    batch.getAssignments().forEach(assignment -> builders.putAssignments(assignment.getId(), assignment));
    batch.getCommits().forEach(commit -> builders.putCommits(commit.getCommitId(), commit));
    final var container = builders.build();
    handleNewState.accept(container);
  }

}
