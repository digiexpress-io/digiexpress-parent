package io.resys.thena.fs.spi.tag;

import java.util.Collections;

/*-
 * #%L
 * thena-fs-client
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

import java.util.Optional;
import java.util.function.BiConsumer;

import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.fs.api.tags.ImmutableTagResult;
import io.resys.thena.fs.api.tags.ModifyTag;
import io.resys.thena.fs.api.tags.TagBuilder;
import io.resys.thena.fs.api.tags.TagBuilder.BeforeTagCompletion;
import io.resys.thena.fs.api.tags.TagResult;
import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.ImmutableTagTransitives;
import io.resys.thena.fs.entities.Tag;
import io.resys.thena.fs.entities.Tree;
import io.resys.thena.fs.spi.commit.CommitBuilderException;
import io.resys.thena.fs.spi.commit.CommitBuilderImpl;
import io.resys.thena.fs.tables.CommitTable.NodesAndBlobsFilter;
import io.resys.thena.fs.tables.FsDb;
import io.resys.thena.fs.tables.FsDbBuilder.FsBuilderException;
import io.resys.thena.fs.tables.FsDbBuilder.PersistenceUnit;
import io.resys.thena.fs.tables.ImmutablePersistenceUnit;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class ModifyTagImpl implements ModifyTag {
  private final Uni<FsDb> db_uni;
  private final String tenantId;
  
  private String tagAuthor;
  private String tagIdOrName;
  private BiConsumer<Tag, TagBuilder> tagBuilder;
  private BeforeTagCompletion callback;
  
  @Override
  public ModifyTag tagId(String tagIdOrName) {
    this.tagIdOrName = RepoAssert.notEmpty(tagIdOrName, () -> "tagIdOrName cannot be empty!");
    return this;
  }
  @Override
  public ModifyTag modifyTag(BiConsumer<Tag, TagBuilder> tagBuilder) {
    this.tagBuilder = RepoAssert.notNull(tagBuilder, () -> "tagBuilder cannot be empty!");
    return this;
  }
  @Override
  public ModifyTag beforeTagCompletion(BeforeTagCompletion callback) {
    this.callback = RepoAssert.notNull(callback, () -> "beforeTagCompletion cannot be empty!");
    return this;
  }
  @Override
  public ModifyTag tagAuthor(String tagAuthor) {
    this.tagAuthor = RepoAssert.notEmpty(tagAuthor, () -> "tagAuthor cannot be empty!");
    return this;
  }
  @Override
  public Uni<TagResult> build() {
    // Double validation - ensure required fields were set
    RepoAssert.notEmpty(tagIdOrName, () -> "tagIdOrName cannot be empty!");
    RepoAssert.notNull(tagBuilder, () -> "tagBuilder cannot be empty!");
    RepoAssert.notEmpty(tagAuthor, () -> "tagAuthor cannot be empty!");

    final var scope = ImmutableTxScope.builder()
        .commitAuthor(tagAuthor)
        .commitMessage("creating new tag")
        .tenantId(tenantId)
        .build();
    return this.db_uni
        .onItem().transformToUni(db -> db.withTransaction(scope, this::visitTransaction))
        .onFailure(t -> {
          // force crash on anything
          return false;
        })
        .recoverWithItem(this::visitFailure);
  }

  private Uni<TagResult> visitTransaction(FsDb tx) {
    return tx.query().queryTag().findByTagName(tagIdOrName)
        .map(found -> {
          if(found.isEmpty()) {
            throw new TagCreationException("Can't find tag by given id", JsonObject.of("id", tagIdOrName));
          }
          return found.get();
        })
        .onItem().transformToUni(found -> {
          if(callback != null) {
            return tx.query().queryCommit().getByIdWithNodesAndBlobs(new NodesAndBlobsFilter(found.getCommitId(), Collections.emptyList()))
              .map(tuple -> new TagModRequest(found, Optional.of(tuple.getItem1()), tuple.getItem2()));  
          }
          return Uni.createFrom().item(new TagModRequest(found, Optional.empty(), Optional.empty()));
        })
        .onItem().transformToUni(request -> visitPersistenceUnit(tx, request))
        .onItem().transform(this::visitSuccess);
  }
  
  private TagResult visitFailure(Throwable t) {
    if(t instanceof FsBuilderException) {
      final var fs = (FsBuilderException) t;
      final var builder = ImmutableTagResult.builder()
          .tenantId(tenantId)
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .exception(fs)
              .text(fs.getMessage())
              .build());
      
      if(t.getCause() != null) {
        return builder
          .addMessages(ImmutableMessage.builder().text(fs.getCause().getMessage()).build())
          .build();
      }
      
      return builder.build();
    }
    
    return ImmutableTagResult.builder()
        .tenantId(tenantId)
        .status(CommitResultStatus.ERROR)
        .addMessages(ImmutableMessage.builder()
            .text("Tag creation has failed with unknown exception!")
            .exception(t)
            .build())
        .build();
  }
  
  private TagResult visitSuccess(PersistenceUnit unit) {
    return ImmutableTagResult.builder()
      .addAllMessages(
          unit.getCommitMessages().stream()
            .map(text -> ImmutableMessage.builder().text(text).build())
            .toList()
      )
      .addAllMessages(unit.getCommitLogs())
      .tenantId(tenantId)
      .status(CommitBuilderImpl.mapCommitStatus(unit.getStatus()))
      .tag(unit.getTagInserts().isEmpty() ? null : unit.getTagInserts().getLast())
      .build();
  }
  


  private Uni<PersistenceUnit> visitPersistenceUnit(FsDb tx, TagModRequest request) {
    try {
      final var createdAt = request.getTag().getTagCreatedAt();
      final var prevTag = Optional.<Tag>empty();
      final var commitId = request.getTag().getCommitId();
      final var refId = request.getTag().getRefId();
      final var tagTransitives = ImmutableTagTransitives.builder().build();  
      
      final var tagBuilder = new TagBuilderImpl(prevTag, commitId, refId, tagTransitives, createdAt, tagAuthor);
      this.tagBuilder.accept(request.getTag(), tagBuilder);
      if(this.callback != null) {
        this.callback.apply(tagTransitives, tagBuilder);
      }
      final var result = tagBuilder.close();
      
      final var unit = ImmutablePersistenceUnit.builder()
          .tenantId(tenantId)
          .status(BatchStatus.OK)
          .log("")
          .addTagUpdates(result.getTag())
          .build();
      return tx.builder().from(unit).persist();
    } catch(Exception e) {
      throw new CommitBuilderException(e, JsonObject.of("error", e.getMessage(), "tenantId", tenantId, "message", e.getMessage()));
    }
  }
  
  @Value
  private static class TagModRequest {
    Tag tag;
    Optional<Commit> commit; 
    Optional<Tree> tree;
  }
}
