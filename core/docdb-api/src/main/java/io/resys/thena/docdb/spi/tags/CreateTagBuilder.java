package io.resys.thena.docdb.spi.tags;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import java.time.LocalDateTime;

import io.resys.thena.docdb.api.actions.ImmutableTagResult;
import io.resys.thena.docdb.api.actions.TagActions.TagBuilder;
import io.resys.thena.docdb.api.actions.TagActions.TagResult;
import io.resys.thena.docdb.api.actions.TagActions.TagResultStatus;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ImmutableTag;
import io.resys.thena.docdb.api.models.ThenaObject.Branch;
import io.resys.thena.docdb.api.models.ThenaObject.Commit;
import io.resys.thena.docdb.api.models.ThenaObject.Tag;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.ClientState.ClientRepoState;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateTagBuilder implements TagBuilder {
  private final ClientState state;
  
  private String repoId;
  private String commitIdOrHead;
  private String tagName;
  private String author;
  private String message;
  
  @Override
  public TagBuilder tagName(String tagName) {
    this.tagName = tagName;
    return this;
  }
  @Override
  public TagBuilder head(String repoId, String commitIdOrHead) {
    this.repoId = repoId;
    this.commitIdOrHead = commitIdOrHead;
    return this;
  }
  @Override
  public TagBuilder author(String author) {
    this.author = author;
    return this;
  }
  @Override
  public TagBuilder message(String message) {
    this.message = message;
    return this;
  }
  @Override
  public Uni<TagResult> build() {
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(commitIdOrHead, () -> "commitIdOrHead can't be empty!");
    RepoAssert.notEmpty(tagName, () -> "tagName can't be empty!");
    
    return state.project().getByNameOrId(repoId).onItem()
        .transformToUni(repo -> {
          if(repo == null) {
            return Uni.createFrom().item(ImmutableTagResult.builder()
                .status(TagResultStatus.ERROR)
                .addMessages(ImmutableMessage.builder()
                    .text(new StringBuilder()
                        .append("Can't create tag: '").append(tagName).append("'")
                        .append(" because there is no repository with id or name: '").append(repoId).append("'!")
                        .toString())
                    .build())
                .build());
          }
          
          final var ctx = this.state.withRepo(repo);
          return findRef(ctx, commitIdOrHead).onItem()
            .transformToUni(ref -> findCommit(ctx, ref == null ? commitIdOrHead : ref.getCommit())).onItem()
            .transformToUni(commit -> {
              
              if(commit == null) {
                return Uni.createFrom().item((TagResult) ImmutableTagResult.builder()
                    .status(TagResultStatus.ERROR)
                    .addMessages(ImmutableMessage.builder()
                        .text(new StringBuilder()
                            .append("Can't create tag: '").append(tagName).append("'")
                            .append(" because there is no commit or head: '").append(commitIdOrHead).append("'!")
                            .toString())
                        .build())
                    .build());
              }
              
              return findTag(ctx, tagName).onItem()
                .transformToUni(existingTag -> {
                  if(existingTag != null) {
                    return Uni.createFrom().item((TagResult) ImmutableTagResult.builder()
                        .status(TagResultStatus.ERROR)
                        .addMessages(ImmutableMessage.builder()
                            .text(new StringBuilder()
                                .append("Can't create tag: '").append(tagName).append("'")
                                .append(" because there is already tag with the same name with commit: '").append(existingTag.getCommit()).append("'!")
                                .toString())
                            .build())
                        .build());
                  }
                  return createTag(ctx, commit.getId());  
                });
            });
        });
  }

  private Uni<Tag> findTag(ClientRepoState state, String tagName) {
    return state.query().tags().name(tagName).getFirst();
  }

  private Uni<Branch> findRef(ClientRepoState state, String refNameOrCommit) {
    return state.query().refs().nameOrCommit(refNameOrCommit);
  }
  
  private Uni<Commit> findCommit(ClientRepoState state, String commit) {
    return state.query().commits().getById(commit);
  }
  
  private Uni<TagResult> createTag(ClientRepoState state, String commit) {
    final var tag = ImmutableTag.builder()
        .commit(commit)
        .name(tagName)
        .message(message)
        .author(author)
        .dateTime(LocalDateTime.now())
        .build();
    return state.insert()
        .tag(tag)
        .onItem().transform(inserted -> {
          if(inserted.getDuplicate()) {
            return (TagResult) ImmutableTagResult.builder()
                .status(TagResultStatus.ERROR)
                .tag(tag)
                .addMessages(ImmutableMessage.builder()
                    .text(new StringBuilder()
                        .append("Tag with name:")
                        .append(" '").append(tagName).append("'")
                        .append(" is already created.")
                        .toString())
                    .build())
                .build();
          } 
          return (TagResult) ImmutableTagResult.builder()
              .status(TagResultStatus.OK).tag(tag)
              .build();
        });
  }
}
