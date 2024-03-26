package io.resys.thena.api.models;

import java.util.Arrays;
import java.util.Collections;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;
import org.slf4j.Logger;

import io.resys.thena.api.exceptions.RepoException;
import io.resys.thena.api.models.ImmutableMessage;
import io.resys.thena.api.models.ImmutableQueryEnvelope;
import io.resys.thena.api.models.ImmutableQueryEnvelopeList;
import io.resys.thena.api.models.ThenaEnvelope.ThenaObjects;
import io.resys.thena.api.models.ThenaGitObject.Commit;
import io.resys.thena.structures.git.objects.PullObjectsQueryImpl.BlobAndTree;


@Value.Immutable
public interface QueryEnvelope<T extends ThenaObjects> extends ThenaEnvelope {
  @Nullable
  Repo getRepo();    
  @Nullable
  T getObjects();
  
  QueryEnvelopeStatus getStatus();
  List<Message> getMessages();
  
  enum QueryEnvelopeStatus { OK, ERROR }

  public default QueryEnvelopeList<T> toList() {
    return ImmutableQueryEnvelopeList
        .<T>builder()
        .status(getStatus())
        .addAllMessages(this.getMessages())
        .repo(this.getRepo())
        .addAllObjects(this.getObjects() == null ? Collections.emptyList() : Arrays.asList(this.getObjects()))
        .build();
  }

  public default <X extends ThenaObjects> QueryEnvelopeList<X> toListOfType() {
    return ImmutableQueryEnvelopeList
        .<X>builder()
        .status(getStatus())
        .addAllMessages(this.getMessages())
        .repo(this.getRepo())
        .build();
  }
  public default <X extends ThenaObjects> QueryEnvelope<X> toType() {
    return ImmutableQueryEnvelope
        .<X>builder()
        .status(getStatus())
        .addAllMessages(this.getMessages())
        .repo(this.getRepo())
        .build();
  }
  
  
  public static <T extends ThenaEnvelope.ThenaObjects> QueryEnvelope<T> repoBlobNotFound(
      Repo repo, 
      BlobAndTree blobAndTree, 
      Commit commit,
      List<String> docIds,
      Logger logger) {
    
    final var error = RepoException.builder()
        .noBlob(repo, blobAndTree.getTreeId(), commit.getId(), docIds.toArray(new String[] {}));
    logger.warn(error.getText());
    return ImmutableQueryEnvelope
        .<T>builder()
        .status(QueryEnvelopeStatus.ERROR)
        .addMessages(error)
        .build();
  }
  public static <T extends ThenaEnvelope.ThenaObjects> QueryEnvelope<T> repoCommitNotFound(Repo repo, String refCriteria, Logger logger) {
    final var error = RepoException.builder().noCommit(repo, refCriteria);
    logger.warn(error.getText());
    return ImmutableQueryEnvelope
        .<T>builder()
        .status(QueryEnvelopeStatus.ERROR)
        .addMessages(error)
        .build();
  }
  public static <T extends ThenaEnvelope.ThenaObjects> QueryEnvelope<T> repoNotFound(String repoId, Logger logger) {
    final var ex = RepoException.builder().notRepoWithName(repoId);
    logger.warn(ex.getText());
    return ImmutableQueryEnvelope
        .<T>builder()
        .status(QueryEnvelopeStatus.ERROR)
        .addMessages(ex)
        .build();
  }
  public static <T extends ThenaEnvelope.ThenaObjects> QueryEnvelopeList<T> repoNotFoundList(String repoId, Logger logger) {
    final var ex = RepoException.builder().notRepoWithName(repoId);
    logger.warn(ex.getText());
    return ImmutableQueryEnvelopeList
        .<T>builder()
        .status(QueryEnvelopeStatus.ERROR)
        .addMessages(ex)
        .build();
  }
  
  public static <T extends ThenaEnvelope.ThenaObjects> QueryEnvelope<T> docNotFound(
      Repo existing, Logger logger, String text,
      DocNotFoundException ex
    ) {
    return ImmutableQueryEnvelope.<T>builder()
      .repo(existing)
      .status(QueryEnvelopeStatus.ERROR)
      .addMessages(ImmutableMessage.builder().exception(ex).text(text).build())
      .build();
  }
  
  public static <T extends ThenaEnvelope.ThenaObjects> QueryEnvelope<T> docUnexpected(Repo existing, Logger logger, String text) {
    logger.warn(text);
    return ImmutableQueryEnvelope.<T>builder()
      .repo(existing)
      .status(QueryEnvelopeStatus.ERROR)
      .addMessages(ImmutableMessage.builder().text(text).build())
      .build();
  }
  
  public static <T extends ThenaEnvelope.ThenaObjects> QueryEnvelope<T> fatalError(Repo existing, String msg, Logger logger, Throwable t) {
    logger.error(msg, t);
    return ImmutableQueryEnvelope.<T>builder()
      .repo(existing)
      .status(QueryEnvelopeStatus.ERROR)
      .addMessages(ImmutableMessage.builder().text(msg).exception(t).build())
      .addMessages(ImmutableMessage.builder().text(t.getMessage()).build())
      .build();
  }
  
  public static class DocNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 5293530111825347496L;
    
  }
}
