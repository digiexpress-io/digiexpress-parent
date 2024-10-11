package io.resys.thena.docdb.spi.commits;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.resys.thena.docdb.api.actions.CommitActions.CommitResult;
import io.resys.thena.docdb.api.actions.CommitActions.CommitStatus;
import io.resys.thena.docdb.api.actions.CommitActions.HeadCommitBuilder;
import io.resys.thena.docdb.api.actions.ImmutableCommitResult;
import io.resys.thena.docdb.api.actions.ObjectsActions;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsResult;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsStatus;
import io.resys.thena.docdb.api.actions.ObjectsActions.RefObjects;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.commits.CommitVisitor.CommitOutputStatus;
import io.resys.thena.docdb.spi.support.Identifiers;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class HeadCommitBuilderDefault implements HeadCommitBuilder {

  private final ClientState state;
  private final ObjectsActions objectsActions;
  private final Map<String, String> appendBlobs = new HashMap<>();
  private final List<String> deleteBlobs = new ArrayList<>();
  
  private String headGid;
  private String repoId;
  private String headName;
  private String author;
  private String message;
  private String parentCommit;
  private Boolean parentIsLatest;

  @Override
  public HeadCommitBuilder id(String headGid) {
    RepoAssert.isEmpty(headName, () -> "Can't defined id when head is defined!");
    this.headGid = headGid;
    return this;
  }
  @Override
  public HeadCommitBuilder head(String repoId, String headName) {
    RepoAssert.isEmpty(headGid, () -> "Can't defined head when id is defined!");
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(headName, () -> "headName can't be empty!");
    RepoAssert.isName(headName, () -> "headName has invalid charecters!");
    this.repoId = repoId;
    this.headName = headName;
    return this;
  }
  @Override
  public HeadCommitBuilder append(String name, String blob) {
    RepoAssert.notEmpty(blob, () -> "blob can't be empty!");
    RepoAssert.notEmpty(name, () -> "name can't be empty!");
    RepoAssert.isTrue(!this.appendBlobs.containsKey(name), () -> "Blob with name: '" + name + "' is already defined!");
    RepoAssert.isTrue(!this.deleteBlobs.contains(name), () -> "Blob with name: '" + name + "' can't be appended because it's been marked for removal!");
    this.appendBlobs.put(name, blob);
    return this;
  }
  @Override
  public HeadCommitBuilder remove(String name) {
    RepoAssert.isTrue(!this.appendBlobs.containsKey(name), () -> "Blob with name: '" + name + "' can't be marked for removal because it's beed appended!");
    RepoAssert.isTrue(!this.deleteBlobs.contains(name), () -> "Blob with name: '" + name + "' is already marked for removal!");
    RepoAssert.notEmpty(name, () -> "name can't be empty!");
    this.deleteBlobs.add(name);
    return this;
  }
  @Override
  public HeadCommitBuilder author(String author) {
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    this.author = author;
    return this;
  }
  @Override
  public HeadCommitBuilder message(String message) {
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    this.message = message;
    return this;
  }
  @Override
  public HeadCommitBuilder parent(String parentCommit) {
    this.parentCommit = parentCommit;
    return this;
  }
  @Override
  public Uni<CommitResult> build() {
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.isTrue(!appendBlobs.isEmpty() || !deleteBlobs.isEmpty(), () -> "Nothing to commit, no content!");
    
    if(this.headGid != null) {
      final var id = Identifiers.fromRepoHeadGid(this.headGid);
      this.repoId = id[0];
      this.headName = id[1];
    }
    RepoAssert.notEmpty(repoId, () -> "Can't resolve repoId!");
    RepoAssert.notEmpty(headName, () -> "Can't resolve headName!");
    
    final String gid = Identifiers.toRepoHeadGid(repoId, headName);
    
    return objectsActions.refState().repo(this.repoId).ref(headName).build()
        .onItem().transformToUni(state -> {
          final var result = validateState(state, gid);
          if(result != null) {
            return Uni.createFrom().item(result);
          }
          return createCommit(state, gid);
        });
  }
  
  private CommitResult validateState(ObjectsResult<RefObjects> state, String gid) {
    if(state.getStatus() == ObjectsStatus.ERROR) {
      return ImmutableCommitResult.builder()
          .gid(gid)
          .addAllMessages(state.getMessages())
          .status(CommitStatus.ERROR)
          .build();
    }
    
    
    // Unknown parent
    if(state.getObjects() == null && parentCommit != null) {
      return (CommitResult) ImmutableCommitResult.builder()
          .gid(gid)
          .addMessages(ImmutableMessage.builder()
              .text(new StringBuilder()
                  .append("Commit to head: '").append(headName).append("'")
                  .append(" is rejected.")
                  .append(" Your head is: '").append(parentCommit).append("')")
                  .append(" but remote has no head.").append("'!")
                  .toString())
              .build())
          .status(CommitStatus.ERROR)
          .build();
      
    }
    
    
    // No parent commit defined for existing head
    if(state.getObjects() != null && parentCommit == null && !Boolean.TRUE.equals(this.parentIsLatest)) {
      return (CommitResult) ImmutableCommitResult.builder()
          .gid(gid)
          .addMessages(ImmutableMessage.builder()
              .text(new StringBuilder()
                  .append("Parent commit can only be undefined for the first commit!")
                  .append(" Parent commit for")
                  .append(" head: '").append(headName).append("'")
                  .append(" is: '").append(state.getObjects().getRef().getCommit()).append("'!")
                  .toString())
              .build())
          .status(CommitStatus.ERROR)
          .build();
    }
    
    // Wrong parent commit
    if(state.getObjects() != null && parentCommit != null && 
        !parentCommit.equals(state.getObjects().getRef().getCommit()) &&
        !Boolean.TRUE.equals(this.parentIsLatest)) {
      
      final var text = new StringBuilder()
        .append("Commit to head: '").append(headName).append("'")
        .append(" is rejected.")
        .append(" Your head is: '").append(parentCommit).append("')")
        .append(" but remote is: '").append(state.getObjects().getRef().getCommit()).append("'!")
        .toString();
      
      return ImmutableCommitResult.builder()
          .gid(gid)
          .addMessages(ImmutableMessage.builder().text(text).build())
          .status(CommitStatus.ERROR)
          .build();
    }

    return null;
  }
  
  private Uni<CommitResult> createCommit(ObjectsResult<RefObjects> state, String gid) {
    final var toBeSaved = new CommitVisitor().visit(
        ImmutableCommitInput.builder()
          .commitAuthor(this.author)
          .commitMessage(this.message)
          .ref(headName)
          .append(appendBlobs)
          .remove(deleteBlobs)
          .repo(state.getRepo())
          .parent(Optional.ofNullable(state.getObjects()))
          .build());
    return new CommitSaveVisitor(this.state.withRepo(state.getRepo())).visit(toBeSaved)
        .onItem().transform(saved -> (CommitResult) ImmutableCommitResult.builder()
          .gid(gid)
          .commit(saved.getCommit())
          .addAllMessages(saved.getMessages())
          .addMessages(saved.getLog())
          .status(visitStatus(saved.getStatus()))
          .build());
  }
  
  private CommitStatus visitStatus(CommitOutputStatus src) {
    if(src == CommitOutputStatus.OK) {
      return CommitStatus.OK;
    } else if(src == CommitOutputStatus.CONFLICT) {
      return CommitStatus.CONFLICT;
    }
    return CommitStatus.ERROR;
    
  }
  @Override
  public HeadCommitBuilder parentIsLatest() {
    this.parentIsLatest = true;
    return this;
  }
}
