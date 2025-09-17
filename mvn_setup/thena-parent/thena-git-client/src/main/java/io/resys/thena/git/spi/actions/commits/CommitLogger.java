package io.resys.thena.git.spi.actions.commits;

import java.util.List;

import org.slf4j.Logger;

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

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.ThenaContainer;
import io.resys.thena.git.spi.actions.objects.PullObjectsQueryImpl.BlobAndTree;
import io.resys.thena.git.spi.support.RepoException;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = LogConstants.SHOW_COMMIT)
public class CommitLogger {
  private final StringBuilder data = new StringBuilder();
  
  public CommitLogger append(String data) {
    if(log.isDebugEnabled()) {
      this.data.append(data);
    }
    return this;
  }
  @Override
  public String toString() {
    if(log.isDebugEnabled()) {
      log.debug(data.toString());
    } else {
      data.append("Log DEBUG disabled for: " + LogConstants.SHOW_COMMIT + "!");
    }
    return data.toString();
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
} 
