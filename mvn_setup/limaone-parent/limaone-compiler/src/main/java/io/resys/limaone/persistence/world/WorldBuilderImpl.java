package io.resys.limaone.persistence.world;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.persistence.ModelWorldDb.NextWorld;
import io.resys.limaone.persistence.ModelWorldDb.WorldBuilder;
import io.resys.limaone.persistence.world.ModelWorldDb_FS.WorldLockException;
import io.resys.limaone.persistence.world.NextWorldImpl.NextWorldResult;
import io.resys.thena.fs.api.FileSystem;
import io.resys.thena.fs.api.FileSystem.FileSystemTenant;
import io.resys.thena.fs.api.commits.CommitBuilder;
import io.resys.thena.fs.api.tags.TagBuilder;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.entities.Tag.TagTransitives;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class WorldBuilderImpl implements WorldBuilder {
  private final FileSystem fileSystem;
  private final WorldPersistenceLogger logger = new WorldPersistenceLogger();
  private final List<String> blobTypes = new ArrayList<>();
  private final List<String> docIds = new ArrayList<>();
  
  public static String branchName = "main";
  private UUID commitId;
  private String author;
  private OffsetDateTime createdAt;
  
  @Override public WorldBuilder author(String author) { this.author = author; return this; }
  @Override public WorldBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
  @Override public WorldBuilder lockWithCommit(UUID commitId) { this.commitId = commitId; return this; }
  @Override public WorldBuilder docs(BodyType... type) { this.blobTypes.addAll(Arrays.asList(type).stream().map(e -> e.name()).toList()); return this; }
  
  @Override
  public WorldBuilder docsId(String... objectId) {
    this.docIds.addAll(Arrays.asList(objectId));
    return this;
  }
  
  @Override
  public <T> Uni<T> build(Function<NextWorld, T> mergeFunction) {
    Objects.requireNonNull(author, () -> "author must be provided");
    Objects.requireNonNull(createdAt, () -> "createdAt must be provided");
    
    final var tenant = fileSystem.withTenant();
    logger.stage1TenantConfigured(tenant, commitId);
    return tenant.branchQuery()
      .branchId(branchName)
      .docIds(docIds)
      .findOne()

      .onItem().transformToUni(ref -> {
        
        logger.stage2CurrentState(ref);
        
        // Ref must be present if commit is present
        if(commitId != null && !ref.get().getCommitId().equals(commitId)) {
          logger.stage3LockFailed(ref);
          throw new WorldLockException();
        }
        final var commitBuilder = tenant.commitBuilder();
        final var nextWorld = new NextWorldImpl(commitBuilder, ref, author, createdAt);
        final var mapped = mergeFunction.apply(nextWorld);
        final var nextWorldResult = nextWorld.close();
        
        logger.stage4NextState(nextWorldResult);
        
        
        final var commitStream = createCommitStream(ref, nextWorldResult, commitBuilder);
        final var newTagStream = createNewTagStream(nextWorldResult, tenant);
        final var updateTagStream = createUpdateTagStream(nextWorldResult, tenant);

        return commitStream
            .onItem().transformToUni(ignore -> newTagStream)
            .onItem().transformToUni(ignore -> updateTagStream)
            .onItem().transform(ignore -> mapped)
            .onFailure().invoke((e) -> logger.closeWithFailure(e))
            .onItem().invoke(() -> logger.close());

      });
  }
  
  
  private Uni<Void> createCommitStream(Optional<Ref> ref, NextWorldResult nextWorldResult, CommitBuilder commitBuilder) {
    if(nextWorldResult.isCommits()) {
      return commitBuilder
        .branchName(branchName)
        .branchLock(ref.map(r -> r.getCommitId()).orElse(null)).build()
        .onItem().transformToUni(ignore -> Uni.createFrom().voidItem());
    }
    return Uni.createFrom().voidItem();
  }

  private Uni<Void> createNewTagStream(NextWorldResult nextWorldResult, FileSystemTenant tenant) {
    if(nextWorldResult.getNewDeployment().isEmpty()) {
      return Uni.createFrom().voidItem();
    }
    
    final var deployment = nextWorldResult.getNewDeployment().get();
    return tenant.createTag()
        .commitId(deployment.getFromRefId())
        
        .tagAuthor(deployment.getCreatedBy())
        .tagCreatedAt(deployment.getCreatedAt())

        .beforeTagCompletion((TagTransitives loaded, TagBuilder builder) -> {
          if(!loaded.getCommit().getId().equals(deployment.getFromCommitId())) {
            throw new WorldLockException();
          }
        })
        
        .newTag(props -> props
            .externalId(deployment.getExternalId())
            
            .tagName(deployment.getName())
            .tagDescription(deployment.getDescription())
            .tagStartsAt(deployment.getStartsAt())
            .build()
        )
        .build()
    .onItem().transformToUni(ignore -> Uni.createFrom().voidItem());
  }
  
  
  private Uni<Void> createUpdateTagStream(NextWorldResult nextWorldResult, FileSystemTenant tenant) {
    if(nextWorldResult.getUpdateDeployment().isEmpty()) {
      return Uni.createFrom().voidItem();
    }
    
    final var model = nextWorldResult.getUpdateDeployment().get();
    final var deployment = model.getBody();
    
    return tenant.modifyTag()
        .tagId(model.getId())
        .tagAuthor(deployment.getCreatedBy())
        .modifyTag((prev, props) -> props
            .externalId(deployment.getExternalId())
            .tagName(deployment.getName())
            .tagDescription(deployment.getDescription())
            .tagStartsAt(deployment.getStartsAt())
            .build()
        )
        .build()
    .onItem().transformToUni(ignore -> Uni.createFrom().voidItem());
  }
}
