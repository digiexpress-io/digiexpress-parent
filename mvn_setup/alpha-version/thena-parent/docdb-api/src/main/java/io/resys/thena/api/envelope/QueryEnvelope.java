package io.resys.thena.api.envelope;

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

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.exceptions.RepoException;
import io.resys.thena.structures.git.objects.PullObjectsQueryImpl.BlobAndTree;


@Value.Immutable
public interface QueryEnvelope<T extends ThenaContainer> extends ThenaEnvelope {
  @Nullable
  Tenant getRepo();    
  @Nullable
  T getObjects();
  
  QueryEnvelopeStatus getStatus();
  List<Message> getMessages();
  
  default boolean isNotFound() {
    if(getStatus() == QueryEnvelopeStatus.OK) {
      return false;
    }
    if(getMessages().isEmpty()) {
      return false;
    }
    return getMessages().stream()
      .filter(m -> m.getException() != null)
      .filter(m -> m.getException() instanceof DocNotFoundException)
      .findFirst().isPresent();
  }
  
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

  public default <X extends ThenaContainer> QueryEnvelopeList<X> toListOfType() {
    return ImmutableQueryEnvelopeList
        .<X>builder()
        .status(getStatus())
        .addAllMessages(this.getMessages())
        .repo(this.getRepo())
        .build();
  }
  public default <X extends ThenaContainer> QueryEnvelope<X> toType() {
    return ImmutableQueryEnvelope
        .<X>builder()
        .status(getStatus())
        .addAllMessages(this.getMessages())
        .repo(this.getRepo())
        .build();
  }
  
  
  public static <T extends ThenaContainer> QueryEnvelope<T> repoBlobNotFound(
      Tenant repo, 
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
  public static <T extends ThenaContainer> QueryEnvelope<T> repoCommitNotFound(Tenant repo, String refCriteria, Logger logger) {
    final var error = RepoException.builder().noCommit(repo, refCriteria);
    logger.warn(error.getText());
    return ImmutableQueryEnvelope
        .<T>builder()
        .status(QueryEnvelopeStatus.ERROR)
        .addMessages(error)
        .build();
  }
  public static <T extends ThenaContainer> QueryEnvelope<T> repoNotFound(String repoId, Logger logger) {
    final var ex = RepoException.builder().notRepoWithName(repoId);
    logger.warn(ex.getText());
    return ImmutableQueryEnvelope
        .<T>builder()
        .status(QueryEnvelopeStatus.ERROR)
        .addMessages(ex)
        .build();
  }
  public static <T extends ThenaContainer> QueryEnvelopeList<T> repoNotFoundList(String repoId, Logger logger) {
    final var ex = RepoException.builder().notRepoWithName(repoId);
    logger.warn(ex.getText());
    return ImmutableQueryEnvelopeList
        .<T>builder()
        .status(QueryEnvelopeStatus.ERROR)
        .addMessages(ex)
        .build();
  }
  
  public static <T extends ThenaContainer> QueryEnvelope<T> docNotFound(
      Tenant existing, Logger logger, String text,
      DocNotFoundException ex
    ) {
    return ImmutableQueryEnvelope.<T>builder()
      .repo(existing)
      .status(QueryEnvelopeStatus.ERROR)
      .addMessages(ImmutableMessage.builder().exception(ex).text(text).build())
      .build();
  }
  
  public static <T extends ThenaContainer> QueryEnvelope<T> docUnexpected(Tenant existing, Logger logger, String text) {
    logger.warn(text);
    return ImmutableQueryEnvelope.<T>builder()
      .repo(existing)
      .status(QueryEnvelopeStatus.ERROR)
      .addMessages(ImmutableMessage.builder().text(text).build())
      .build();
  }
  
  public static <T extends ThenaContainer> QueryEnvelope<T> fatalError(Tenant existing, String msg, Logger logger, Throwable t) {
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
