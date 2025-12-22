package io.resys.thena.structures.fs.actions.modify;

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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;

import io.resys.thena.api.entities.fs.FsDirent.DirentType;
import io.resys.thena.api.entities.fs.FsDirentAssignment;
import io.resys.thena.api.entities.fs.FsDirentData;
import io.resys.thena.api.entities.fs.FsDirentLabel;
import io.resys.thena.api.entities.fs.FsDirentLink;
import io.resys.thena.api.entities.fs.FsDirentRemark;
import io.resys.thena.api.entities.fs.ImmutableFsDirent;
import io.resys.thena.api.entities.fs.ImmutableFsDirentData;
import io.resys.thena.api.entities.fs.ImmutableFsDirentTransitives;
import io.resys.thena.api.entities.fs.ThenaFsContainers.FsDirentContainer;
import io.resys.thena.api.entities.fs.ThenaFsMergeObject;
import io.resys.thena.api.entities.fs.ThenaFsMergeObject.MergeDirent;
import io.resys.thena.api.entities.fs.ThenaFsMergeObject.MergeDirentLink;
import io.resys.thena.api.entities.fs.ThenaFsMergeObject.MergeDirentRemark;
import io.resys.thena.api.entities.fs.ThenaFsNewObject.NewDirentAssignment;
import io.resys.thena.api.entities.fs.ThenaFsNewObject.NewDirentLabel;
import io.resys.thena.api.entities.fs.ThenaFsNewObject.NewDirentLink;
import io.resys.thena.api.entities.fs.ThenaFsNewObject.NewDirentRemark;
import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.structures.fs.ImmutableFsBatchDirents;
import io.resys.thena.structures.fs.actions.commitlog.FsCommitBuilder;
import io.resys.thena.structures.fs.actions.create.NewDirentAssignmentBuilder;
import io.resys.thena.structures.fs.actions.create.NewDirentLabelBuilder;
import io.resys.thena.structures.fs.actions.create.NewDirentLinkBuilder;
import io.resys.thena.structures.fs.actions.create.NewDirentRemarkBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;

public class MergeDirentBuilder implements ThenaFsMergeObject.MergeDirent {
  
  private final FsDirentContainer container;
  private final FsCommitBuilder logger;
  private final ImmutableFsBatchDirents.Builder batch;
  private final ImmutableFsDirent.Builder nextDirent;
  private final ImmutableFsDirentData.Builder nextDirentMeta;
  private final ImmutableFsDirentTransitives.Builder nextTransitives;
  private final String direntId;
  private Consumer<FsDirentContainer> handleCurrentState;
  private boolean built;
  
  public MergeDirentBuilder(FsDirentContainer container, FsCommitBuilder logger) {
    super();
    final var start = container.getDirents().values().iterator().next();
    this.nextTransitives = ImmutableFsDirentTransitives.builder()
        .from(start.getTransitives());
    
    this.container = container;
    this.logger = logger;
    this.batch = ImmutableFsBatchDirents.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.nextDirent = ImmutableFsDirent.builder().from(start);
    this.direntId = container.getDirents().values().iterator().next().getId();
    this.nextDirentMeta = ImmutableFsDirentData.builder()
        .from(container
            .getData().values().stream()
            .findFirst().orElseGet(() -> {
              
              final FsDirentData data = ImmutableFsDirentData.builder()
                  .id(OidUtils.gen())
                  .createdWithCommitId(logger.getCommitId())
                  .commitId(logger.getCommitId())
                  .direntId(direntId).build();
              return data;
            }));
  }

  @Override
  public ThenaFsMergeObject.MergeDirent direntName(String direntName) {
    this.nextDirent.direntName(direntName);
    return this;
  }
  @Override
  public ThenaFsMergeObject.MergeDirent direntDescription(String direntDescription) {
    this.nextDirent.direntDescription(direntDescription);
    return this;
  }

  @Override
  public ThenaFsMergeObject.MergeDirent externalId(String externalId) {
    this.nextDirent.externalId(externalId);
    return this;
  }

  @Override
  public ThenaFsMergeObject.MergeDirent direntParentId(String direntParentId) {
    this.nextDirent.direntParentId(direntParentId);
    return this;
  }

  @Override
  public ThenaFsMergeObject.MergeDirent direntType(DirentType direntType) {
    this.nextDirent.direntType(direntType);
    return this;
  }

