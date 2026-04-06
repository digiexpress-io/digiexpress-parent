package io.resys.hdes.client.spi.store;

/*-
 * #%L
 * hdes-client
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

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import io.resys.hdes.client.api.HdesStore.CommitLog;
import io.resys.hdes.client.api.HdesStore.CommitLogBuilder;
import io.resys.hdes.client.api.ImmutableCommitLog;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommitLogBuilderThenaImpl implements CommitLogBuilder {
  private final ThenaConfig config;

  @Override
  public Uni<List<CommitLog>> build() {
    final var repoName = config.getRepoName();
    final var headName = config.getHeadName();

    return config.getClient().git(repoName).tenants().get().onItem()
      .transformToUni(repo -> {
        if(repo == null || repo.getStatus() != QueryEnvelopeStatus.OK) {
         return Uni.createFrom().item(Collections.emptyList()); 
        }
      
        return config.getClient().git(repoName)
            .history().blobCommitQuery().branchName(headName).includBlob(false).findAll()
            .onItem().transform(state -> {
              if(state.getStatus() == QueryEnvelopeStatus.ERROR) {
                return Collections.emptyList();
              }

              // Nothing present
              if(state.getObjects() == null) {
                return Collections.emptyList();
              }

              return state.getObjects().getValues().stream().map(log -> {
                final CommitLog result = ImmutableCommitLog.builder()
                    .commitId(log.getCommit().getId())
                    .createdAt(log.getCommit().getDateTime()
                        .atZone(ZoneId.systemDefault())
                        .withZoneSameInstant(ZoneOffset.UTC)
                        .toOffsetDateTime())
                    .createdBy(log.getCommit().getAuthor())
                    .objectId(log.getResourceName())
                    .build();
                
                return result;
              }).toList();
            });
      });
  }
}