  @Override
  public ThenaFsMergeObject.MergeDirent direntUserType(String direntUserType) {
    this.nextDirent.direntUserType(direntUserType);
    return this;
  }
  @Override
  public ThenaFsMergeObject.MergeDirent archivedAt(OffsetDateTime archivedAt) {
    this.nextDirent.archivedAt(archivedAt);
    return this;
  }
  @Override
  public <T> MergeDirent setAllAssignees(String assigneeType, List<T> replacments, Function<T, Consumer<NewDirentAssignment>> callbacks) {
    
    
    // clear old
    final var intermed = this.batch.build()
        .getAssignments().stream()
        .filter(a -> !a.getAssignmentType().equals(assigneeType))

        .toList();
    this.batch.assignments(intermed);
    final var all_assignments = new HashMap<String, FsDirentAssignment>();
    
    // delete old
    final var toBeDeleted = new ArrayList<>(container.getAssignments().values().stream()
        .filter(e -> e.getAssignmentType().equals(assigneeType))
        .map(e -> {
          logger.rm(e);
          return e;
        })
        .toList());


    
    // add new
    for(final var replacement : replacments) {
      final var assignment = callbacks.apply(replacement);
      
      final var builder = new NewDirentAssignmentBuilder(logger, direntId, Collections.unmodifiableMap(all_assignments));
      assignment.accept(builder);
      final var built = builder.close();
      
      // previous version exists and is exactly the same
      final var previous = toBeDeleted.stream()
          .filter(a -> a.getAssignmentType().equals(built.getAssignmentType()))
          .filter(a -> a.getAssignee().equals(built.getAssignee()))
          .filter(a -> Objects.equals(a.getAssigneeContact(), built.getAssigneeContact()))
          
          .findFirst();
      
      if(previous.isPresent()) {
        toBeDeleted.remove(previous.get());
      } else {
        all_assignments.put(built.getId(), built);
        this.batch.addAssignments(built);        
      }
    }
    
    this.batch.addAllDeleteAssignments(toBeDeleted);
    
    
    updateVersion();
    return this;
  }

  @Override
  public <T> MergeDirent setAllLabels(String labelType, List<T> replacments, Function<T, Consumer<NewDirentLabel>> callbacks) {
    // clear old
    final var intermed = this.batch.build()
        .getLabels().stream()
        .filter(a -> !a.getLabelType().equals(labelType))
        .toList();
    this.batch.labels(intermed);
    final var all_dirent_label = new HashMap<String, FsDirentLabel>();
    
    // delete old
    this.batch.addAllDeleteDirentLabels(container.getDirentLabels().values().stream()
        .filter(e -> e.getLabelType().equals(labelType))
        .sorted((a, b) -> ComparisonChain.start()
            .compare(a.getLabelType(), b.getLabelType())
            .compare(a.getLabelValue(), b.getLabelValue())
            .result())
        
        .map(e -> {
          logger.rm(e);
          return e;
        })
        .toList());
    
    // add new
    for(final var replacement : replacments) {
      final var label = callbacks.apply(replacement);
      
      final var builder = new NewDirentLabelBuilder(logger, direntId, all_dirent_label);
      label.accept(builder);

      final var built = builder.close();
      all_dirent_label.put(built.getId(), built);
      this.batch.addLabels(built);

    }
    
    updateVersion();
    return this;
  }

  @Override
  public <T> MergeDirent setAllLinks(String linkType, List<T> replacments, Function<T, Consumer<NewDirentLink>> callbacks) {
    // clear old
    final var intermed = this.batch.build()
        .getLinks().stream()
        .filter(a -> !a.getLinkType().equals(linkType))
        .toList();
    this.batch.links(intermed);
    final var all_links = new HashMap<String, FsDirentLink>();
    
    // delete old
    this.batch.addAllDeleteLinks(container.getLinks().values().stream()
        .filter(a -> a.getLinkType().equals(linkType))
        .map(e -> {
          logger.rm(e);
          return e;
        })
        .toList());
    
    // add new
    for(final var replacement : replacments) {
      final var link = callbacks.apply(replacement);
      
      final var builder = new NewDirentLinkBuilder(logger, direntId, Collections.unmodifiableMap(all_links));
      link.accept(builder);

      final var built = builder.close();
      all_links.put(built.getId(), built);
      this.batch.addLinks(built);
    }
    
    updateVersion();
    return this;
  }

  @Override
  public MergeDirent addAssignees(Consumer<NewDirentAssignment> assignment) {
    final var all_assignments = this.batch.build().getAssignments().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewDirentAssignmentBuilder(logger, direntId, all_assignments);
    assignment.accept(builder);
    final var built = builder.close();
    this.batch.addAssignments(built);
    
    updateVersion();
    return this;
  }

  @Override
  public MergeDirent addLabels(Consumer<NewDirentLabel> label) {
    final var all_dirent_label = this.batch.build().getLabels().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewDirentLabelBuilder(
        logger, direntId,
        all_dirent_label
    );
    
    label.accept(builder);
    final var built = builder.close();
    this.batch.addLabels(built);
    
    updateVersion();
    return this;
  }

  @Override
  public MergeDirent addLink(Consumer<NewDirentLink> link) {
    final var all_links = this.batch.build().getLinks().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewDirentLinkBuilder(logger, direntId, all_links);
    link.accept(builder);
    final var built = builder.close();
    this.batch.addLinks(built);
    
    updateVersion();
    return this;
  }

  @Override
  public MergeDirent addRemark(Consumer<NewDirentRemark> remark) {
    final var current_remarks = this.batch.build().getRemarks().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var all_remarks = ImmutableMap.<String, FsDirentRemark>builder()
        .putAll(this.container.getRemarks().values().stream()
            .filter(e -> !current_remarks.containsKey(e.getId()))
            .collect(Collectors.toMap(e -> e.getId(), e -> e)))
        .putAll(current_remarks)
        .build();
    
    final var builder = new NewDirentRemarkBuilder(logger, direntId, Collections.unmodifiableMap(all_remarks));
    remark.accept(builder);
    final var built = builder.close();
    this.batch.from(built);
    
    updateVersion();
    return this;
  }
  @Override
  public MergeDirent modifyRemark(String remarkId, Consumer<MergeDirentRemark> mergeRemark) {
    final var builder = new MergeDirentRemarkBuilder(container, logger, direntId, remarkId, container.getRemarks());
    mergeRemark.accept(builder);
    final var built = builder.close();
    this.batch.from(built);
    
    updateVersion();
    
    return this;
  }
  @Override
  public MergeDirent modifyLink(String linkId, Consumer<MergeDirentLink> mergeLink) {
    final var builder = new MergeDirentLinkBuilder(container, logger, direntId, linkId);
    mergeLink.accept(builder);
    final var built = builder.close();
    this.batch.from(built);
    
    updateVersion();
    
    return this;
  }

  @Override
  public MergeDirent removeRemark(String remarkId) {
    final var currentRemark = container.getRemarks().get(remarkId);
    RepoAssert.notNull(currentRemark, () -> "Can't find remark with id: '" + remarkId + "' for dirent: '" + direntId + "'!");


    updateVersion();
    this.logger.rm(currentRemark);
    this.batch.addDeleteRemarks(currentRemark);
    this.container.getLinks().values().stream()
    .forEach(link -> {
      this.logger.rm(link);
      this.batch.addDeleteLinks(link);
    });
    this.container.getDirentLabels().values().stream()
    .forEach(label -> {
      this.logger.rm(label);
      this.batch.addDeleteDirentLabels(label);
    });
    this.container.getData().values().stream()
    .forEach(data -> {
      this.logger.rm(data);
      this.batch.addDeleteData(data);
    });
    this.container.getAssignments().values().stream()
    .forEach(data -> {
      this.logger.rm(data);
      this.batch.addDeleteAssignments(data);
    });
    return this;
  }
  
  @Override
  public MergeDirent onCurrentState(Consumer<FsDirentContainer> handleCurrentState) {
    this.handleCurrentState = handleCurrentState;
    return this;
  }
  @Override
  public void build() {
    this.built = true;
  }
  public ImmutableFsBatchDirents close() {
    RepoAssert.isTrue(built, () -> "you must call MergeDirent.build() to finalize dirent CREATE or UPDATE!");

    if(this.handleCurrentState != null) {
      this.handleCurrentState.accept(container);
    }
    
    
    
    // dirent meta merge
    {
      var data = this.nextDirentMeta.build();
      final var previous = this.container.getData().get(data.getId());
      final var isModified = previous != null && !data.equals(previous) && data.getDataExtension() != null;
      final var isInsert = previous == null && data.getDataExtension() != null;
      final var isDelete = previous != null && data.getDataExtension() == null;
      
      if(isDelete) {
        logger.rm(previous);
        batch.addDeleteData(previous);
        updateVersion();
        nextTransitives.dataExtension(null);
      } if(isModified) {
        data = ImmutableFsDirentData.builder()
            .from(data)
            .commitId(this.logger.getCommitId())
            .build();
        updateVersion();
        logger.merge(previous, data);
        batch.addUpdateData(data);
        
        nextTransitives.dataExtension(data.getDataExtension());
      } else if(isInsert) {
        logger.add(data);
        batch.addData(data);
        updateVersion();
        nextTransitives.dataExtension(data.getDataExtension());
      }
      
    }
    
    // dirent merge
    {
      var dirent = this.nextDirent.transitives(nextTransitives.build()).build();
      final var previous = this.container.getDirents().get(dirent.getId());
      final var isModified = !dirent.equals(previous);
      
      if(isModified) {
        updateVersion();
        dirent = ImmutableFsDirent.builder()
            .from(dirent)
            .commitId(this.logger.getCommitId())
            .updatedTreeWithCommitId(this.logger.getCommitId())
            .transitives(nextTransitives.build())
            .build();
        logger.merge(previous, dirent);
        batch.addUpdateDirents(dirent);
      }
    }
    
    return batch.build();
  }
  
  
  private void updateVersion() {
    // forces version change on the dirent
    nextTransitives
      .treeUpdatedAt(logger.getCreatedAt())
      .treeUpdatedBy(logger.getAuthor());
  }
  
  
  @Override
  public FsDirentContainer getCurrentState() {
    return container;
  }
}
